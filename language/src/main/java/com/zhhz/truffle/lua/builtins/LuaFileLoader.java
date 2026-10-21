package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

final class LuaFileLoader {

    private LuaFileLoader() {
    }

    @TruffleBoundary
    static LuaFunction load(LuaContext context, Object[] arguments, String function) throws IOException {
        String filename = filename(arguments, function);
        String mode = mode(arguments, function);

        TruffleFile file = context.resolveScriptFile(filename);
        byte[] bytes = readAllBytes(file);

        // 复刻 luaL_loadfilex：先跳过 BOM 与以 '#' 开头的首行（exec 文件/前置注释），
        // 再据首字节判断是二进制还是文本块。
        int offset = 0;
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            offset = 3;
        }
        if (offset < bytes.length && bytes[offset] == '#') {
            while (offset < bytes.length && bytes[offset] != '\n') {
                offset++;
            }
            if (offset < bytes.length) {
                offset++;
            }
        }
        boolean binary = isBinary(bytes, offset);
        checkMode(mode, binary);
        if (binary) {
            byte[] chunk = java.util.Arrays.copyOfRange(bytes, offset, bytes.length);
            com.zhhz.truffle.lua.bytecode.Proto proto =
                    com.zhhz.truffle.lua.bytecode.LuaUndumper.undump(chunk, "@" + file.getPath());
            Object environment = arguments.length > 2 && arguments[2] != LuaNil.SINGLETON ? arguments[2] : null;
            return com.zhhz.truffle.lua.vm.LuaVm.newClosure(context, proto, environment);
        }

        Source source = Source.newBuilder(LuaLanguage.ID, file)
                .name("@" + file.getPath())
                .build();
        LuaFunction functionObject = new LuaFunction(context.getFunctionShape(),
                context.getEnv().parseInternal(source));
        // loadfile(filename, mode, env)：显式给出的环境（含显式 nil）作为 chunk 的 _ENV 初值。
        if (arguments.length > 2) {
            functionObject.setEnvironment(arguments[2]);
        }
        return functionObject;
    }

    static String errorMessage(Throwable exception) {
        if (exception instanceof NoSuchFileException noSuchFile) {
            return "cannot open " + noSuchFile.getFile() + ": No such file or directory";
        }
        if (exception instanceof AccessDeniedException accessDenied) {
            return "cannot open " + accessDenied.getFile() + ": Permission denied";
        }
        String message = LuaException.messageOf(exception);
        return message == null || message.isBlank() ? LuaException.simpleNameOf(exception) : message;
    }

    private static String filename(Object[] arguments, String function) {
        if (arguments.length == 0 || arguments[0] == null || arguments[0] == LuaNil.SINGLETON) {
            throw LuaException.create(function + " from standard input is not supported", null);
        }
        if (arguments[0] instanceof TruffleString string) {
            return string.toJavaStringUncached();
        }
        if (arguments[0] instanceof String string) {
            return string;
        }
        throw LuaException.create("bad argument #1 to '" + function + "' (string expected)", null);
    }

    private static String mode(Object[] arguments, String function) {
        if (arguments.length < 2 || arguments[1] == null || arguments[1] == LuaNil.SINGLETON) {
            return null;
        }
        if (arguments[1] instanceof TruffleString string) {
            return string.toJavaStringUncached();
        }
        if (arguments[1] instanceof String string) {
            return string;
        }
        throw LuaException.create("bad argument #2 to '" + function + "' (string expected)", null);
    }

    /**
     * 对应 lua/src/ldo.c 的 {@code checkmode}：mode 中缺失对应字符时报错。
     */
    private static void checkMode(String mode, boolean binary) {
        if (mode == null) {
            return;
        }
        if (binary && mode.indexOf('b') < 0) {
            throw LuaException.create("attempt to load a binary chunk (mode is '" + mode + "')", null);
        }
        if (!binary && mode.indexOf('t') < 0) {
            throw LuaException.create("attempt to load a text chunk (mode is '" + mode + "')", null);
        }
    }

    private static byte[] readAllBytes(TruffleFile file) throws IOException {
        try (InputStream input = file.newInputStream()) {
            return input.readAllBytes();
        }
    }

    /** 复刻 luaL_loadfilex / f_parser：首字节为 ESC ({@code LUA_SIGNATURE[0]}) 即视为二进制块。 */
    private static boolean isBinary(byte[] bytes, int offset) {
        return offset < bytes.length && bytes[offset] == 0x1b;
    }
}
