// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaReadUpvalueNode#readUpvalue}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaReadUpvalueNode.class)
@SuppressWarnings("javadoc")
public final class LuaReadUpvalueNodeGen extends LuaReadUpvalueNode {

    private final int slotIndex;
    private final int depth;

    private LuaReadUpvalueNodeGen(int slotIndex, int depth) {
        this.slotIndex = slotIndex;
        this.depth = depth;
    }

    @Override
    protected int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    protected int getDepth() {
        return this.depth;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        return readUpvalue(frameValue);
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeGeneric(frameValue);
        return;
    }

    @NeverDefault
    public static LuaReadUpvalueNode create(int slotIndex, int depth) {
        return new LuaReadUpvalueNodeGen(slotIndex, depth);
    }

}
