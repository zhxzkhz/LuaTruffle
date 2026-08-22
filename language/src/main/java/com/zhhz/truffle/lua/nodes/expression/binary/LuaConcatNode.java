package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
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
                                              @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(left, right, LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doStringLong(TruffleString left, long right,
                                                  @Bind Node node,
                                                  @Cached LuaToTruffleStringNode toStr,
                                                  @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(left, toStr.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doLongString(long left, TruffleString right,
                                                 @Bind Node node,
                                                 @Cached LuaToTruffleStringNode toStr,
                                                 @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStr.execute(node, left), right, LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doStringDouble(TruffleString left, double right,
                                                    @Bind Node node,
                                                    @Cached LuaToTruffleStringNode toStr,
                                                    @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(left, toStr.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doDoubleString(double left, TruffleString right,
                                                   @Bind Node node,
                                                   @Cached LuaToTruffleStringNode toStr,
                                                   @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStr.execute(node, left), right, LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doLongLong(long left, long right,
                                               @Bind Node node,
                                               @Cached LuaToTruffleStringNode toStrLeft,
                                               @Cached LuaToTruffleStringNode toStrRight,
                                               @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doDoubleLong(double left, long right,
                                                 @Bind Node node,
                                                 @Cached LuaToTruffleStringNode toStrLeft,
                                                 @Cached LuaToTruffleStringNode toStrRight,
                                                 @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doLongDouble(long left, double right,
                                                 @Bind Node node,
                                                 @Cached LuaToTruffleStringNode toStrLeft,
                                                 @Cached LuaToTruffleStringNode toStrRight,
                                                 @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    protected static TruffleString doDoubleDouble(double left, double right,
                                                   @Bind Node node,
                                                   @Cached LuaToTruffleStringNode toStrLeft,
                                                   @Cached LuaToTruffleStringNode toStrRight,
                                                   @Cached TruffleString.ConcatNode concatNode) {
        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    @Fallback
    @TruffleBoundary
    protected static Object doGeneric(Object left, Object right,
                                      @Bind Node node,
                                      @Cached LuaToTruffleStringNode toStrLeft,
                                      @Cached LuaToTruffleStringNode toStrRight,
                                      @Cached("create()") LuaMetatableNode metatableNode,
                                      @Cached TruffleString.ConcatNode concatNode) {

        Object result = metatableNode.execute(left, right, LuaMetatables.__CONCAT);
        if (result != null) {
            return result;
        }

        if (!isStringOrNumber(left) || !isStringOrNumber(right)) {
            throw LuaException.typeError(node, "..", left, right);
        }

        return concatNode.execute(toStrLeft.execute(node, left), toStrRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    private static boolean isStringOrNumber(Object value) {
        return value instanceof TruffleString || value instanceof String || value instanceof Long || value instanceof Double;
    }
}
