package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 实现了Lua的 'next' 内置函数。
 */
@NodeInfo(shortName = "next")
public abstract class LuaNextBuiltinNode extends LuaBuiltinNode {

    // 假设你的 LuaTable 是一个包装类
    @Specialization
    public Object doNext(Object[] arguments,
                         @CachedLibrary(limit = "3") DynamicObjectLibrary objLib) {

        if (arguments.length < 1 || !(arguments[0] instanceof LuaTable table)) {
            throw LuaException.create("bad argument #1 to 'next' (table expected)",this);
        }

        Object key = (arguments.length > 1) ? arguments[1] : LuaNil.SINGLETON;

        Object nextKey = table.nextKey(key, objLib);

        if (nextKey == LuaNil.SINGLETON) {
            return LuaNil.SINGLETON;
        } else {
            Object nextValue = table.rawget(nextKey, objLib);
            return new LuaMultiValue(new Object[]{ nextKey, nextValue });
        }
    }


}