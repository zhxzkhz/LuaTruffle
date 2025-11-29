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

    @Specialization(rewriteOn = ArithmeticException.class)
    public static double doLong(long left, long right) throws ArithmeticException {
        return Math.pow(left,right);
    }


    /**
     * Fallback 用于处理字符串转换和元方法。
     */
    @Fallback
    protected Object doGeneric(Object left, Object right,
                               @Cached("create()") LuaCoerceToNumberNode coerceLeft,
                               @Cached("create()") LuaCoerceToNumberNode coerceRight,
                               @Cached("create()") LuaMetatableNode metatableNode) {

        assertNotNull(left,right,this);

        Object numLeft = coerceLeft.execute(left);
        Object numRight = coerceRight.execute(right);

        if (numLeft != null && numRight != null) {
            double dLeft = (numLeft instanceof Double) ? (Double) numLeft : ((Long) numLeft).doubleValue();
            double dRight = (numRight instanceof Double) ? (Double) numRight : ((Long) numRight).doubleValue();
            return Math.pow(dLeft,dRight);
        }

        // 2. 尝试元方法 (逻辑不变)
        Object result = metatableNode.execute(left, right, LuaMetatables.__POW);
        if (result != null) {
            return result;
        }
        throw LuaException.typeError(this, "^", left, right);
    }

}
