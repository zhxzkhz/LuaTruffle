package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.exception.AbstractTruffleException;
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
    protected static Object doConcat(TruffleString left,
                                          TruffleString right,
                                          @Bind Node node,
                                          @Cached LuaToTruffleStringNode toTruffleStringNodeLeft,
                                          @Cached LuaToTruffleStringNode toTruffleStringNodeRight,
                                          @Cached("create()") LuaMetatableNode metatableNode,
                                          @Cached TruffleString.ConcatNode concatNode) {
        Object result = metatableNode.execute(left, right, LuaMetatables.__CONCAT);
        if (result != null) {
            return result;
        }
        return concatNode.execute(toTruffleStringNodeLeft.execute(node, left), toTruffleStringNodeRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);
    }

    // --- 慢速路径：处理元方法和错误 ---

    /**
     * 回退 (Fallback) 特化: 处理所有其他类型组合。
     */
    @Fallback
    @TruffleBoundary
    protected static Object doGeneric(Object left, Object right,
                                      @Bind Node node,
                                      @Cached LuaToTruffleStringNode toTruffleStringNodeLeft,
                                      @Cached LuaToTruffleStringNode toTruffleStringNodeRight,
                                      @Cached("create()") LuaMetatableNode metatableNode,
                                      @Cached TruffleString.ConcatNode concatNode) {

        if (isStringOrNumber(left)) {
            throw typeError(left, right, node);
        }
        if (isStringOrNumber(right)) {
            throw typeError(left, right, node);
        }

        Object result = metatableNode.execute(left, right, LuaMetatables.__CONCAT);
        if (result != null) {
            return result;
        }

        return concatNode.execute(toTruffleStringNodeLeft.execute(node, left), toTruffleStringNodeRight.execute(node, right), LuaLanguage.STRING_ENCODING, true);

    }

    // --- 辅助方法 ---

    private static boolean isStringOrNumber(Object value) {
        return !(value instanceof String) &&!(value instanceof TruffleString) && !(value instanceof Long) && !(value instanceof Double);
    }


    public static AbstractTruffleException typeError(Object left, Object right, @Bind Node node) {
        throw LuaException.typeError(node, "..", left, right);
    }

    // --- 辅助方法 ---


    public static boolean isString(Object a, Object b) {
        return a instanceof TruffleString || b instanceof TruffleString;
    }


}
