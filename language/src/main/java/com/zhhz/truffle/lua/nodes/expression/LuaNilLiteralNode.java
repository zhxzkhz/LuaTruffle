package com.zhhz.truffle.lua.nodes.expression;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

public final class LuaNilLiteralNode extends LuaExpressionNode {

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return LuaNil.SINGLETON;
    }
}
