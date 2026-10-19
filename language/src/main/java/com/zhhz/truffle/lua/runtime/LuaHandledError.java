package com.zhhz.truffle.lua.runtime;

/**
 * 承载“已在抛出点由消息处理函数处理过”的错误结果。
 * <p>
 * 标准 Lua 的 {@code luaG_errormsg} 会在【展开栈之前】调用消息处理函数（xpcall 的
 * handler）。对于栈溢出这种必须在栈仍然很深时生成回溯的情况，本引擎在抛出点直接调用
 * handler，并用本异常把处理结果带回给 xpcall。
 */
@SuppressWarnings("serial")
public final class LuaHandledError extends RuntimeException {

    private final Object handled;

    public LuaHandledError(Object handled) {
        super("error handled");
        this.handled = handled;
    }

    public Object getHandled() {
        return handled;
    }
}
