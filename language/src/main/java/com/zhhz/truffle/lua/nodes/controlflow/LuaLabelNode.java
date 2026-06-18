package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

@NodeInfo(shortName = "label")
public final class LuaLabelNode extends LuaStatementNode {

    private final TruffleString label;

    public LuaLabelNode(TruffleString label) {
        this.label = label;
    }

    public TruffleString getLabel() {
        return label;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
    }
}
