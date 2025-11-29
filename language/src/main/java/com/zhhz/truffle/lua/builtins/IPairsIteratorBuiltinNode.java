package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

@NodeInfo(shortName = "ipairs_iterator")
public abstract class IPairsIteratorBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doIteration(Object[] arguments) {

        if (arguments.length < 2 || !(arguments[0] instanceof LuaTable table)) {
            // 这个函数不应该被用户直接调用，所以错误处理可以简化
            return LuaNil.SINGLETON;
        }

        int currentIndex = -1;
        if (arguments[1] instanceof Long l) {
            currentIndex = Math.toIntExact(l);
        } else if (arguments[1] instanceof Integer integer) {
            currentIndex = integer;
        } else {
            return LuaNil.SINGLETON;
        }

        // 1. 【计算下一个索引】
        int nextIndex = currentIndex + 1;

        // 2. 【读取下一个值】
        //    这里我们直接调用 table 的【原始】读取方法，因为 ipairs 不涉及元方法
        Object nextValue = table.arrayRawGet(nextIndex);

        // 3. 【检查终止条件】
        if (nextValue == null || nextValue == LuaNil.SINGLETON) {
            // 如果值为 nil，返回 nil，这将终止 for..in 循环
            return LuaNil.SINGLETON;
        } else {
            // 否则，返回 (nextIndex, nextValue)
            return new LuaMultiValue(new Object[]{ nextIndex, nextValue });
        }
    }
}
