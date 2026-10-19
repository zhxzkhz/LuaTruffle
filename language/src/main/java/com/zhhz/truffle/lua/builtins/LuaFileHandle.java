package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.zhhz.truffle.lua.runtime.LuaValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;

@ExportLibrary(InteropLibrary.class)
public final class LuaFileHandle extends LuaValue {
    private final RandomAccessFile file;
    private final PushbackInputStream input;
    private final OutputStream output;
    private final Process process;
    private final boolean standard;
    private final boolean readable;
    private final boolean writable;
    private boolean closed;

    // 模拟 C stdio 的写缓冲。默认直写（不缓冲）；setvbuf 可切换为 "no"/"line"/"full"。
    private static final int MODE_NO = 0;
    private static final int MODE_FULL = 1;
    private static final int MODE_LINE = 2;
    private int bufferMode = MODE_NO;
    private int bufferCapacity = 8192;
    private byte[] writeBuffer = new byte[8192];
    private int writeBufferSize = 0;

    private LuaFileHandle(Shape shape, RandomAccessFile file, InputStream input, OutputStream output,
                          Process process, boolean standard, boolean readable, boolean writable) {
        super(shape);
        this.file = file;
        this.input = input == null ? null : new PushbackInputStream(input, 1);
        this.output = output;
        this.process = process;
        this.standard = standard;
        this.readable = readable;
        this.writable = writable;
    }

    static LuaFileHandle open(Shape shape, Path path, String mode) throws IOException {
        boolean append = mode.startsWith("a");
        boolean updatable = mode.contains("+");
        boolean readable = mode.startsWith("r") || updatable;
        boolean writable = mode.startsWith("w") || append || updatable;
        RandomAccessFile file = new RandomAccessFile(path.toFile(), writable ? "rw" : "r");
        if (mode.startsWith("w")) file.setLength(0);
        if (append) file.seek(file.length());
        LuaFileHandle handle = new LuaFileHandle(shape, file, null, null, null, false, readable, writable);
        handle.registerFinalizer();
        return handle;
    }

    public static LuaFileHandle stream(Shape shape, InputStream input, OutputStream output, boolean standard) {
        return new LuaFileHandle(shape, null, input, output, null, standard, input != null, output != null);
    }

    static LuaFileHandle process(Shape shape, Process process, InputStream input, OutputStream output) {
        LuaFileHandle handle = new LuaFileHandle(shape, null, input, output, process, false,
                input != null, output != null);
        handle.registerFinalizer();
        return handle;
    }

    /**
     * 注册一个终结动作：当本句柄不可达时由 collectgarbage() 关闭底层资源。
     * 注意：动作只捕获底层资源（而非 this），以免强引用阻止本句柄被回收。
     */
    private void registerFinalizer() {
        com.zhhz.truffle.lua.runtime.LuaContext context = com.zhhz.truffle.lua.runtime.LuaContext.get(null);
        if (context == null) return;
        RandomAccessFile fileResource = this.file;
        InputStream inputResource = this.input;
        OutputStream outputResource = this.output;
        context.registerFileHandle(this, () -> {
            closeQuietly(fileResource);
            closeQuietly(inputResource);
            closeQuietly(outputResource);
        });
    }

    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // 忽略终结过程中的错误
            }
        }
    }

    int read() throws IOException {
        ensureOpen();
        if (!readable) throw new IOException("Bad file descriptor");
        if (file != null) return file.read();
        return input.read();
    }

    void unread(int value) throws IOException {
        if (value < 0) return;
        if (file != null) file.seek(file.getFilePointer() - 1);
        else input.unread(value);
    }

    void write(byte[] bytes) throws IOException {
        ensureOpen();
        if (!writable) throw new IOException("Bad file descriptor");
        if (file != null) {
            if (bufferMode == MODE_NO) {
                file.write(bytes);
                return;
            }
            for (byte value : bytes) {
                writeBuffer[writeBufferSize++] = value;
                if (writeBufferSize >= bufferCapacity) {
                    flushWriteBuffer();
                } else if (bufferMode == MODE_LINE && value == '\n') {
                    flushWriteBuffer();
                }
            }
        } else if (output != null) {
            output.write(bytes);
            output.flush();
        } else {
            throw new IOException("file is not writable");
        }
    }

    private void flushWriteBuffer() throws IOException {
        if (writeBufferSize > 0) {
            file.write(writeBuffer, 0, writeBufferSize);
            writeBufferSize = 0;
        }
    }

    void flush() throws IOException {
        ensureOpen();
        // 对应 C 的 fflush：先把用户态缓冲写入操作系统，不做 fsync。
        if (file != null) flushWriteBuffer();
        if (output != null) output.flush();
    }

    void setvbuf(String mode, int size) {
        bufferMode = switch (mode) {
            case "no" -> MODE_NO;
            case "line" -> MODE_LINE;
            default -> MODE_FULL;
        };
        if (size > 0) {
            bufferCapacity = size;
            if (writeBuffer.length < size) writeBuffer = new byte[size];
        }
    }

    long seek(String whence, long offset) throws IOException {
        ensureOpen();
        if (file == null) throw new IOException("file is not seekable");
        flushWriteBuffer(); // 与 C 的 fseek 一致：定位前先刷新输出缓冲
        long base = switch (whence) {
            case "set" -> 0;
            case "cur" -> file.getFilePointer();
            case "end" -> file.length();
            default -> throw new IllegalArgumentException("invalid option '" + whence + "'");
        };
        long position = Math.addExact(base, offset);
        if (position < 0) throw new IOException("invalid argument");
        file.seek(position);
        return position;
    }

    Object closeHandle() throws IOException, InterruptedException {
        if (closed) throw new IOException("attempt to use a closed file");
        if (standard) return false;
        closed = true;
        if (file != null) {
            flushWriteBuffer();
            file.close();
        }
        if (input != null) input.close();
        if (output != null) output.close();
        return process == null ? true : process.waitFor() == 0;
    }

    boolean isClosed() {
        return closed;
    }

    private void ensureOpen() throws IOException {
        if (closed) throw new IOException("attempt to use a closed file");
    }

    @Override
    public String getTypeName() {
        return "FILE*";
    }

    @Override
    public String toString() {
        return closed ? "file (closed)" : "file (" + Integer.toHexString(System.identityHashCode(this)) + ")";
    }

    @ExportMessage
    boolean hasMembers() {
        return true;
    }

    // 导出语言身份：标识此宿主对象归属 Lua 语言（工具与调试器依赖）
    @ExportMessage
    boolean hasLanguageId() {
        return true;
    }

    @ExportMessage
    String getLanguageId() {
        return com.zhhz.truffle.lua.LuaLanguage.ID;
    }

    @ExportMessage
    Object getMembers(boolean includeInternal) {
        return new String[]{"close", "flush", "read", "write", "seek", "setvbuf", "lines"};
    }
}
