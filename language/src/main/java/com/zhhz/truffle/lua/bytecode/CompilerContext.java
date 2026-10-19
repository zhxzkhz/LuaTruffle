package com.zhhz.truffle.lua.bytecode;

/**
 * 编译期间在多个 {@link FuncState} 之间共享的状态 (对应 lparser.c 中 LexState 的
 * 部分字段：{@code lastline} 等)。
 */
public final class CompilerContext {

    /** 当前词法行号 (luaK_code 用它填写 lineinfo)。 */
    public int lastline = 0;

    /** 当前处理的源名 (如 "@file.lua")。 */
    public String source;

    /** 源码文本 (字节模型)，用于回查"上一个 token"的行号等。 */
    public String text;

    /**
     * 常量去重索引表 (对应 lcode.c 中 LexState 的 {@code h})。整个编译单元
     * (主块及所有嵌套函数) 共享这一个表；其 value 是"当时那个函数"里的常量下标。
     */
    public final java.util.Map<LuaConstant, Integer> kmap = new java.util.HashMap<>();
}
