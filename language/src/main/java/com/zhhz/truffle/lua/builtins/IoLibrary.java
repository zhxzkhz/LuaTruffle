package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class IoLibrary {
    private IoLibrary() {
    }

    @LuaBuiltin(name = "open")
    @TruffleBoundary
    public static Object open(Object[] arguments) {
        String filename = stringArgument(arguments, 0, "open");
        String mode = arguments.length > 1 && !isNil(arguments[1]) ? string(arguments[1], 2, "open") : "r";
        if (!mode.matches("[rwa]\\+?b?")) throw argumentError(2, "open", "invalid mode");
        try {
            return LuaFileHandle.open(context().getFileShape(), context().resolveScriptPath(filename), mode);
        } catch (Exception exception) {
            return failure(exception);
        }
    }

    @LuaBuiltin(name = "input")
    public static Object input(Object[] arguments) {
        LuaContext context = context();
        if (arguments.length == 0 || isNil(arguments[0])) return context.getDefaultInput();
        LuaFileHandle handle = fileOrOpen(arguments[0], "r", "input");
        context.setDefaultInput(handle);
        return handle;
    }

    @LuaBuiltin(name = "output")
    public static Object output(Object[] arguments) {
        LuaContext context = context();
        if (arguments.length == 0 || isNil(arguments[0])) return context.getDefaultOutput();
        LuaFileHandle handle = fileOrOpen(arguments[0], "w", "output");
        context.setDefaultOutput(handle);
        return handle;
    }

    @LuaBuiltin(name = "close")
    public static Object close(Object[] arguments) {
        LuaFileHandle handle = arguments.length == 0 || isNil(arguments[0])
                ? context().getDefaultOutput() : file(arguments[0], 1, "close");
        // 标准 Lua 的 tofile：对已关闭的文件抛错（io.close 属于此路径）。
        if (handle.isClosed()) throw LuaException.create("attempt to use a closed file", null);
        return closeHandle(handle);
    }

    @LuaBuiltin(name = "flush")
    public static Object flush(Object[] arguments) {
        LuaFileHandle handle = arguments.length > 0 && arguments[0] instanceof LuaFileHandle file
                ? file : context().getDefaultOutput();
        try {
            handle.flush();
            return true;
        } catch (Exception exception) {
            return failure(exception);
        }
    }

    @LuaBuiltin(name = "read")
    public static Object read(Object[] arguments) {
        return readValues(requireDefaultInput(), arguments, 0, "read");
    }

    @LuaBuiltin(name = "write")
    public static Object write(Object[] arguments) {
        return writeValues(requireDefaultOutput(), arguments, 0, "write");
    }

    @LuaBuiltin(name = "tmpfile")
    public static Object tmpfile(Object[] arguments) {
        try {
            Path path = Files.createTempFile("lua_io_", ".tmp");
            path.toFile().deleteOnExit();
            return LuaFileHandle.open(context().getFileShape(), path, "w+");
        } catch (Exception exception) {
            return failure(exception);
        }
    }

    @LuaBuiltin(name = "type")
    public static Object type(Object[] arguments) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaFileHandle file)) return LuaNil.SINGLETON;
        return text(file.isClosed() ? "closed file" : "file");
    }

    @LuaBuiltin(name = "popen")
    @TruffleBoundary
    public static Object popen(Object[] arguments) {
        String command = stringArgument(arguments, 0, "popen");
        String mode = arguments.length > 1 && !isNil(arguments[1]) ? string(arguments[1], 2, "popen") : "r";
        if (!mode.equals("r") && !mode.equals("w")) throw argumentError(2, "popen", "invalid mode");
        // 与 os.execute 保持同一 shell：测试套件假设 Unix-like shell（&、$! 等语法），
        // Windows 上需经 Git Bash 执行而不是 cmd.exe
        ProcessBuilder builder;
        try {
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
                java.nio.file.Path script = java.nio.file.Files.createTempFile("lua_cmd_", ".sh");
                java.nio.file.Files.writeString(script, command + "\n");
                script.toFile().deleteOnExit();
                String bash = "D:\\Program Files\\Git\\bin\\bash.exe";
                builder = new ProcessBuilder(bash, script.toString().replace('\\', '/'));
            } else {
                builder = new ProcessBuilder("/bin/sh", "-c", command);
            }
        } catch (java.io.IOException ioException) {
            return failure(ioException);
        }
        try {
            Process process = builder.start();
            return LuaFileHandle.process(context().getFileShape(), process,
                    mode.equals("r") ? process.getInputStream() : null,
                    mode.equals("w") ? process.getOutputStream() : null);
        } catch (Exception exception) {
            return failure(exception);
        }
    }

    @LuaBuiltin(name = "lines")
    public static Object lines(Object[] arguments) {
        LuaFileHandle handle;
        boolean closeAtEnd;
        int formatStart;
        if (arguments.length == 0) {
            handle = requireDefaultInput();
            closeAtEnd = false;
            formatStart = 0;
        } else if (isNil(arguments[0])) {
            handle = requireDefaultInput();
            closeAtEnd = false;
            formatStart = 1;
        } else {
            String filename = string(arguments[0], 1, "lines");
            try {
                handle = LuaFileHandle.open(context().getFileShape(), context().resolveScriptPath(filename), "r");
            } catch (Exception exception) {
                // 标准 Lua 的 opencheck：打开失败时直接抛出错误（而非返回 nil）。
                throw LuaException.create("cannot open file '" + filename + "' (" + errorMessage(exception) + ")", null);
            }
            closeAtEnd = true;
            formatStart = 1;
        }
        return lineIterator(handle, arguments, formatStart, closeAtEnd);
    }

    @LuaBuiltin(name = "file_close")
    public static Object fileClose(Object[] arguments) {
        return closeHandle(self(arguments, "close"));
    }

    @LuaBuiltin(name = "file_flush")
    public static Object fileFlush(Object[] arguments) {
        try {
            self(arguments, "flush").flush();
            return true;
        } catch (Exception exception) {
            return failure(exception);
        }
    }

    @LuaBuiltin(name = "file_read")
    public static Object fileRead(Object[] arguments) {
        return readValues(self(arguments, "read"), arguments, 1, "read");
    }

    @LuaBuiltin(name = "file_write")
    public static Object fileWrite(Object[] arguments) {
        LuaFileHandle handle = self(arguments, "write");
        return writeValues(handle, arguments, 1, "write");
    }

    @LuaBuiltin(name = "file_seek")
    public static Object fileSeek(Object[] arguments) {
        LuaFileHandle handle = self(arguments, "seek");
        String whence = arguments.length > 1 && !isNil(arguments[1]) ? string(arguments[1], 2, "seek") : "cur";
        long offset = arguments.length > 2 && !isNil(arguments[2]) ? integer(arguments[2], 3, "seek") : 0;
        try {
            return handle.seek(whence, offset);
        } catch (IllegalArgumentException exception) {
            throw argumentError(2, "seek", LuaException.messageOf(exception));
        } catch (Exception exception) {
            return failure(exception);
        }
    }

    @LuaBuiltin(name = "file_setvbuf")
    public static Object fileSetvbuf(Object[] arguments) {
        LuaFileHandle handle = self(arguments, "setvbuf");
        if (arguments.length < 2) throw argumentError(2, "setvbuf", "string expected, got no value");
        String mode = string(arguments[1], 2, "setvbuf");
        if (!mode.equals("no") && !mode.equals("full") && !mode.equals("line"))
            throw argumentError(2, "setvbuf", "invalid option '" + mode + "'");
        int size = arguments.length > 2 && !isNil(arguments[2]) ? (int) integer(arguments[2], 3, "setvbuf") : 0;
        handle.setvbuf(mode, size);
        return true;
    }

    @LuaBuiltin(name = "file_lines")
    public static Object fileLines(Object[] arguments) {
        LuaFileHandle handle = self(arguments, "lines");
        return lineIterator(handle, arguments, 1, false);
    }

    @LuaBuiltin(name = "file_gc")
    public static Object fileGc(Object[] arguments) {
        if (arguments.length == 0) throw argumentError(1, "__gc", "FILE* expected, got no value");
        if (!(arguments[0] instanceof LuaFileHandle handle)) {
            throw argumentError(1, "__gc", "FILE* expected, got "
                    + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(arguments[0]));
        }
        try {
            handle.closeHandle();
        } catch (Exception ignored) {
            // __gc 忽略关闭过程中的错误（如已关闭的文件）
        }
        return LuaNil.SINGLETON;
    }

    private static LuaFunction lineIterator(LuaFileHandle handle, Object[] arguments, int formatStart,
                                            boolean closeAtEnd) {
        Object[] formats = new Object[arguments.length - formatStart];
        System.arraycopy(arguments, formatStart, formats, 0, formats.length);
        RootNode root = new LinesIteratorRootNode(context().getLanguage(), handle, formats, closeAtEnd);
        LuaFunction function = new LuaFunction(context().getFunctionShape(), root.getCallTarget());
        // 句柄挂在 LuaFunction 上强引用（迭代器的 CallTarget 会被调用点缓存，不能依赖其回收）。
        function.setRetainedResource(handle);
        return function;
    }

    @TruffleBoundary
    static Object readValues(LuaFileHandle handle, Object[] arguments, int start, String function) {
        try {
            if (start >= arguments.length) return readLine(handle, false);
            Object[] values = new Object[arguments.length - start];
            int count = 0;
            for (int i = start; i < arguments.length; i++) {
                Object value = readFormat(handle, arguments[i], i + 1, function);
                if (value == LuaNil.SINGLETON) {
                    if (count == 0) return LuaNil.SINGLETON;
                    break;
                }
                values[count++] = value;
            }
            if (count == 1) return values[0];
            Object[] result = new Object[count];
            System.arraycopy(values, 0, result, 0, count);
            return new LuaMultiValue(result);
        } catch (IOException exception) {
            return failure(exception);
        }
    }

    static Object writeValues(LuaFileHandle handle, Object[] arguments, int start, String function) {
        try {
            // 注意：标准 Lua 的 io.write 不自动 flush（依赖 setvbuf 缓冲与显式 flush/close）。
            for (int i = start; i < arguments.length; i++) handle.write(bytes(arguments[i], i + 1, function));
            return handle;
        } catch (IOException exception) {
            return failure(exception);
        }
    }

    @TruffleBoundary
    private static Object readFormat(LuaFileHandle handle, Object format, int argument, String function) throws IOException {
        if (format instanceof Long number) return readCount(handle, number, argument, function);
        if (format instanceof Double number && number == Math.rint(number)) return readCount(handle, (long) number.doubleValue(), argument, function);
        String option = string(format, argument, function);
        if (option.startsWith("*")) option = option.substring(1); // 兼容可选的 '*'
        if (option.isEmpty()) throw argumentError(argument, function, "invalid format");
        // 与 liolib.c 的 g_read 一致：只检查格式串的第一个字符。
        return switch (option.charAt(0)) {
            case 'a' -> readAll(handle);
            case 'l' -> readLine(handle, false);
            case 'L' -> readLine(handle, true);
            case 'n' -> readNumber(handle);
            default -> throw argumentError(argument, function, "invalid format");
        };
    }

    @TruffleBoundary
    private static Object readCount(LuaFileHandle handle, long count, int argument, String function) throws IOException {
        if (count < 0 || count > Integer.MAX_VALUE) throw argumentError(argument, function, "invalid format");
        if (count == 0) {
            int value = handle.read();
            handle.unread(value);
            return value < 0 ? LuaNil.SINGLETON : text("");
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream((int) count);
        for (long i = 0; i < count; i++) {
            int value = handle.read();
            if (value < 0) break;
            output.write(value);
        }
        return output.size() == 0 ? LuaNil.SINGLETON : LuaStringSupport.fromBytes(output.toByteArray());
    }

    @TruffleBoundary
    private static Object readAll(LuaFileHandle handle) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int value;
        while ((value = handle.read()) >= 0) output.write(value);
        return LuaStringSupport.fromBytes(output.toByteArray());
    }

    @TruffleBoundary
    private static Object readLine(LuaFileHandle handle, boolean keepNewline) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int value;
        while ((value = handle.read()) >= 0) {
            if (value == '\n') {
                if (keepNewline) output.write(value);
                break;
            }
            output.write(value);
        }
        if (value < 0 && output.size() == 0) return LuaNil.SINGLETON;
        byte[] bytes = output.toByteArray();
        if (!keepNewline && bytes.length > 0 && bytes[bytes.length - 1] == '\r') {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 0, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        return LuaStringSupport.fromBytes(bytes);
    }

    /**
     * 忠实移植 liolib.c 的 {@code read_number}：先按 Lua 词法读取一个数字的最长有效前缀，
     * 再用 {@link com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode#parseLuaNumber} 校验并转换。
     * 读取失败时仅回退一个前瞻字符，其余已消费字符按标准 Lua 语义丢弃。
     */
    private static final int MAXRN = 200;

    private static final class RN {
        LuaFileHandle f;
        int c;                       // 前瞻字符
        int n;                       // 缓冲区长度
        final byte[] buff = new byte[MAXRN + 1];
    }

    private static boolean nextc(RN rn) throws IOException {
        if (rn.n >= MAXRN) {         // 缓冲区溢出：使结果无效并停止消费
            rn.n = 0;
            return false;
        }
        rn.buff[rn.n++] = (byte) rn.c;
        rn.c = rn.f.read();
        return true;
    }

    private static boolean test2(RN rn, char first, char second) throws IOException {
        if (rn.c == first || (rn.c == second && rn.c != '\0')) return nextc(rn);
        return false;
    }

    private static int readDigits(RN rn, boolean hex) throws IOException {
        int count = 0;
        while ((hex ? isHexDigit(rn.c) : isDigit(rn.c)) && nextc(rn)) count++;
        return count;
    }

    @TruffleBoundary
    private static Object readNumber(LuaFileHandle handle) throws IOException {
        RN rn = new RN();
        rn.f = handle;
        int count = 0;
        boolean hex = false;
        do { rn.c = handle.read(); } while (isSpace(rn.c));
        test2(rn, '-', '+');                       // 可选符号
        if (test2(rn, '0', '\0')) {
            if (test2(rn, 'x', 'X')) hex = true;   // 十六进制
            else count = 1;                        // 起始的 '0' 计为一个有效数字
        }
        count += readDigits(rn, hex);              // 整数部分
        if (test2(rn, '.', '\0')) count += readDigits(rn, hex);  // 小数部分
        if (count > 0 && test2(rn, hex ? 'p' : 'e', hex ? 'P' : 'E')) {  // 指数标记
            test2(rn, '-', '+');
            readDigits(rn, false);
        }
        handle.unread(rn.c);
        if (rn.n == 0) return LuaNil.SINGLETON;
        Object number = com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode
                .parseLuaNumber(new String(rn.buff, 0, rn.n, StandardCharsets.ISO_8859_1));
        return number == null ? LuaNil.SINGLETON : number;
    }

    private static boolean isSpace(int value) {
        return value == ' ' || value == '\f' || value == '\n'
                || value == '\r' || value == '\t' || value == 0x0b;
    }

    private static boolean isDigit(int value) {
        return value >= '0' && value <= '9';
    }

    private static boolean isHexDigit(int value) {
        return isDigit(value) || (value >= 'a' && value <= 'f') || (value >= 'A' && value <= 'F');
    }

    private static Object closeHandle(LuaFileHandle handle) {
        try {
            return handle.closeHandle();
        } catch (Exception exception) {
            return failure(exception);
        }
    }

    private static LuaFileHandle fileOrOpen(Object value, String mode, String function) {
        if (value instanceof LuaFileHandle handle) return handle;
        if (!(value instanceof TruffleString) && !(value instanceof String)
                && !(value instanceof Long) && !(value instanceof Double)) {
            throw argumentError(1, function, "FILE* expected, got "
                    + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
        }
        String filename = string(value, 1, function);
        try {
            return LuaFileHandle.open(context().getFileShape(), context().resolveScriptPath(filename), mode);
        } catch (Exception exception) {
            throw LuaException.create(errorMessage(exception), null);
        }
    }

    private static LuaFileHandle self(Object[] arguments, String function) {
        if (arguments.length == 0) throw argumentError(1, function, "FILE* expected, got no value");
        return file(arguments[0], 1, function);
    }

    private static LuaFileHandle file(Object value, int argument, String function) {
        if (value instanceof LuaFileHandle handle) {
            // 对应 C 的 tofile：对已关闭的文件统一抛错。
            if (handle.isClosed()) throw LuaException.create("attempt to use a closed file", null);
            return handle;
        }
        throw argumentError(argument, function, "FILE* expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
    }

    /** 对应 C 的 getiofile(IO_INPUT)：默认输入已关闭时抛错。 */
    private static LuaFileHandle requireDefaultInput() {
        LuaFileHandle handle = context().getDefaultInput();
        if (handle.isClosed()) throw LuaException.create("standard input file is closed", null);
        return handle;
    }

    /** 对应 C 的 getiofile(IO_OUTPUT)：默认输出已关闭时抛错。 */
    private static LuaFileHandle requireDefaultOutput() {
        LuaFileHandle handle = context().getDefaultOutput();
        if (handle.isClosed()) throw LuaException.create("standard output file is closed", null);
        return handle;
    }

    private static byte[] bytes(Object value, int argument, String function) {
        if (value instanceof TruffleString string)
            return TruffleString.CopyToByteArrayNode.getUncached().execute(string, com.zhhz.truffle.lua.LuaLanguage.STRING_ENCODING);
        if (value instanceof Long number) return Long.toString(number).getBytes(StandardCharsets.US_ASCII);
        if (value instanceof Double number)
            return com.zhhz.truffle.lua.runtime.LuaNumberFormat
                    .formatG(number, com.zhhz.truffle.lua.runtime.LuaNumberFormat.TOSTRING_PRECISION, false, false)
                    .getBytes(StandardCharsets.US_ASCII);
        throw argumentError(argument, function, "string expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
    }

    private static String stringArgument(Object[] arguments, int index, String function) {
        if (index >= arguments.length) throw argumentError(index + 1, function, "string expected, got no value");
        return string(arguments[index], index + 1, function);
    }

    @TruffleBoundary
    private static String string(Object value, int argument, String function) {
        if (value instanceof TruffleString string) return string.toJavaStringUncached();
        if (value instanceof String string) return string;
        throw argumentError(argument, function, "string expected");
    }

    private static long integer(Object value, int argument, String function) {
        if (value instanceof Long number) return number;
        if (value instanceof Double number && number == Math.rint(number)) return (long) number.doubleValue();
        throw argumentError(argument, function, "integer expected");
    }

    private static LuaMultiValue failure(Exception exception) {
        return new LuaMultiValue(new Object[]{LuaNil.SINGLETON, text(errorMessage(exception)), 1L});
    }

    private static String errorMessage(Exception exception) {
        return LuaException.messageOf(exception) == null ? LuaException.simpleNameOf(exception) : LuaException.messageOf(exception);
    }

    private static RuntimeException argumentError(int argument, String function, String message) {
        return LuaException.create("bad argument #" + argument + " to '"
                + resolvedName(function) + "' (" + message + ")", null);
    }

    private static String resolvedName(String fallback) {
        String found = com.zhhz.truffle.lua.nodes.util.LuaTypesUtil
                .globalFuncName(com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.currentFunction());
        return found != null ? found : fallback;
    }

    private static boolean isNil(Object value) {
        return value == null || value == LuaNil.SINGLETON;
    }

    private static LuaContext context() {
        return LuaContext.get(null);
    }

    private static TruffleString text(String value) {
        return LuaContext.toTruffleString(value);
    }

    private static final class LinesIteratorRootNode extends RootNode {
        private final java.lang.ref.WeakReference<LuaFileHandle> handleRef;
        private final Object[] formats;
        private final boolean closeAtEnd;

        private LinesIteratorRootNode(com.zhhz.truffle.lua.LuaLanguage language, LuaFileHandle handle,
                                      Object[] formats, boolean closeAtEnd) {
            super(language);
            // 弱引用句柄：其生命周期由持有该迭代器函数的 LuaFunction 决定（见 lineIterator）。
            this.handleRef = new java.lang.ref.WeakReference<>(handle);
            this.formats = formats;
            this.closeAtEnd = closeAtEnd;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            LuaFileHandle handle = handleRef.get();
            if (handle == null || handle.isClosed()) {
                throw LuaException.create("file is already closed", null);
            }
            Object result = readValues(handle, formats, 0, "lines");
            if (LuaMultiValue.firstValue(result) == LuaNil.SINGLETON) {
                // 对应 liolib.c 的 io_readline：若返回值中带有错误信息（>1 个值），抛出该错误。
                if (result instanceof LuaMultiValue multiValue && multiValue.values().length > 1) {
                    throw LuaException.create(string(multiValue.values()[1], 1, "lines"), null);
                }
                if (closeAtEnd) closeHandle(handle);
            }
            return result;
        }
    }
}
