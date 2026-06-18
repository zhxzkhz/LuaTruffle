// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression.literals;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaTableFieldWithValueNode#writeObjectValue}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaTableFieldWithValueNode.class)
@SuppressWarnings("javadoc")
public final class LuaTableFieldWithValueNodeGen extends LuaTableFieldWithValueNode {

    private LuaTableFieldWithValueNodeGen(LuaExpressionNode luaExpressionNode, int arrayIndex) {
        super(luaExpressionNode, arrayIndex);
    }

    @Override
    protected void executeWithTable(VirtualFrame frameValue, LuaTable arg0Value, Object arg1Value) {
        writeObjectValue(arg0Value, arg1Value);
        return;
    }

    @NeverDefault
    public static LuaTableFieldWithValueNode create(LuaExpressionNode luaExpressionNode, int arrayIndex) {
        return new LuaTableFieldWithValueNodeGen(luaExpressionNode, arrayIndex);
    }

}
