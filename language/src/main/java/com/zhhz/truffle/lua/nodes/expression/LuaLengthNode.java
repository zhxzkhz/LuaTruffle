package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;
import com.zhhz.truffle.lua.runtime.LuaValue;

/**
 * 实现了Lua的长度运算符 (#)。
 */
@NodeInfo(shortName = "#")
@NodeChild("operandNode")
public abstract class LuaLengthNode extends LuaExpressionNode {

    // --- 特化 1: 快速路径，操作数是字符串 ---
    @Specialization
    protected int lengthOfString(LuaString operand,
                                 @Cached TruffleString.CodePointLengthNode codePointLengthNode) {
        return codePointLengthNode.execute(operand.getValue(), LuaLanguage.STRING_ENCODING);
    }

    @Specialization
    protected int lengthOfString(TruffleString operand,
                                 @Cached TruffleString.CodePointLengthNode codePointLengthNode) {
        return codePointLengthNode.execute(operand, LuaLanguage.STRING_ENCODING);
    }

    // --- 特化 2: 快速路径，操作数是Table ---
    @Specialization
    protected int lengthOfTable(LuaTable operand) {
        // 返回我们为 table 维护的数组部分的长度
        // (需要为 LuaTable 添加 public getter: getArraySize())
        return operand.getArraySize();
    }

    // --- 后备/回退方法，处理错误情况 ---
    @Fallback
    @CompilerDirectives.TruffleBoundary
    protected Object typeError(Object operand,
                               @Bind("$node") Node node) {

        // 抛出标准的Lua错误信息
        String typeName = (operand instanceof LuaValue)
                ? ((LuaValue) operand).getTypeName()
                : "value";
        throw LuaException.create(String.format("attempt to get length of a %s value", typeName), node);
    }

}