package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

@GenerateUncached
public abstract class LuaCoerceToStringNode extends Node {

    public abstract TruffleString execute(Object value);

    // --- 快速路径 ---
    @Specialization
    protected TruffleString fromNil(LuaNil value) {
        return LuaMetatables.NIL_STRING; // 假设有一个常量 TruffleString "nil"
    }

    @Specialization
    protected TruffleString fromBoolean(boolean value) {
        return value ? LuaMetatables.TRUE_STRING : LuaMetatables.FALSE_STRING;
    }

    @Specialization
    protected TruffleString fromLong(long value) {
        // TruffleString 有高效的 fromLong 方法
        return TruffleString.fromLongUncached(value, LuaLanguage.STRING_ENCODING,false);
    }

    @Specialization
    @TruffleBoundary
    protected TruffleString fromDouble(double value) {
        return toTruffleString(String.valueOf(value));
    }

    @Specialization
    protected TruffleString fromTruffleString(TruffleString value) {
        // 字符串就是它自己
        return value;
    }

    // --- 慢速路径：处理 Table, Function, 和元方法 ---
    @Specialization(guards = {"!isPrimitive(value)"})
    @TruffleBoundary
    protected TruffleString fromObject(Object value,
                                       @Cached("create()") LuaMetatableNode metaNode) {
        if (value == null) {
            return LuaNil.NIL_CL;
        }
        // 【1. 优先查找 __tostring 元方法】
        Object result = metaNode.execute(value,null, LuaMetatables.__TOSTRING);

        if (result != null) {
            // 【重要】检查元方法的返回值
            if (result instanceof TruffleString) {
                return (TruffleString) result;
            } else {
                // 如果 __tostring 返回的不是字符串，Lua 会报错
                throw LuaException.create("'__tostring' must return a string", this);
            }
        }

        // 【2. 如果没有元方法，回退到默认表示】
        if (value instanceof LuaTable) {
            return toTruffleString("table: 0x" + Integer.toHexString(value.hashCode()));
        }
        if (value instanceof LuaFunction) {
            return toTruffleString("function: 0x" + Integer.toHexString(value.hashCode()));
        }

        // 其他 TruffleObject 或 Java 对象的默认表示
        return toTruffleString(value.toString());
    }

    @Fallback
    protected TruffleString fromObject(Object value){
        throw LuaException.create("未知问题",this);
    }

    // Guard
    protected boolean isPrimitive(Object value) {
        return value instanceof Boolean || value instanceof Long || value instanceof Double || value instanceof TruffleString;
    }
}
