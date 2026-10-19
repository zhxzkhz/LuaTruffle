package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

/**
 * 实现了Lua的乘方运算符 (^)。
 * 遵循Lua的 double 算术和字符串自动转换语义。
 */
@NodeInfo(shortName = "^")
public abstract class LuaPowerNode extends LuaBinaryNode {


    @Specialization
    protected static double doDouble(double left, double right) {
        return Math.pow(left,right);
    }

    @Specialization
    public static double doLong(long left, long right) {
        return Math.pow(left, right);
    }


    /**
     * Fallback 用于处理字符串转换和元方法。
     */
    @Fallback
    protected Object doGeneric(Object left, Object right,
                               @Cached("create()") LuaCoerceToNumberNode coerceLeft,
                               @Cached("create()") LuaCoerceToNumberNode coerceRight,
                               @Cached("create()") LuaMetatableNode metatableNode) {


        Object numLeft = coerceLeft.executeArithmetic(left);
        Object numRight = coerceRight.executeArithmetic(right);

        if (numLeft != null && numRight != null) {
            double dLeft = (numLeft instanceof Double) ? (Double) numLeft : ((Long) numLeft).doubleValue();
            double dRight = (numRight instanceof Double) ? (Double) numRight : ((Long) numRight).doubleValue();
            return Math.pow(dLeft,dRight);
        }

        // 2. 尝试元方法 (逻辑不变)
        Object result = metatableNode.execute(left, right, LuaMetatables.__POW);
        if (result != null) {
            return com.zhhz.truffle.lua.runtime.LuaMultiValue.firstValue(result);
        }
        throw arithmeticError(left, right, numLeft, numRight);
    }

}
