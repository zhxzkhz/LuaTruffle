package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.util.WriteHelperNode;
import com.zhhz.truffle.lua.nodes.util.WriteHelperNodeGen;

/**
 * 实现 table[key] = value 赋值操作的节点。
 */

@NodeInfo(shortName = "write_table")
@NodeChild(value = "tableNode", type = LuaExpressionNode.class)
@NodeChild(value = "keyNode", type = LuaExpressionNode.class)
public abstract class LuaWriteTableNode extends LuaWriteVariableNode {

    private record PreparedTarget(Object table, Object key) {}

    abstract LuaExpressionNode getTableNode();
    abstract LuaExpressionNode getKeyNode();

    // 【新增】一个内部的、专门负责写入的辅助节点
    @Child private WriteHelperNode writeHelperNode;

    // 我们不能再让这个类是 abstract 的了
    public LuaWriteTableNode() {
        // Truffle DSL 会在生成的 ...Gen 类中处理子节点的赋值
        this.writeHelperNode = WriteHelperNodeGen.create();
    }

    // Keeps the Truffle DSL factory generation without participating in table writes.
    @Specialization
    static void generateFactory(VirtualFrame frame, Object ignored) {
    }

    abstract void executeFactory(VirtualFrame frame, Object ignored);

    @Override
    public final void execute(VirtualFrame frame, Object value) {
        Object table = getTableNode().executeGeneric(frame);
        Object key = getKeyNode().executeGeneric(frame);
        try {
            writeHelperNode.executeWrite(table, key, value);
        } catch (com.oracle.truffle.api.exception.AbstractTruffleException e) {
            throw withIndexSuffix(e);
        }
    }

    @Override
    public Object prepareTarget(VirtualFrame frame) {
        return new PreparedTarget(getTableNode().executeGeneric(frame), getKeyNode().executeGeneric(frame));
    }

    @Override
    public void writePrepared(VirtualFrame frame, Object preparedTarget, Object value) {
        PreparedTarget target = (PreparedTarget) preparedTarget;
        try {
            writeHelperNode.executeWrite(target.table(), target.key(), value);
        } catch (com.oracle.truffle.api.exception.AbstractTruffleException e) {
            throw withIndexSuffix(e);
        }
    }

    /**
     * 写入路径的 "attempt to index a ... value" 错误需要附带上表表达式的变量来源后缀
     * （如 {@code " (upvalue 'a')"}），与标准 Lua 的 varinfo 一致。
     */
    private com.oracle.truffle.api.exception.AbstractTruffleException withIndexSuffix(
            com.oracle.truffle.api.exception.AbstractTruffleException e) {
        String message = e.getMessage();
        if (message != null && message.startsWith("attempt to index a ")) {
            return com.zhhz.truffle.lua.LuaException.runtimeError(
                    message + getTableNode().getErrorSuffix(), this);
        }
        return e;
    }


}
