package com.zhhz.truffle.lua.bytecode;

/**
 * Lua 5.3 预编译 chunk 的头部与格式常量 (lua.h / lundump.h / lparser.h)。
 */
public final class LuaBytecodeConstants {

    private LuaBytecodeConstants() {
    }

    /** LUA_SIGNATURE = ESC + "Lua"。 */
    public static final byte[] LUA_SIGNATURE = {0x1b, 'L', 'u', 'a'};
    /** 首个字节 (ESC)，load 时用于识别二进制 chunk。 */
    public static final int LUA_SIGNATURE_0 = 0x1b;

    /** LUAC_VERSION = major*16 + minor = 5*16 + 3 = 0x53。 */
    public static final int LUAC_VERSION = 0x53;
    /** 官方格式号。 */
    public static final int LUAC_FORMAT = 0;

    /** LUAC_DATA。 */
    public static final byte[] LUAC_DATA = {0x19, (byte) 0x93, '\r', '\n', 0x1a, '\n'};

    /** 头部声明的各类型大小 (64 位平台)。 */
    public static final int SIZEOF_INT = 4;
    public static final int SIZEOF_SIZE_T = 8;
    public static final int SIZEOF_INSTRUCTION = 4;
    public static final int SIZEOF_LUA_INTEGER = 8;
    public static final int SIZEOF_LUA_NUMBER = 8;

    /** 用于检测字节序的整数与浮点数。 */
    public static final long LUAC_INT = 0x5678L;
    public static final double LUAC_NUM = 370.5;

    /** 常量表每项的类型标签。 */
    public static final int LUA_TNIL = LuaConstant.LUA_TNIL;
    public static final int LUA_TBOOLEAN = LuaConstant.LUA_TBOOLEAN;
    public static final int LUA_TNUMFLT = LuaConstant.LUA_TNUMFLT;
    public static final int LUA_TSHRSTR = LuaConstant.LUA_TSHRSTR;
    public static final int LUA_TNUMINT = LuaConstant.LUA_TNUMINT;
    public static final int LUA_TLNGSTR = LuaConstant.LUA_TLNGSTR;

    /** 每条 SETLIST 指令最多刷入的字段数 (LFIELDS_PER_FLUSH)。 */
    public static final int LFIELDS_PER_FLUSH = 50;

    // --- is_vararg 标志位 (lparser.h) ---
    public static final int VARARG_HASARG = 1;
    public static final int VARARG_ISVARARG = 2;
    public static final int VARARG_NEEDSARG = 4;
}
