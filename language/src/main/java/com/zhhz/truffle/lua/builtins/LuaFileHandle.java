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
    private boolean closed;

    private LuaFileHandle(Shape shape, RandomAccessFile file, InputStream input, OutputStream output,
                          Process process, boolean standard) {
        super(shape);
        this.file = file;
        this.input = input == null ? null : new PushbackInputStream(input, 1);
        this.output = output;
        this.process = process;
        this.standard = standard;
    }

    static LuaFileHandle open(Shape shape, Path path, String mode) throws IOException {
        boolean append = mode.startsWith("a");
        boolean writable = append || mode.startsWith("w") || mode.contains("+");
        RandomAccessFile file = new RandomAccessFile(path.toFile(), writable ? "rw" : "r");
        if (mode.startsWith("w")) file.setLength(0);
        if (append) file.seek(file.length());
        return new LuaFileHandle(shape, file, null, null, null, false);
    }

    public static LuaFileHandle stream(Shape shape, InputStream input, OutputStream output, boolean standard) {
        return new LuaFileHandle(shape, null, input, output, null, standard);
    }

    static LuaFileHandle process(Shape shape, Process process, InputStream input, OutputStream output) {
        return new LuaFileHandle(shape, null, input, output, process, false);
    }

    int read() throws IOException {
        ensureOpen();
        if (file != null) return file.read();
        if (input == null) throw new IOException("file is not readable");
        return input.read();
    }

    void unread(int value) throws IOException {
        if (value < 0) return;
        if (file != null) file.seek(file.getFilePointer() - 1);
        else input.unread(value);
    }

    void write(byte[] bytes) throws IOException {
        ensureOpen();
        if (file != null) file.write(bytes);
        else if (output != null) output.write(bytes);
        else throw new IOException("file is not writable");
    }

    void flush() throws IOException {
        ensureOpen();
        if (output != null) output.flush();
        if (file != null) file.getFD().sync();
    }

    long seek(String whence, long offset) throws IOException {
        ensureOpen();
        if (file == null) throw new IOException("file is not seekable");
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
        if (file != null) file.close();
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

    @ExportMessage
    Object getMembers(boolean includeInternal) {
        return new String[]{"close", "flush", "read", "write", "seek", "setvbuf", "lines"};
    }
}
