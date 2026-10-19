package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.expression.LuaStringLiteralNode;

/**
 * 将值写入一个全局变量。
 *
 * <p>Lua 5.3 中，写全局 {@code x = v} 等价于 {@code _ENV.x = v}，因此本节点内部持有
 * 一个 {@link LuaWriteTableNode}，其表子节点求值为当前环境（{@code _ENV}），键为变量名。
 * 这保证了 {@code __newindex} 元方法语义。保留独立的节点类型是为了让 {@code debug.getinfo}
 * 仍能把该访问识别为 {@code global}。
 */
@NodeInfo(shortName = "write_global")
public final class LuaWriteGlobalVariableNode extends LuaWriteVariableNode {

    private final TruffleString name;

    @Child
    private LuaWriteTableNode tableWriteNode;

    public LuaWriteGlobalVariableNode(LuaExpressionNode envNode, TruffleString name) {
        this.name = name;
        this.tableWriteNode = LuaWriteTableNodeGen.create(envNode, new LuaStringLiteralNode(name));
    }

    @Override
    public void execute(VirtualFrame frame, Object value) {
        tableWriteNode.execute(frame, value);
    }

    @Override
    public Object prepareTarget(VirtualFrame frame) {
        return tableWriteNode.prepareTarget(frame);
    }

    @Override
    public void writePrepared(VirtualFrame frame, Object preparedTarget, Object value) {
        tableWriteNode.writePrepared(frame, preparedTarget, value);
    }
}
