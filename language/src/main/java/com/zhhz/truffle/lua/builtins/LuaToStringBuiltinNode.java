package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;

/**
 * 实现了Lua的 'tostring' 内置函数。
 */
@NodeInfo(shortName = "tostring")
public abstract class LuaToStringBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary // 字符串转换是慢速路径
    protected TruffleString toString(Object[] arguments,
                                     @Cached("create()") LuaCoerceToStringNode toStringNode) {

        if (arguments.length < 1) {
            // 没有参数，可以抛出错误或返回空字符串
            return LuaTypesUtil.T_NIL;
        }

        return toStringNode.execute(arguments[0]);
    }
}
