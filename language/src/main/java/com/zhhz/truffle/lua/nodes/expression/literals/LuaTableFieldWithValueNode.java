package com.zhhz.truffle.lua.nodes.expression.literals;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 实现形如 `[keyExpression] = valueExpression` 的字段初始化。
 * 也可用于 `name = valueExpression` (此时 keyExpression 是一个字符串字面量)。
 */

public abstract class LuaTableFieldWithValueNode extends LuaTableFieldInitializerNode {

    private final int arrayIndex;

    @Child private LuaExpressionNode luaExpressionNode;

    public LuaTableFieldWithValueNode(LuaExpressionNode luaExpressionNode,int arrayIndex) {
        this.luaExpressionNode = luaExpressionNode;
        this.arrayIndex = arrayIndex;
    }

    @Override
    public final void executeVoid(VirtualFrame frame, LuaTable table) {
        var a = luaExpressionNode.executeGeneric(frame);
        executeWithTable(frame,table,a);
    }

    protected abstract void executeWithTable(VirtualFrame frame,LuaTable table,Object valueNode);


    @Specialization()
    protected void writeObjectValue(LuaTable table,Object valueNode) {
        table.arrayRawSet(this.arrayIndex, valueNode);
    }

}
