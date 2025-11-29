package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.strings.TruffleString;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

/**
 * 一个存放所有 Lua 元方法名 TruffleString 常量的类。
 * 这可以提高性能并防止拼写错误。
 */
public final class LuaMetatables {

    public static final TruffleString NIL_STRING = toTruffleString("nil");
    public static final TruffleString TRUE_STRING = toTruffleString("true");
    public static final TruffleString FALSE_STRING = toTruffleString("false");

    // 私有构造函数，防止实例化
    private LuaMetatables() {}

    // --- 算术元方法 ---
    public static final TruffleString __ADD = toTruffleString("__add");             // +
    public static final TruffleString __SUB = toTruffleString("__sub");             // -
    public static final TruffleString __MUL = toTruffleString("__mul");             // *
    public static final TruffleString __DIV = toTruffleString("__div");             // /
    public static final TruffleString __MOD = toTruffleString("__mod");             // %
    public static final TruffleString __POW = toTruffleString("__pow");             // ^
    public static final TruffleString __UNM = toTruffleString("__unm");             // 一元负
    public static final TruffleString __IDIV = toTruffleString("__idiv");           // // (整除)

    // --- 位运算元方法 --- (Lua 5.3+)
    public static final TruffleString __BAND = toTruffleString("__band");           // &
    public static final TruffleString __BOR = toTruffleString("__bor");             // |
    public static final TruffleString __BXOR = toTruffleString("__bxor");           // ~ (二元异或)
    public static final TruffleString __BNOT = toTruffleString("__bnot");           // ~ (一元按位非)
    public static final TruffleString __SHL = toTruffleString("__shl");             // << (左移)
    public static final TruffleString __SHR = toTruffleString("__shr");             // >> (右移)

    // --- 关系元方法 ---
    public static final TruffleString __EQ = toTruffleString("__eq");               // ==
    public static final TruffleString __LT = toTruffleString("__lt");               // <
    public static final TruffleString __LE = toTruffleString("__le");               // <=

    // (`>`, `>=`, `~=`) 没有自己的元方法，Lua 会通过对 `<` 和 `==` 的结果取反来模拟它们

    // --- 库相关的元方法 ---

    @Deprecated public static final TruffleString __IPAIRS = toTruffleString("__ipairs");       // for in ipairs(t)(已废弃)
    public static final TruffleString __PAIRS = toTruffleString("__pairs");         // for in pairs(t)
    public static final TruffleString __TOSTRING = toTruffleString("__tostring");   // tostring(t)

    // --- Table 访问元方法 ---
    public static final TruffleString __INDEX = toTruffleString("__index");         // t[k] (读取)
    public static final TruffleString __NEWINDEX = toTruffleString("__newindex");   // t[k] = v (写入)
    public static final TruffleString __LEN = toTruffleString("__len");             // #t (长度)

    // --- 其他元方法 ---
    public static final TruffleString __CALL = toTruffleString("__call");           // t() (作为函数调用)
    public static final TruffleString __CONCAT = toTruffleString("__concat");       // .. (字符串连接)
    public static final TruffleString __GC = toTruffleString("__gc");               // 垃圾回收 (在 Truffle 中通常较难精确实现)
    public static final TruffleString __MODE = toTruffleString("__mode");           // 用于弱引用 table (weak tables)
    public static final TruffleString __METATABLE = toTruffleString("__metatable"); // 用于保护元表 (getmetatable/setmetatable)
    public static final TruffleString __NAME = toTruffleString("__name");           // 用于 `tostring` 默认输出 (Lua 5.4+)
}
