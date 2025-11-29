package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

@NodeInfo(shortName = "select")
public abstract class LuaSelectBuiltinNode extends LuaBuiltinNode {

    // 预先缓存 '#' 字符串，用于高效比较
    private static final TruffleString HASH_STRING = toTruffleString("#");

    @Specialization
    @CompilerDirectives.TruffleBoundary
    public Object doSelect(Object[] arguments,
                           @Cached("create()") LuaCoerceToIntegerNode coerceValue) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'select' (value expected)", this);
        }

        Object indexOrHash = arguments[0];

        // --- 用法 1: select('#', ...) ---
        if (indexOrHash instanceof TruffleString && HASH_STRING.equals(indexOrHash)) {
            // 返回参数总数 (arguments.length - 1 是因为要去掉 '#' 本身)
            return (long)(arguments.length - 1);
        }

        // --- 用法 2: select(index, ...) ---

        Long value = coerceValue.execute(indexOrHash);

        // a. 检查第一个参数是否是数字
        if (value == null) {
            // 如果不是数字也不是'#'，则报错
            throw LuaException.create("bad argument #1 to 'select' (number expected, got " + LuaTypesUtil.getTypeName(indexOrHash) + ")", this);
        }

        int index = Math.toIntExact(value);

        int totalArgs = arguments.length - 1; // 真正可供选择的参数数量

        // b. 规范化索引
        int startIndex;
        if (index > 0) {
            // 正数索引
            startIndex = (int) index;
        } else if (index < 0 && Math.abs(index) < arguments.length) {
            // 负数索引: 从 totalArgs + index 开始
            startIndex = totalArgs + (int) index + 1;
        } else { // index == 0
            throw LuaException.create("bad argument #1 to 'select' (index out of range)", this);
        }

        // c. 边界检查和子序列截取
        if (startIndex < 1 || startIndex > totalArgs) {
            // 如果计算出的起始索引超出范围，返回一个空的多值列表
            return LuaNil.SINGLETON;
        }

        // 截取从 startIndex 开始的所有参数
        // 在 arguments 数组中，它们的实际起始位置是 startIndex
        int resultLength = totalArgs - startIndex + 1;
        Object[] result = new Object[resultLength];

        // System.arraycopy(source_array, source_pos, dest_array, dest_pos, length)
        System.arraycopy(arguments, startIndex, result, 0, resultLength);

        // d. 返回多值 (Object[])
        // 使用 LuaMultiValue 包裹
        return new LuaMultiValue(result);
    }

}
