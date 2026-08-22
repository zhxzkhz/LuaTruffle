package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;

@NodeInfo(shortName = "xpcall")
public abstract class LuaXpcallBuiltinNode extends LuaBuiltinNode {

    @Specialization(limit = "3")
    @TruffleBoundary
    protected Object xpcall(Object[] arguments,
                            @CachedLibrary(limit = "3") InteropLibrary interop) {
        if (arguments.length < 2) {
            throw LuaException.create("bad argument #2 to 'xpcall' (function expected, got no value)", this);
        }
        Object[] callArguments = new Object[arguments.length - 2];
        System.arraycopy(arguments, 2, callArguments, 0, callArguments.length);
        try {
            return LuaPcallBuiltinNode.success(interop.execute(arguments[0], callArguments));
        } catch (Exception exception) {
            try {
                Object handled = interop.execute(arguments[1], LuaPcallBuiltinNode.errorObject(exception));
                return new LuaMultiValue(new Object[]{false, LuaMultiValue.firstValue(handled)});
            } catch (Exception handlerError) {
                throw LuaException.create("error in error handling", this);
            }
        }
    }
}
