// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaWriteTableNode#empty}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaWriteTableNode.class)
@SuppressWarnings("javadoc")
public final class LuaWriteTableNodeGen extends LuaWriteTableNode {

    @Child private LuaExpressionNode tableNode_;
    @Child private LuaExpressionNode keyNode_;

    private LuaWriteTableNodeGen(LuaExpressionNode tableNode, LuaExpressionNode keyNode) {
        this.tableNode_ = tableNode;
        this.keyNode_ = keyNode;
    }

    @Override
    LuaExpressionNode getTableNode() {
        return this.tableNode_;
    }

    @Override
    LuaExpressionNode getKeyNode() {
        return this.keyNode_;
    }

    @Override
    public void executeEmpty(VirtualFrame frameValue, Object tableNodeValue) {
        Object keyNodeValue_ = this.keyNode_.executeGeneric(frameValue);
        LuaWriteTableNode.empty(frameValue, tableNodeValue, keyNodeValue_);
        return;
    }

    @NeverDefault
    public static LuaWriteTableNode create(LuaExpressionNode tableNode, LuaExpressionNode keyNode) {
        return new LuaWriteTableNodeGen(tableNode, keyNode);
    }

}
