package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaFunction;

/**
 * 返回当前 chunk 的执行环境（即 {@code _ENV} 的初始值）。
 *
 * <p>调用约定：Lua 函数帧的 {@code arguments[0]} 存放被调用的 {@link LuaFunction}。
 * 对于由 {@code load} 加载并显式指定了第 4 个参数的 chunk，环境即该函数的
 * {@code environment} 字段；否则回退到全局表。
 */
public final class LuaEnvironmentNode extends LuaExpressionNode {

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object[] arguments = frame.getArguments();
        if (arguments.length > 0 && arguments[0] instanceof LuaFunction function) {
            Object environment = function.getEnvironment();
            if (environment != null) {
                return environment;
            }
        }
        return getContext().getGlobals();
    }
}
