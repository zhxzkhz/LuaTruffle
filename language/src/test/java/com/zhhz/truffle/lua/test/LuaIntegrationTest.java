package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

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

    @Test
    public void testProtectedCallsPreserveErrorObjectsAndNilResults() {
        Value result = eval("""
                local marker = {kind = 'marker'}
                local ok1, caught = pcall(error, marker)
                local ok2, transformed = xpcall(function() error(marker) end,
                    function(value) return value.kind end)
                local ok3, nilResult = pcall(function() return nil end)
                local ok4, a, b = pcall(function() return 1, nil end)
                return not ok1, caught == marker, not ok2, transformed,
                    ok3, nilResult == nil, ok4, a, b == nil
                """);
        assertTrue(result.getArrayElement(0).asBoolean());
        assertTrue(result.getArrayElement(1).asBoolean());
        assertTrue(result.getArrayElement(2).asBoolean());
        assertEquals("marker", result.getArrayElement(3).asString());
        assertTrue(result.getArrayElement(4).asBoolean());
        assertTrue(result.getArrayElement(5).asBoolean());
        assertTrue(result.getArrayElement(6).asBoolean());
        assertEquals(1, result.getArrayElement(7).asInt());
        assertTrue(result.getArrayElement(8).asBoolean());
    }

    @Test
    public void testErrorAddsPositionPrefixForStringMessages() {
        // level 1（默认）：前缀指向 error() 的调用行（第 2 行）
        Value level1 = eval("""
                local function inner()
                    error('boom')
                end
                local ok, err = pcall(inner)
                return err
                """);
        assertTrue(level1.asString().endsWith(":2: boom"), level1.asString());
    }

    @Test
    public void testErrorLevelZeroOmitsPositionPrefix() {
        // level 0：不加位置前缀（若 level 参数被忽略而回落到 1，此测试会失败）
        assertEquals("plain", eval("""
                local ok, err = pcall(function() error('plain', 0) end)
                return err
                """).asString());
    }

    @Test
    public void testErrorLevelTwoReportsCallerCallSite() {
        // level 2：前缀指向 outer 中调用 inner 的行（第 5 行），而非 error() 所在行
        Value level2 = eval("""
                local function inner()
                    error('deep', 2)
                end
                local function outer()
                    inner()
                end
                local ok, err = pcall(outer)
                return err
                """);
        assertTrue(level2.asString().endsWith(":5: deep"), level2.asString());
    }

    @Test
    public void testErrorAcceptsIntegralFloatLevel() {
        // 2.0 与 2 等价
        Value level2 = eval("""
                local function inner()
                    error('deep', 2.0)
                end
                local function outer()
                    inner()
                end
                local ok, err = pcall(outer)
                return err
                """);
        assertTrue(level2.asString().endsWith(":5: deep"), level2.asString());
    }

    @Test
    public void testErrorLeavesNonStringMessagesUnchanged() {
        Value result = eval("""
                local marker = {kind = 'marker'}
                local ok, caught = pcall(error, marker, 2)
                return not ok, caught == marker
                """);
        assertTrue(result.getArrayElement(0).asBoolean());
        assertTrue(result.getArrayElement(1).asBoolean());
    }

    @Test
    public void testErrorRejectsNonNumericLevel() {
        Value result = eval("""
                local ok, err = pcall(function() error('x', 'str') end)
                return not ok, err
                """);
        assertTrue(result.getArrayElement(0).asBoolean());
        assertTrue(result.getArrayElement(1).asString().contains("number expected"),
                result.getArrayElement(1).asString());
    }

    @Test
    public void testMissingTableKeysReturnNilForAllKeyKinds() {
        Value result = eval("""
                local table = {}
                return table[0] == nil, table[-1] == nil,
                    table.missing == nil, table[{}] == nil
                """);
        assertTrue(result.getArrayElement(0).asBoolean());
        assertTrue(result.getArrayElement(1).asBoolean());
        assertTrue(result.getArrayElement(2).asBoolean());
        assertTrue(result.getArrayElement(3).asBoolean());
    }

    @Test
    public void testApplicationArgumentsUseLuaArgTable() {
        try (Context argumentContext = newContextBuilder("lua")
                .arguments("lua", new String[]{"lua", "script.lua", "first", "second"}).build()) {
            Value result = argumentContext.eval("lua", """
                    local i = 0
                    while arg[i] do i = i - 1 end
                    local progname = arg[i + 1]
                    return type(arg), arg[0], arg[1], arg[2], arg[-1],
                        i, progname
                    """);
            assertEquals("table", result.getArrayElement(0).asString());
            assertEquals("script.lua", result.getArrayElement(1).asString());
            assertEquals("first", result.getArrayElement(2).asString());
            assertEquals("second", result.getArrayElement(3).asString());
            assertEquals("lua", result.getArrayElement(4).asString());
            assertEquals(-2, result.getArrayElement(5).asInt());
            assertEquals("lua", result.getArrayElement(6).asString());
        }
    }

    @Test
    public void testWhileLoopUpdatesLocalUsedByCondition() {
        Value result = eval("""
                local i = 0
                while i == 0 do i = i - 1 end
                return i
                """);
        assertEquals(-1, result.asInt());
    }

    @Test
    public void testStderrWriteBinding() {
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        try (Context ioContext = newContextBuilder("lua").err(error).build()) {
            Value result = ioContext.eval("lua", """
                    local handle = io.stderr:write('error ', 42, '\n')
                    return handle == io.stderr
                    """);
            assertTrue(result.asBoolean());
        }
        assertEquals("error 42\n", error.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testPrintUsesLuaNewlineOnAllPlatforms() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Context printContext = newContextBuilder("lua").out(output).build()) {
            printContext.eval("lua", "print(1, nil)");
        }
        assertEquals("1\tnil\n", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testStderrWriteAcceptsAllNumericWrappers() {
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        try (Context ioContext = newContextBuilder("lua").err(error).build()) {
            Value result = ioContext.eval("lua", "return io.stderr:write('a', 1, 'b', 2)");
            assertTrue(result.hasMembers());
        }
        assertEquals("a1b2", error.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testVersionIsAvailableToStderrWrite() {
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        try (Context ioContext = newContextBuilder("lua").err(error).build()) {
            Value result = ioContext.eval("lua", """
                    return _VERSION == 'Lua 5.3' and io.stderr:write(_VERSION)
                    """);
            assertTrue(result.hasMembers());
        }
        assertEquals("Lua 5.3", error.toString(StandardCharsets.UTF_8));
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
