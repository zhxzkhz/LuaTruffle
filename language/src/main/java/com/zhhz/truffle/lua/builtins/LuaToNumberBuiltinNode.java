package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

@NodeInfo(shortName = "tonumber")
public abstract class LuaToNumberBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary // 数字解析是慢路径
    public Object doToNumber(Object[] arguments,
                             @Cached("create()") LuaCoerceToNumberNode toNumberNode) {
        if (arguments.length < 1) {
            return LuaNil.SINGLETON; // tonumber() -> nil
        }

        Object value = arguments[0];

        // --- 情况 1: 一个参数 tonumber(e) ---
        if (arguments.length == 1) {
            var v = toNumberNode.execute(value);
            if (v == null) return LuaNil.SINGLETON;
            return v;
        }

        // --- 情况 2: 两个参数 tonumber(e, base) ---
        else {
            if (!(value instanceof TruffleString strValue)) {
                // e 必须是字符串
                return LuaNil.SINGLETON;
            }
            if (!(arguments[1] instanceof Long)) {
                // base 必须是整数
                return LuaNil.SINGLETON;
            }

            long base = (Long) arguments[1];

            // 检查 base 的范围
            if (base < 2 || base > 36) {
                throw LuaException.create("bad argument #2 to 'tonumber' (base out of range)", this);
            }

            return parseStringWithBase(strValue, (int) base);
        }
    }

    /**
     * 辅助方法：将字符串按指定进制解析为 long。
     * @return 成功返回一个long,解析失败返回nil
     */
    private Object parseStringWithBase(TruffleString s, int base) {
        // Lua 对于带 base 的解析，规则更严格，通常不允许空格和符号
        // 这里是一个简化的实现
        try {
            // Java 的 Long.parseLong 可以处理不同进制
            return Long.parseLong(s.toJavaStringUncached(), base);
        } catch (NumberFormatException e) {
            return LuaNil.SINGLETON;
        }
    }

}
