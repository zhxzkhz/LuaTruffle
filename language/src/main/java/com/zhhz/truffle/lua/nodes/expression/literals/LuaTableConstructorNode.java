package com.zhhz.truffle.lua.nodes.expression.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 代表 table 构造器 `{...}` 的表达式节点。
 */
@NodeInfo(shortName = "table", description = "The node implementing table constructors")
public final class LuaTableConstructorNode extends LuaExpressionNode{

    @Children
    private final LuaTableFieldInitializerNode[] fieldInitializers;

    public LuaTableConstructorNode(LuaTableFieldInitializerNode[] fieldInitializers) {
        this.fieldInitializers = fieldInitializers;
    }

    @Override
    @ExplodeLoop // 展开循环以获得更好的性能
    public Object executeGeneric(VirtualFrame frame) {
        // 1. 【创建空 Table】
        //    从上下文中获取初始的 table shape
        LuaTable newTable = new LuaTable(LuaContext.get(this).getTableShape());

        // 2. 【执行所有初始化器】
        for (LuaTableFieldInitializerNode initializer : fieldInitializers) {
            initializer.executeVoid(frame, newTable);
        }

        // 3. 【返回填充好的 Table】
        return newTable;
    }
}