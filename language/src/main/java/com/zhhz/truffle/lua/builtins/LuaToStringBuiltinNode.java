package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 实现了Lua的 'tostring' 内置函数。
 *
 * <p>对应基础库的 {@code luaB_tostring}：若存在 {@code __tostring} 元方法，则直接返回其调用结果
 * （即使结果是 nil，也不做类型检查；严格检查只出现在 print/concat 使用的 {@code luaL_tolstring}）。
 */
@GenerateInline(false)
@NodeInfo(shortName = "tostring")
public abstract class LuaToStringBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary // 字符串转换是慢速路径
    protected Object toString(Object[] arguments,
                              @Cached("create()") LuaCoerceToStringNode toStringNode) {

        if (arguments.length < 1) {
            throw LuaException.create("bad argument #1 to 'tostring' (value expected)", this);
        }

        Object value = arguments[0];
        LuaTable metatable = getContext().getEffectiveMetatable(value);
        if (metatable != null) {
            Object metamethod = metatable.rawget(LuaMetatables.__TOSTRING);
            if (metamethod != null && metamethod != LuaNil.SINGLETON) {
                try {
                    Object result = InteropLibrary.getUncached().execute(metamethod, value);
                    return LuaMultiValue.firstValue(result);
                } catch (Exception exception) {
                    throw LuaException.create("error in __tostring metamethod: " + LuaException.messageOf(exception), this);
                }
            }
        }

        return toStringNode.execute(value);
    }
}
