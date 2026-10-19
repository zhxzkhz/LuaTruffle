package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 实现 {@code rawlen(v)}：返回 table 的原始长度或字符串的字节长度，忽略 {@code __len}。
 */
@GenerateInline(false)
@NodeInfo(shortName = "rawlen")
public abstract class LuaRawLenBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doRawLen(Object[] arguments) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'rawlen' (table or string expected)", this);
        }
        Object value = arguments[0];
        if (value instanceof LuaTable table) {
            return (long) table.getArraySize();
        }
        if (value instanceof TruffleString string) {
            return (long) string.byteLength(LuaLanguage.STRING_ENCODING);
        }
        throw LuaException.create("bad argument #1 to 'rawlen' (table or string expected)", this);
    }
}
