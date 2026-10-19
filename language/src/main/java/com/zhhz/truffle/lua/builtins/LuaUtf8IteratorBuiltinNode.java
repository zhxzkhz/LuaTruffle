package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * {@code utf8.codes} 使用的迭代器函数，对应 lutf8lib.c 的 {@code iter_aux}。
 * 每次返回 {@code (nextPosition, codepoint)}，合法性检查与标准库一致。
 */
@GenerateInline(false)
@NodeInfo(shortName = "utf8_iterator")
public abstract class LuaUtf8IteratorBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doIteration(Object[] arguments) {
        if (arguments.length < 2) {
            return LuaNil.SINGLETON;
        }
        byte[] source = LuaStringSupport.ensureBytes(arguments, 0, "codes");
        long n = 0;
        if (arguments[1] instanceof Long value) {
            n = value;
        } else if (arguments[1] instanceof Integer value) {
            n = value;
        }
        n = n - 1;
        if (n < 0) {
            n = 0;
        } else if (n < source.length) {
            n++;
            while (n < source.length && Utf8Library.isContinuation(source[(int) n])) {
                n++;
            }
        }
        if (n >= source.length) {
            return LuaNil.SINGLETON;
        }
        int[] out = new int[1];
        int next = Utf8Library.decode(source, (int) n, out);
        if (next < 0 || (next < source.length && Utf8Library.isContinuation(source[next]))) {
            throw LuaException.create("invalid UTF-8 code", this);
        }
        return new LuaMultiValue(new Object[]{n + 1, (long) out[0]});
    }
}
