package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaFunction;

@NodeInfo(shortName = "dofile")
public abstract class LuaDoFileBuiltinNode extends LuaBuiltinNode {

    @Specialization(limit = "3")
    @TruffleBoundary
    protected Object doFile(Object[] arguments,
                            @CachedLibrary(limit = "3") InteropLibrary interop) {
        try {
            LuaFunction function = LuaFileLoader.load(getContext(), arguments, "dofile");
            return interop.execute(function);
        } catch (LuaException exception) {
            throw exception;
        } catch (Exception exception) {
            throw LuaException.create(LuaFileLoader.errorMessage(exception), this);
        }
    }
}
