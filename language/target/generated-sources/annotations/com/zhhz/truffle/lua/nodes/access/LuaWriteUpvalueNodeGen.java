// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaWriteUpvalueNode#doWrite}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaWriteUpvalueNode.class)
@SuppressWarnings("javadoc")
public final class LuaWriteUpvalueNodeGen extends LuaWriteUpvalueNode {

    private final int depth;
    private final int slotIndex;

    private LuaWriteUpvalueNodeGen(int depth, int slotIndex) {
        this.depth = depth;
        this.slotIndex = slotIndex;
    }

    @Override
    protected int getDepth() {
        return this.depth;
    }

    @Override
    protected int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    public void execute(VirtualFrame frameValue, Object arg0Value) {
        doWrite(frameValue, arg0Value);
        return;
    }

    @NeverDefault
    public static LuaWriteUpvalueNode create(int depth, int slotIndex) {
        return new LuaWriteUpvalueNodeGen(depth, slotIndex);
    }

}
