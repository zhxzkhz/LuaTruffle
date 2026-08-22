package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;

import java.io.PrintWriter;

@NodeInfo(shortName = "io.stderr:write")
public abstract class LuaStderrWriteBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    protected Object write(Object[] arguments) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaTable handle)) {
            throw LuaException.create("bad self (FILE expected)", this);
        }

        PrintWriter error = getContext().getError();
        for (int i = 1; i < arguments.length; i++) {
            error.print(toText(arguments[i], i));
        }
        error.flush();
        return handle;
    }

    private String toText(Object value, int argumentIndex) {
        if (value instanceof TruffleString string) {
            return string.toJavaStringUncached();
        }
        if (value instanceof LuaString string) {
            return string.getValue().toJavaStringUncached();
        }
        if (value instanceof Number) {
            return value.toString();
        }
        throw LuaException.create("bad argument #" + argumentIndex
                + " to 'write' (string or number expected)", this);
    }
}
