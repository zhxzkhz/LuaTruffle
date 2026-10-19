package com.zhhz.truffle.lua.bytecode;

/**
 * 解析/生成预编译 chunk 时出现的错误。
 *
 * <p>消息格式复刻 lundump.c 的 {@code error()}:
 * {@code "<name>: <why> precompiled chunk"}。
 */
public final class LuaBytecodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LuaBytecodeException(String message) {
        super(message);
    }

    /** 按 lundump.c 的格式构造消息。 */
    public static LuaBytecodeException chunkError(String name, String why) {
        return new LuaBytecodeException(name + ": " + why + " precompiled chunk");
    }
}
