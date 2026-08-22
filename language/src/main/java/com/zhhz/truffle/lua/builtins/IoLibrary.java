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
        if (!mode.matches("[rwa](\\+b?|b\\+?)?")) throw argumentError(2, "open", "invalid mode");
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
        return readValues(context().getDefaultInput(), arguments, 0, "read");
    }

    @LuaBuiltin(name = "write")
    public static Object write(Object[] arguments) {
        return writeValues(context().getDefaultOutput(), arguments, 0, "write");
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
        ProcessBuilder builder = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
                ? new ProcessBuilder("cmd.exe", "/c", command) : new ProcessBuilder("/bin/sh", "-c", command);
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
        if (arguments.length == 0 || isNil(arguments[0])) {
            handle = context().getDefaultInput();
            closeAtEnd = false;
            formatStart = 0;
        } else {
            Object opened = open(new Object[]{arguments[0], text("r")});
            if (!(opened instanceof LuaFileHandle file)) return opened;
            handle = file;
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
            throw argumentError(2, "seek", exception.getMessage());
        } catch (Exception exception) {
            return failure(exception);
        }
    }

    @LuaBuiltin(name = "file_setvbuf")
    public static Object fileSetvbuf(Object[] arguments) {
        self(arguments, "setvbuf");
        if (arguments.length < 2) throw argumentError(2, "setvbuf", "string expected, got no value");
        String mode = string(arguments[1], 2, "setvbuf");
        if (!mode.equals("no") && !mode.equals("full") && !mode.equals("line"))
            throw argumentError(2, "setvbuf", "invalid option '" + mode + "'");
        return true;
    }

    @LuaBuiltin(name = "file_lines")
    public static Object fileLines(Object[] arguments) {
        LuaFileHandle handle = self(arguments, "lines");
        return lineIterator(handle, arguments, 1, false);
    }

    private static LuaFunction lineIterator(LuaFileHandle handle, Object[] arguments, int formatStart,
                                            boolean closeAtEnd) {
        Object[] formats = new Object[arguments.length - formatStart];
        System.arraycopy(arguments, formatStart, formats, 0, formats.length);
        RootNode root = new LinesIteratorRootNode(context().getLanguage(), handle, formats, closeAtEnd);
        return new LuaFunction(context().getFunctionShape(), root.getCallTarget());
    }

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
            for (int i = start; i < arguments.length; i++) handle.write(bytes(arguments[i], i + 1, function));
            handle.flush();
            return handle;
        } catch (IOException exception) {
            return failure(exception);
        }
    }

    private static Object readFormat(LuaFileHandle handle, Object format, int argument, String function) throws IOException {
        if (format instanceof Long number) return readCount(handle, number, argument, function);
        if (format instanceof Double number && number == Math.rint(number)) return readCount(handle, (long) number.doubleValue(), argument, function);
        String option = string(format, argument, function);
        if (option.startsWith("*")) option = option.substring(1);
        return switch (option) {
            case "a" -> readAll(handle);
            case "l" -> readLine(handle, false);
            case "L" -> readLine(handle, true);
            case "n" -> readNumber(handle);
            default -> throw argumentError(argument, function, "invalid format");
        };
    }

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

    private static Object readAll(LuaFileHandle handle) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int value;
        while ((value = handle.read()) >= 0) output.write(value);
        return LuaStringSupport.fromBytes(output.toByteArray());
    }

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

    private static Object readNumber(LuaFileHandle handle) throws IOException {
        StringBuilder number = new StringBuilder();
        int value;
        do value = handle.read(); while (value >= 0 && Character.isWhitespace(value));
        while (value >= 0 && (Character.isDigit(value) || "+-.eExXpPabcdefABCDEF".indexOf(value) >= 0)) {
            number.append((char) value);
            value = handle.read();
        }
        handle.unread(value);
        if (number.isEmpty()) return LuaNil.SINGLETON;
        try {
            String text = number.toString();
            return text.matches("[+-]?\\d+") ? Long.parseLong(text) : Double.parseDouble(text);
        } catch (NumberFormatException exception) {
            return LuaNil.SINGLETON;
        }
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
        if (value instanceof LuaFileHandle handle) return handle;
        throw argumentError(argument, function, "FILE* expected");
    }

    private static byte[] bytes(Object value, int argument, String function) {
        if (value instanceof TruffleString string)
            return TruffleString.CopyToByteArrayNode.getUncached().execute(string, com.zhhz.truffle.lua.LuaLanguage.STRING_ENCODING);
        if (value instanceof Long number) return Long.toString(number).getBytes(StandardCharsets.US_ASCII);
        if (value instanceof Double number) return Double.toString(number).getBytes(StandardCharsets.US_ASCII);
        throw argumentError(argument, function, "string or number expected");
    }

    private static String stringArgument(Object[] arguments, int index, String function) {
        if (index >= arguments.length) throw argumentError(index + 1, function, "string expected, got no value");
        return string(arguments[index], index + 1, function);
    }

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
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    private static RuntimeException argumentError(int argument, String function, String message) {
        return LuaException.create("bad argument #" + argument + " to '" + function + "' (" + message + ")", null);
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
        private final LuaFileHandle handle;
        private final Object[] formats;
        private final boolean closeAtEnd;
        private boolean done;

        private LinesIteratorRootNode(com.zhhz.truffle.lua.LuaLanguage language, LuaFileHandle handle,
                                      Object[] formats, boolean closeAtEnd) {
            super(language);
            this.handle = handle;
            this.formats = formats;
            this.closeAtEnd = closeAtEnd;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            if (done) return LuaNil.SINGLETON;
            Object result = readValues(handle, formats, 0, "lines");
            if (LuaMultiValue.firstValue(result) == LuaNil.SINGLETON) {
                done = true;
                if (closeAtEnd) closeHandle(handle);
            }
            return result;
        }
    }
}
