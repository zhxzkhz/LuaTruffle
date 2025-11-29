package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

@NodeInfo(shortName = "assert")
public abstract class LuaAssertBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doAssert(Object[] arguments,
                           @Cached("create()") LuaCoerceToStringNode toStringNode) {

        // 1. 检查是否有参数
        if (arguments.length == 0) {
            // assert() -> v 是 nil -> 失败
            throw LuaException.create("assertion failed!", this);
        }

        Object v = arguments[0];

        // 2. 【核心】进行真值判断
        if (LuaTypesUtil.isTruthy(v)) {
            // --- 3. 成功路径 ---
            // 返回所有原始参数

            if (arguments.length == 1) {
                // 如果只有一个参数，就返回那个单值
                return v;
            } else {
                // 如果有多个参数，返回一个 Object[] (符合多返回值约定)
                // 现在修改成 LuaMultiValue
                return new LuaMultiValue(arguments);
            }
        } else {
            // --- 4. 失败路径 ---
            String message;

            if (arguments.length > 1 && arguments[1] != null && arguments[1] != LuaNil.SINGLETON) {
                // 如果提供了第二个参数 (message)，使用它
                // 我们需要一个健壮的 to-string 转换
                message = toStringNode.execute(arguments[1]).toJavaStringUncached();
            } else {
                // 否则，使用默认消息
                message = "assertion failed!";
            }

            // 抛出错误
            throw LuaException.create(message, this);
        }
    }


}
