package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;
import com.zhhz.truffle.lua.runtime.LuaValue;

/**
 * 实现了Lua的长度运算符 (#)。
 */
@NodeInfo(shortName = "#")
@NodeChild("operandNode")
public abstract class LuaLengthNode extends LuaExpressionNode {

    /** 操作数表达式节点（供运行时类型错误消息使用）。 */
    protected abstract LuaExpressionNode getOperandNode();

    @Specialization
    protected long lengthOfString(TruffleString operand) {
        return operand.byteLength(LuaLanguage.STRING_ENCODING);
    }

    // --- 特化 2: 快速路径，操作数是Table ---
    @Specialization
    @TruffleBoundary
    protected Object lengthOfTable(LuaTable operand) {
        // 若存在 __len 元方法，则调用之（标准 luaV_objlen 语义）
        LuaTable metatable = LuaContext.get(this).getEffectiveMetatable(operand);
        if (metatable != null) {
            Object lenMeta = metatable.rawget(LuaMetatables.__LEN, DynamicObjectLibrary.getUncached());
            if (lenMeta != null && lenMeta != LuaNil.SINGLETON) {
                try {
                    Object result = InteropLibrary.getUncached().execute(lenMeta, operand, operand);
                    return LuaMultiValue.firstValue(result);
                } catch (Exception e) {
                    throw LuaException.create("error in metamethod '__len': " + LuaException.messageOf(e), this);
                }
            }
        }
        // 返回我们为 table 维护的数组部分的长度
        return operand.getArraySize();
    }

    // --- 后备/回退方法，处理错误情况 ---
    @Fallback
    @CompilerDirectives.TruffleBoundary
    protected Object typeError(Object operand,
                               @Cached(value = "create()", neverDefault = true) LuaMetatableNode metatableNode) {

        // 基本类型（number/boolean/nil 等）可能通过类型元表提供 __len。
        Object metaResult = metatableNode.execute(operand, operand, LuaMetatables.__LEN);
        if (metaResult != null) {
            return LuaMultiValue.firstValue(metaResult);
        }

        // 抛出标准的Lua错误信息
        throw LuaException.runtimeError("attempt to get length of a "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName(operand) + " value"
                + getOperandNode().getErrorSuffix(), this);
    }

}
