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
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

final class LuaFileLoader {

    private LuaFileLoader() {
    }

    @TruffleBoundary
    static LuaFunction load(LuaContext context, Object[] arguments, String function) throws IOException {
        String filename = filename(arguments, function);
        validateMode(arguments, function);
        if (arguments.length > 2 && arguments[2] != null && arguments[2] != LuaNil.SINGLETON) {
            throw LuaException.create(function + " does not support a custom environment", null);
        }

        TruffleFile file = context.resolveScriptFile(filename);
        Source source = Source.newBuilder(LuaLanguage.ID, file)
                .name("@" + file.getPath())
                .build();
        return new LuaFunction(context.getFunctionShape(), context.getEnv().parseInternal(source));
    }

    static String errorMessage(Throwable exception) {
        if (exception instanceof NoSuchFileException noSuchFile) {
            return "cannot open " + noSuchFile.getFile() + ": No such file or directory";
        }
        if (exception instanceof AccessDeniedException accessDenied) {
            return "cannot open " + accessDenied.getFile() + ": Permission denied";
        }
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
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

    private static void validateMode(Object[] arguments, String function) {
        if (arguments.length < 2 || arguments[1] == null || arguments[1] == LuaNil.SINGLETON) {
            return;
        }
        String mode;
        if (arguments[1] instanceof TruffleString string) {
            mode = string.toJavaStringUncached();
        } else if (arguments[1] instanceof String string) {
            mode = string;
        } else {
            throw LuaException.create("bad argument #2 to '" + function + "' (string expected)", null);
        }
        if (!mode.equals("t") && !mode.equals("bt")) {
            throw LuaException.create("attempt to load a text chunk (mode is '" + mode + "')", null);
        }
    }
}
