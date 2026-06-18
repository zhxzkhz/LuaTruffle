package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

@NodeInfo(shortName = "goto")
public final class LuaGotoNode extends LuaStatementNode {

    private final TruffleString label;
    private int[] targetBlockPath;
    @Children private LuaCloseSlotNode[] closeOnJump;

    public LuaGotoNode(TruffleString label, int[] targetBlockPath) {
        this.label = label;
        this.targetBlockPath = targetBlockPath;
        this.closeOnJump = new LuaCloseSlotNode[0];
    }

    public TruffleString getLabel() {
        return label;
    }

    public void setTargetBlockPath(int[] targetBlockPath) {
        this.targetBlockPath = targetBlockPath;
    }

    public void setCloseOnJump(LuaCloseSlotNode[] closeOnJump) {
        this.closeOnJump = closeOnJump;
    }

    @Override
    public void executeVoid(com.oracle.truffle.api.frame.VirtualFrame frame) {
        for (int i = closeOnJump.length - 1; i >= 0; i--) {
            closeOnJump[i].executeClose(frame, LuaNil.SINGLETON);
        }
        throw new LuaGotoException(label, targetBlockPath);
    }
}
