package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaNil;
@GenerateInline(false)

@NodeInfo(shortName = "tonumber")
public abstract class LuaToNumberBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary // 数字解析是慢路径
    public Object doToNumber(Object[] arguments,
                             @Cached(value = "create()", neverDefault = true) LuaCoerceToNumberNode toNumberNode) {
        if (arguments.length < 1) {
            // 零参数：luaL_checkany 报 "value expected"（5.1–5.4 一致，官方套件 errors.lua:54）
            throw LuaException.create("bad argument #1 to 'tonumber' (value expected)", null);
        }

        Object value = arguments[0];

        // --- 情况 1: 一个参数 tonumber(e)：nil 与不可转换的值均返回 nil，不报错 ---
        if (arguments.length == 1) {
            var v = toNumberNode.execute(value);
            if (v == null) return LuaNil.SINGLETON;
            return v;
        }

        // --- 情况 2: 两个参数 tonumber(e, base) ---
        else {
            // base 为 nil 时按标准转换处理（lua_isnoneornil）
            if (arguments[1] == LuaNil.SINGLETON || arguments[1] == null) {
                var v = toNumberNode.execute(value);
                if (v == null) return LuaNil.SINGLETON;
                return v;
            }
            // 5.4 顺序：先按 luaL_checkinteger 校验 base，再要求 e 是字符串，最后校验范围
            long base = coerceBase(arguments[1]);
            if (!(value instanceof TruffleString strValue)) {
                throw LuaException.create("bad argument #1 to 'tonumber' (string expected, got "
                        + LuaTypesUtil.getArgumentTypeName(value) + ")", null);
            }
            if (base < 2 || base > 36) {
                throw LuaException.create("bad argument #2 to 'tonumber' (base out of range)", this);
            }
            return parseStringWithBase(strValue, (int) base);
        }
    }

    /**
     * 复刻标准 Lua 的 {@code luaL_checkinteger}：整数直接用；整数值浮点收窄；
     * 字符串按数字解析（容忍首尾空白，支持负数）；其余报 "number expected, got X"。
     */
    private long coerceBase(Object baseArg) {
        if (baseArg instanceof Long l) {
            return l;
        }
        if (baseArg instanceof Number number) {
            if (baseArg instanceof Double || baseArg instanceof Float) {
                double d = number.doubleValue();
                if (Double.isNaN(d) || Double.isInfinite(d)
                        || d != Math.rint(d) || d < -9223372036854775808.0 || d >= 9223372036854775808.0) {
                    throw noIntegerRepresentation();
                }
            }
            return number.longValue();
        }
        if (baseArg instanceof TruffleString ts) {
            Object parsed = LuaCoerceToNumberNode.parseLuaNumber(ts.toJavaStringUncached());
            if (parsed instanceof Long l) {
                return l;
            }
            if (parsed instanceof Double) {
                throw noIntegerRepresentation();
            }
            throw LuaException.create("bad argument #2 to 'tonumber' (number expected, got string)", null);
        }
        throw LuaException.create("bad argument #2 to 'tonumber' (number expected, got "
                + LuaTypesUtil.getArgumentTypeName(baseArg) + ")", null);
    }

    private RuntimeException noIntegerRepresentation() {
        return LuaException.create("bad argument #2 to 'tonumber' (number has no integer representation)", null);
    }

    /**
     * 辅助方法：将字符串按指定进制解析为 long。
     * 复刻标准 Lua 的 {@code b_str2int}：
     * <ul>
     *   <li>跳过首尾空白（空格、\f、\n、\r、\t、\v）；</li>
     *   <li>允许可选的 {@code +}/{@code -} 前导符号；</li>
     *   <li>逐位以 64 位无符号方式累加（溢出自然回绕）；</li>
     *   <li>仅当整个字符串被消费完才算成功。</li>
     * </ul>
     *
     * @return 成功返回 Long，解析失败返回 nil
     */
    private Object parseStringWithBase(TruffleString ts, int base) {
        String s = ts.toJavaStringUncached();
        int len = s.length();
        int i = 0;
        while (i < len && isLuaSpace(s.charAt(i))) {
            i++;
        }
        boolean negative = false;
        if (i < len && (s.charAt(i) == '-' || s.charAt(i) == '+')) {
            negative = s.charAt(i) == '-';
            i++;
        }
        // 标准 Lua：符号之后必须至少有一个字母/数字，否则解析失败
        if (i >= len || !isAsciiAlnum(s.charAt(i))) {
            return LuaNil.SINGLETON;
        }
        long n = 0;
        while (i < len) {
            char c = s.charAt(i);
            int digit;
            if (c >= '0' && c <= '9') {
                digit = c - '0';
            } else if (c >= 'a' && c <= 'z') {
                digit = c - 'a' + 10;
            } else if (c >= 'A' && c <= 'Z') {
                digit = c - 'A' + 10;
            } else {
                break;
            }
            if (digit >= base) {
                // 非法数字（例如 base=10 时的 'x'）直接判为失败
                return LuaNil.SINGLETON;
            }
            n = n * base + digit; // 与 lua_Unsigned 一致，溢出回绕
            i++;
        }
        while (i < len && isLuaSpace(s.charAt(i))) {
            i++;
        }
        if (i != len) {
            return LuaNil.SINGLETON;
        }
        if (negative) {
            n = -n;
        }
        return n;
    }

    /** 标准 Lua {@code b_str2int} 使用的空白字符集合。 */
    private static boolean isLuaSpace(char c) {
        return c == ' ' || c == '\f' || c == '\n' || c == '\r' || c == '\t' || c == '\u000b';
    }

    /** ASCII 字母或数字（对应 C 的 {@code isalnum}）。 */
    private static boolean isAsciiAlnum(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

}
