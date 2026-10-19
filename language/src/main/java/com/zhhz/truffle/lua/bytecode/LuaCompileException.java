package com.zhhz.truffle.lua.bytecode;

/**
 * 编译（AST → Proto）期间出现的语法/语义错误 (对应 luaX_syntaxerror / luaK_semerror)。
 */
public final class LuaCompileException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LuaCompileException(String message) {
        super(message);
    }
}
