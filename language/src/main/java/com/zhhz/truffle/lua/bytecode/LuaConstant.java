package com.zhhz.truffle.lua.bytecode;

/**
 * 常量表中的一项。对应 dump 格式里的 TValue 标签集合。
 *
 * <p>标签值 (lobject.h):
 * <ul>
 *   <li>{@code LUA_TNIL = 0}</li>
 *   <li>{@code LUA_TBOOLEAN = 1}</li>
 *   <li>{@code LUA_TNUMFLT = 3}</li>
 *   <li>{@code LUA_TSHRSTR = 4}</li>
 *   <li>{@code LUA_TNUMINT = 19}</li>
 *   <li>{@code LUA_TLNGSTR = 20}</li>
 * </ul>
 */
public sealed interface LuaConstant
        permits LuaConstant.Nil, LuaConstant.Bool, LuaConstant.Int, LuaConstant.Flt, LuaConstant.Str {

    int LUA_TNIL = 0;
    int LUA_TBOOLEAN = 1;
    int LUA_TNUMFLT = 3;
    int LUA_TSHRSTR = 4;
    int LUA_TNUMINT = 19;
    int LUA_TLNGSTR = 20;

    /** 用于 dump 的标签值。 */
    int tag();

    record Nil() implements LuaConstant {
        public static final Nil INSTANCE = new Nil();

        @Override
        public int tag() {
            return LUA_TNIL;
        }
    }

    record Bool(boolean value) implements LuaConstant {
        @Override
        public int tag() {
            return LUA_TBOOLEAN;
        }
    }

    /** Lua 整数 (64 位有符号)。 */
    record Int(long value) implements LuaConstant {
        @Override
        public int tag() {
            return LUA_TNUMINT;
        }
    }

    /** Lua 浮点数 (双精度)。 */
    record Flt(double value) implements LuaConstant {
        @Override
        public int tag() {
            return LUA_TNUMFLT;
        }
    }

    /**
     * Lua 字符串，按原始字节保存 (二进制安全)。
     *
     * <p>{@code strTag} 保留原始串标签 ({@link #LUA_TSHRSTR} 或 {@link #LUA_TLNGSTR})，
     * 以便 dump 出的 chunk 与 C Lua 逐字节一致；相等性只比较字节内容。
     */
    record Str(byte[] bytes, int strTag) implements LuaConstant {
        @Override
        public int tag() {
            return strTag;
        }

        public Str(byte[] bytes) {
            this(bytes, LUA_TSHRSTR);
        }

        public Str(String s) {
            this(s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1), LUA_TSHRSTR);
        }

        public String asString() {
            return new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Str other && java.util.Arrays.equals(bytes, other.bytes);
        }

        @Override
        public int hashCode() {
            return java.util.Arrays.hashCode(bytes);
        }
    }
}
