package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

@NodeInfo(shortName = ">")
public abstract class LuaGreaterThanNode extends LuaBinaryNode {

    @Specialization
    protected static boolean doLong(long left, long right) {
        return left > right;
    }

    @Specialization
    protected static boolean doDouble(double left, double right) {
        return left > right;
    }

    @Specialization
    protected static boolean doLongDouble(long left, double right) {
        return (double) left > right;
    }

    @Specialization
    protected static boolean doDoubleLong(double left, long right) {
        return left > (double) right;
    }

    @Specialization
    public static boolean greaterThanStrings(TruffleString left, TruffleString right,
                                              @Cached TruffleString.CompareCharsUTF16Node compareNode) {
        return compareNode.execute(left, right) > 0;
    }

    @Fallback
    public static boolean typeError(Object left, Object right,
                                   @Cached(value = "create()", neverDefault = true) LuaMetatableNode metatableNode,
                                   @Bind Node node) {
        Object result = metatableNode.execute(right, left, LuaMetatables.__LT);
        if (result != null) {
            return LuaTypesUtil.isTruthy(com.zhhz.truffle.lua.runtime.LuaMultiValue.firstValue(result));
        }
        throw LuaException.runtimeError(LuaTypesUtil.orderErrorMessage(right, left), node);
    }
}
