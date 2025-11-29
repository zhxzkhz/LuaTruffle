package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaContext;

public final class LuaBooleanLiteralNode extends LuaExpressionNode {

    private final boolean value;

    public LuaBooleanLiteralNode(boolean value) {
        this.value = value;
    }

    @Override
    public boolean executeBoolean(VirtualFrame frame) {
        return value;
    }

    @Override
    public LuaBoolean executeGeneric(VirtualFrame frame) {
        return LuaBoolean.get(LuaContext.get(this), this.value);
    }
}
