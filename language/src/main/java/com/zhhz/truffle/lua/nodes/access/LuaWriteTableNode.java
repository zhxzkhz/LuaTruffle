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

    abstract LuaExpressionNode getTableNode();
    abstract LuaExpressionNode getKeyNode();

    // 【新增】一个内部的、专门负责写入的辅助节点
    @Child private WriteHelperNode writeHelperNode;

    // 我们不能再让这个类是 abstract 的了
    public LuaWriteTableNode() {
        // Truffle DSL 会在生成的 ...Gen 类中处理子节点的赋值
        this.writeHelperNode = WriteHelperNodeGen.create();
    }

    //无用方法，为了统一生成Gen文件保留
    @Specialization
    public static void empty(VirtualFrame frame, Object value, Object value1){

    }

    //无用方法，为了统一生成Gen文件保留
    public abstract void executeEmpty(VirtualFrame frame, Object value);

    @Override
    public final void execute(VirtualFrame frame, Object value) {
        // 1. 手动执行子节点，计算出 table 和 key
        Object table = getTableNode().executeGeneric(frame);
        Object key = getKeyNode().executeGeneric(frame);

        // 2. 将【所有】需要的值（table, key, value）传递给辅助节点
        writeHelperNode.executeWrite(table, key, value);
    }


}