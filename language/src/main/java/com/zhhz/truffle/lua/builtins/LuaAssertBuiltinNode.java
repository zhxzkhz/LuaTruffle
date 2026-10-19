package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
@GenerateInline(false)

@NodeInfo(shortName = "assert")
public abstract class LuaAssertBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doAssert(Object[] arguments,
                           @Cached("create()") LuaCoerceToStringNode toStringNode) {

        // 1. 检查是否有参数：assert() -> luaL_checkany -> "value expected"
        if (arguments.length == 0) {
            throw LuaTypesUtil.argumentError("assert", 1, "value expected");
        }

        Object v = arguments[0];

        // 2. 【核心】进行真值判断
        if (LuaTypesUtil.isTruthy(v)) {
            // --- 3. 成功路径：返回所有原始参数 ---
            if (arguments.length == 1) {
                return v;
            } else {
                return new LuaMultiValue(arguments);
            }
        } else {
            // --- 4. 失败路径 ---
            // 标准 Lua：提供了第二个参数时，错误对象就是它（原样，不加位置前缀）；
            // 否则使用默认消息，并加上调用点的位置前缀。
            if (arguments.length > 1) {
                throw LuaException.create(arguments[1], this);
            }
            String message = LuaException.withPosition("assertion failed!", LuaTypesUtil.activeCallSite());
            throw LuaException.create(message, this);
        }
    }


}
