package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class LuaBuiltinTest extends AbstractLuaTest {

    private Context context;

    @BeforeEach
    public void setUp() {
        // 创建一个新的上下文，并指定允许访问我们的 "lua" 语言
        this.context = newContextBuilder("lua").build();
    }

    @AfterEach
    public void tearDown() {
        this.context.close();
    }



    /**
     * 辅助方法，用于执行Lua代码并返回结果
     * @param code 要执行的Lua代码字符串，必须是一个返回值的表达式
     * @return 执行结果，封装在Truffle的Value对象中
     */
    protected Value runLua(String code) {
        // 我们通过 "return " 来确保代码块是一个返回值的表达式
        return context.eval("lua", "return " + code);
    }

}
