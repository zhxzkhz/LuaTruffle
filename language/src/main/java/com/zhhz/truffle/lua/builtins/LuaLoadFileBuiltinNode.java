package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;
@GenerateInline(false)

@NodeInfo(shortName = "loadfile")
public abstract class LuaLoadFileBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    protected Object loadFile(Object[] arguments) {
        try {
            return LuaFileLoader.load(getContext(), arguments, "loadfile");
        } catch (Exception exception) {
            return new LuaMultiValue(new Object[]{LuaNil.SINGLETON,
                    toTruffleString(LuaFileLoader.errorMessage(exception))});
        }
    }
}
