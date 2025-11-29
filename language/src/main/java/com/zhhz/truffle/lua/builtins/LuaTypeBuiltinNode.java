
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.*;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.*;

/**
 * 实现 `type(v)` 内置函数。
 */
@NodeInfo(shortName = "type")
public abstract class LuaTypeBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public TruffleString doType(Object[] arguments) {

        // 1. 【参数检查】
        if (arguments.length < 1) {
            // 标准 Lua 在这里会报错，而不是认为参数是 nil
            throw LuaException.create("bad argument #1 to 'type' (value expected)", this);
        }

        Object value = arguments[0];

        // 2. 【核心】进行类型判断
        return getTypeName(value);
    }

    /**
     * 【核心逻辑】将任何 Lua 值映射到其类型名称字符串。
     * 这个方法应该是 @TruffleBoundary，因为它执行了一系列的 instanceof 检查，
     * 这在热路径中被认为是“慢”的。
     */
    @TruffleBoundary
    private TruffleString getTypeName(Object value) {

        if (value == null || value == LuaNil.SINGLETON) {
            return T_NIL;
        }

        if (value instanceof Long || value instanceof Double) {
            return T_NUMBER;
        }

        if (value instanceof TruffleString || value instanceof LuaString || value instanceof String) {
            return T_STRING;
        }

        if (value instanceof Boolean || value instanceof LuaBoolean) {
            return T_BOOLEAN;
        }

        if (value instanceof LuaTable) {
            return T_TABLE;
        }

        if (value instanceof LuaFunction) {
            return T_FUNCTION;
        }

        // 【回退】所有其他类型 (例如，通过 Interop 传入的外部 Java 对象)
        // 都可以被视为 'userdata'。
        // TODO: 可以返回Class类名
        return T_USERDATA;
    }


}