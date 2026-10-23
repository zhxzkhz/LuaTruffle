package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.nodes.util.LuaToTruffleStringNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

/**
 * 实现了Lua的字符串连接运算符 (..)。
 *
 * <p>这个节点实现了Lua的自动类型转换（数字 -> 字符串）。
 */
@NodeInfo(shortName = "..")
public abstract class LuaConcatNode extends LuaBinaryNode {

    @Specialization
    protected static TruffleString doStrings(TruffleString left, TruffleString right,
                                              @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(left, right, LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doStringLong(TruffleString left, long right,
                                                  @Bind Node node,
                                                  @Shared("toStr") @Cached LuaToTruffleStringNode toStr,
                                                  @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(left, toStr.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doLongString(long left, TruffleString right,
                                                 @Bind Node node,
                                                 @Shared("toStr") @Cached LuaToTruffleStringNode toStr,
                                                 @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStr.execute(node, left), right, LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doStringDouble(TruffleString left, double right,
                                                    @Bind Node node,
                                                    @Shared("toStr") @Cached LuaToTruffleStringNode toStr,
                                                    @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(left, toStr.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doDoubleString(double left, TruffleString right,
                                                   @Bind Node node,
                                                   @Shared("toStr") @Cached LuaToTruffleStringNode toStr,
                                                   @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStr.execute(node, left), right, LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doLongLong(long left, long right,
                                               @Bind Node node,
                                               @Shared("toStr") @Cached LuaToTruffleStringNode toStrLeft,
                                               @Shared("toStr") @Cached LuaToTruffleStringNode toStrRight,
                                               @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doDoubleLong(double left, long right,
                                                 @Bind Node node,
                                                 @Shared("toStr") @Cached LuaToTruffleStringNode toStrLeft,
                                                 @Shared("toStr") @Cached LuaToTruffleStringNode toStrRight,
                                                 @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doLongDouble(long left, double right,
                                                 @Bind Node node,
                                                 @Shared("toStr") @Cached LuaToTruffleStringNode toStrLeft,
                                                 @Shared("toStr") @Cached LuaToTruffleStringNode toStrRight,
                                                 @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doDoubleDouble(double left, double right,
                                                   @Bind Node node,
                                                   @Shared("toStr") @Cached LuaToTruffleStringNode toStrLeft,
                                                   @Shared("toStr") @Cached LuaToTruffleStringNode toStrRight,
                                                   @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Fallback
    @TruffleBoundary
    protected Object doGeneric(Object left, Object right,
                                      @Shared("toStr") @Cached LuaToTruffleStringNode toStrLeft,
                                      @Shared("toStr") @Cached LuaToTruffleStringNode toStrRight,
                                      @Cached(value = "create()", neverDefault = true) LuaMetatableNode metatableNode,
                                      @Shared("concat") @Cached TruffleString.ConcatNode concatNode) {

        Object result = metatableNode.execute(left, right, LuaMetatables.__CONCAT);
        if (result != null) {
            return com.zhhz.truffle.lua.runtime.LuaMultiValue.firstValue(result);
        }

        if (!isStringOrNumber(left) || !isStringOrNumber(right)) {
            Object bad = !isStringOrNumber(left) ? left : right;
            String suffix = operandSuffix(!isStringOrNumber(left) ? getLeftNode() : getRightNode());
            throw LuaException.runtimeError("attempt to concatenate a "
                    + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getRuntimeTypeName(bad) + " value" + suffix, this);
        }

        return concatNode.execute(toStrLeft.execute(this, left), toStrRight.execute(this, right), LuaLanguage.STRING_ENCODING, true);
    }

    private static boolean isStringOrNumber(Object value) {
        return value instanceof TruffleString || value instanceof String || value instanceof Long || value instanceof Double;
    }
}
