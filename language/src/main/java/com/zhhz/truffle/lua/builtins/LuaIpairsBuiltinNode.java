package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 实现了Lua的 'ipairs' 内置函数。
 */
@NodeInfo(shortName = "ipairs")
public abstract class LuaIpairsBuiltinNode extends LuaBuiltinNode {

    /**
     * 我们需要一个办法来获取迭代器函数的实例。
     * 最佳实践是在 LuaContext 中只创建一次并缓存起来。
     */
    private LuaFunction getIteratorFunction() {
        return getContext().getIpairsIterator();
    }

    @Specialization
    public Object doIPairs(Object[] arguments) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'ipairs' (table expected)",this);
        }

        // 组装并返回三元组：
        // 1. 迭代器函数 (我们自己实现的 IPairsIterator)
        // 2. 状态 (table 本身)
        // 3. 初始控制变量 (0)
        return new LuaMultiValue(new Object[]{
                getIteratorFunction(),
                arguments[0],
                0L // 初始索引是 0
        });
    }
}
