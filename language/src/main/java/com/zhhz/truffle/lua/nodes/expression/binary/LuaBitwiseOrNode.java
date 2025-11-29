package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName;

/**
 * 实现了Lua的按位或运算符 (|)。
 */
@NodeInfo(shortName = "|")
public abstract class LuaBitwiseOrNode extends LuaBinaryNode {

    // --- 特化 1: 快速路径，两个操作数都已经是数字 ---
    @Specialization
    protected long doLong(long left, long right) {
        return left | right;
    }

    @Fallback
    @CompilerDirectives.TruffleBoundary
    protected Object doGeneric(Object left, Object right,
                               @Cached("create()") LuaCoerceToIntegerNode coerceLeft,
                               @Cached("create()") LuaCoerceToIntegerNode coerceRight,
                               @Cached("create()") LuaMetatableNode metatableNode) {

        boolean leftIsNum = isNumberOrString(left);
        boolean rightIsNum = isNumberOrString(right);

        if (leftIsNum && rightIsNum) {
            Long lLeft = coerceLeft.execute(left);
            Long lRight = coerceRight.execute(right);

            if (lLeft != null && lRight != null) {
                return lLeft | lRight; // 转换成功，计算
            } else {
                // 转换失败（例如 1.5 & 1），直接报错！
                throw LuaException.create("number has no integer representation",this);
            }
        }

        // 3. 只有当【至少有一个】操作数不是数字/字符串时，才查找元方法
        Object metaResult = metatableNode.execute(left, right, LuaMetatables.__BOR);
        if (metaResult != null) {
            return metaResult;
        }

        // 4. 元方法也没找到，抛出类型错误
        throw LuaException.create("attempt to perform bitwise operation on a " + getTypeName(left) + " value",this);

    }


}
