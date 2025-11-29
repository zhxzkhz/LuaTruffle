package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 实现了Lua的 'pairs' 内置函数。
 */
@NodeInfo(shortName = "pairs")
public abstract class LuaPairsBuiltinNode extends LuaBuiltinNode {


    TruffleString nextName = TruffleString.fromJavaStringUncached("next", LuaLanguage.STRING_ENCODING);

    @Specialization
    public Object doPairs(Object[] arguments,@CachedLibrary(limit = "3") DynamicObjectLibrary objLib) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaTable table)) {
            throw LuaException.create("bad argument #1 to 'pairs' (table expected)",this);
        }

        // 1. 获取 `next` 函数。
        //    我们需要从全局环境中查找 `next` 函数。
        //    一个好的设计是在 `LuaContext` 中缓存所有核心内置函数。

        LuaFunction nextFunction = (LuaFunction) getContext().getGlobals().rawget(nextName,objLib);
        if (nextFunction == null) {
            throw LuaException.create("'next' function not found in built-in context",this);
        }

        // 2. 组装三元组：(next_function, table_itself, nil)

        // 3. 用 Object[] 返回
        return new LuaMultiValue(new Object[]{
                nextFunction,
                table,
                LuaNil.SINGLETON
        });
    }
}
