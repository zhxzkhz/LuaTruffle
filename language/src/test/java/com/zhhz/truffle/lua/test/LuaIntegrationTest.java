package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LuaIntegrationTest extends AbstractLuaTest {

    private Context context;

    @BeforeEach
    public void setUp() {
        // 允许 IO 以便测试 print (虽然这里主要通过返回值测试)
        this.context =newContextBuilder("lua").build();
    }

    @AfterEach
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    private Value eval(String code) {
        return context.eval("lua", code);
    }

    // --- 1. 标准库函数 ---

    @Test
    public void testType() {
        assertEquals("nil", eval("return type(nil)").asString());
        assertEquals("number", eval("return type(123)").asString());
        assertEquals("string", eval("return type('s')").asString());
        assertEquals("table", eval("return type({})").asString());
        assertEquals("function", eval("return type(type)").asString());
    }

    @Test
    public void testToString() {
        // 确保 tostring 能正确处理各种类型
        assertEquals("123", eval("return tostring(123)").asString());
        assertEquals("nil", eval("return tostring(nil)").asString());
    }

    @Test
    public void testAssert() {
        // 测试 assert 成功
        assertEquals(10L, eval("return assert(10)").asLong());

        // 测试 assert 失败 (应该抛出 PolyglotException)
        assertThrows(org.graalvm.polyglot.PolyglotException.class, () -> {
            eval("assert(false, 'error message')");
        });
    }

    @Test
    public void testPcall() {
        // pcall 捕获错误
        String code = """
            local status, err = pcall(error, "my error")
            return status
        """;
        assertFalse(eval(code).asBoolean());

        // pcall 成功
        String code2 = """
            local status, res = pcall(function() return 100 end)
            return res
        """;
        assertEquals(100L, eval(code2).asLong());
    }

    // --- 2. 元表 (Metatables) ---

    @Test
    public void testMetatableAdd() {
        // 测试运算符重载 __add
        String code = """
            local t1 = { v = 10 }
            local t2 = { v = 20 }
        
            local mt = {
                __add = function(a, b)
                    return a.v + b.v
                end
            }
            setmetatable(t1, mt)
            setmetatable(t2, mt)
        
            return t1 + t2
        """;
        assertEquals(30L, eval(code).asLong());
    }

    @Test
    public void testIndexMetamethod() {
        // 测试 __index
        String code = """
            local proto = { foo = "bar" }
            local t = setmetatable({}, { __index = proto })
            return t.foo
        """;
        assertEquals("bar", eval(code).asString());
    }

    @Test
    public void testNewIndexMetamethod() {
        // 测试 __newindex
        String code = """
            local store = {}
            local t = setmetatable({}, {
                __newindex = function(table, key, value)
                    store[key] = value * 2
                end
            })
        
            t.a = 10
            return store.a
        """;
        assertEquals(20L, eval(code).asLong());
    }

    @Test
    public void testStringMetatable() {
        // 测试字符串是否拥有默认元表 (支持 : 方法调用)
        // 例如 "hello":upper()
        String code = "return ('abc'):upper()";
        assertEquals("ABC", eval(code).asString());
    }
}