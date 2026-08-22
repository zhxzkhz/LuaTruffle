package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

@NodeInfo(shortName = "pcall")
public abstract class LuaPcallBuiltinNode extends LuaBuiltinNode {

    @Specialization(limit = "3")
    @TruffleBoundary
    protected Object pcall(Object[] arguments,
                           @CachedLibrary(limit = "3") InteropLibrary interop) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'pcall' (value expected)", this);
        }
        Object[] callArguments = new Object[arguments.length - 1];
        System.arraycopy(arguments, 1, callArguments, 0, callArguments.length);
        try {
            return success(interop.execute(arguments[0], callArguments));
        } catch (Exception exception) {
            return new LuaMultiValue(new Object[]{false, errorObject(exception)});
        }
    }

    static Object success(Object result) {
        if (result instanceof LuaMultiValue values) {
            Object[] returned = values.values();
            Object[] protectedValues = new Object[returned.length + 1];
            protectedValues[0] = true;
            System.arraycopy(returned, 0, protectedValues, 1, returned.length);
            return new LuaMultiValue(protectedValues);
        }
        return new LuaMultiValue(new Object[]{true, result == null ? LuaNil.SINGLETON : result});
    }

    static Object errorObject(Exception exception) {
        if (exception instanceof LuaException luaException) {
            return luaException.getErrorObject();
        }
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
