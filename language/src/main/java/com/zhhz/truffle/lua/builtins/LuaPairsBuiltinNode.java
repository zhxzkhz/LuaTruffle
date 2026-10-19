package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 实现了Lua的 'pairs' 内置函数。
 */
@SuppressWarnings("deprecation")
@NodeInfo(shortName = "pairs")
public abstract class LuaPairsBuiltinNode extends LuaBuiltinNode {


    TruffleString nextName = TruffleString.fromJavaStringUncached("next", LuaLanguage.STRING_ENCODING);

    @Specialization
    public Object doPairs(Object[] arguments,@CachedLibrary(limit = "3") DynamicObjectLibrary objLib) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'pairs' (table expected)",this);
        }

        Object target = arguments[0];

        // 标准语义：若存在 __pairs 元方法，则调用它并返回其（补齐到 3 个的）结果。
        LuaTable metatable = getContext().getEffectiveMetatable(target);
        if (metatable != null) {
            Object pairsMeta = metatable.rawget(LuaMetatables.__PAIRS, objLib);
            if (pairsMeta != null && pairsMeta != LuaNil.SINGLETON) {
                Object result;
                try {
                    result = InteropLibrary.getUncached().execute(pairsMeta, target);
                } catch (Exception e) {
                    throw LuaException.create("error in metamethod '__pairs': " + e.getMessage(), this);
                }
                return normalizeToThree(result);
            }
        }

        // 1. 获取 `next` 函数。
        //    我们需要从全局环境中查找 `next` 函数。
        LuaFunction nextFunction = (LuaFunction) getContext().getGlobals().rawget(nextName,objLib);
        if (nextFunction == null) {
            throw LuaException.create("'next' function not found in built-in context",this);
        }

        // 2. 组装三元组：(next_function, table_itself, nil)
        return new LuaMultiValue(new Object[]{
                nextFunction,
                target,
                LuaNil.SINGLETON
        });
    }

    /** 将元方法结果补齐/截断为 3 个值（对应标准 lua_call(L, 1, 3)）。 */
    private static LuaMultiValue normalizeToThree(Object result) {
        Object[] values = result instanceof LuaMultiValue multiValue
                ? multiValue.values()
                : new Object[]{result == null ? LuaNil.SINGLETON : result};
        Object[] three = new Object[3];
        for (int i = 0; i < three.length; i++) {
            three[i] = i < values.length ? values[i] : LuaNil.SINGLETON;
        }
        return new LuaMultiValue(three);
    }
}
