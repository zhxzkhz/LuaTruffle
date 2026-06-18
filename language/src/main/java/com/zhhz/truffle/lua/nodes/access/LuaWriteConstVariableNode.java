package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.LuaException;

public final class LuaWriteConstVariableNode extends LuaWriteVariableNode {

    @Child private LuaWriteVariableNode delegate;

    public LuaWriteConstVariableNode(LuaWriteVariableNode delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(VirtualFrame frame, Object value) {
        throw LuaException.create("attempt to assign to const variable", this);
    }
}
