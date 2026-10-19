package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.TruffleObject;

/**
 * 表示 Lua 的 light userdata：仅以对象同一性标识，没有元表、没有 user value。
 *
 * <p>目前唯一的使用者是 {@code debug.upvalueid}——标准 Lua 返回的是指向该 upvalue
 * 的 light userdata 指针，其相等的语义就是指针相等。
 */
public final class LuaLightUserdata implements TruffleObject {

    private final Object identity;

    public LuaLightUserdata(Object identity) {
        this.identity = identity;
    }

    public Object getIdentity() {
        return identity;
    }
}
