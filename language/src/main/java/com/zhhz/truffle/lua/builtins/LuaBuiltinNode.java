package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.nodes.Node;
import com.zhhz.truffle.lua.runtime.LuaContext;

/**
 * 所有内置函数节点的抽象基类。
 *
 * <p>这个基类使用 @NodeChild 来自动执行一个 "argumentsNode"，
 * 这个子节点负责提供传递给内置函数的所有参数，通常是一个数组。
 *
 * <p>@GenerateNodeFactory 注解是自动化注册的关键，它告诉Truffle DSL
 * 为所有继承此类的具体节点生成一个工厂类。
 */
public abstract class LuaBuiltinNode extends Node {

    // 我们可以添加一个辅助方法来方便地获取上下文
    protected final LuaContext getContext() {
        return LuaContext.get(this);
    }

    /**
     * 这是所有内置函数逻辑节点必须实现的契约方法。
     * @param arguments 从调用点传递过来的、已经计算好的参数数组。
     * @return 内置函数的返回值。
     */
    public abstract Object execute(Object[] arguments);

}