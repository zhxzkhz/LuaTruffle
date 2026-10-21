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
    public void xpcallHandlerRunsBeforeStackUnwinds() {
        // 消息处理函数在抛出点（栈尚未展开）执行，因此能看到 error 与调用者帧；
        // pcall 内部的错误不触发外层 xpcall 的处理函数；处理函数自身出错时为 LUA_ERRERR。
        Value result = eval("""
                local function f() error('boom') end
                local ok, traceback = xpcall(f, function(e) return debug.traceback(e, 2) end)
                local ok2, nested, nestedMessage = xpcall(function()
                    return pcall(function() error('caught') end)
                end, function(e) return 'handler:' .. e end)
                local ok3, handlerError = xpcall(function() error('x') end,
                    function(e) error('handler failed') end)
                return ok, traceback, ok2, nested, nestedMessage, ok3, handlerError
                """);
        assertFalse(result.getArrayElement(0).asBoolean(), "xpcall reports the error");
        String traceback = result.getArrayElement(1).asString();
        assertTrue(traceback.contains("[C]: in function 'error'"), traceback);
        assertTrue(traceback.contains("in function 'f'"), traceback);
        assertTrue(traceback.contains("[C]: in function 'xpcall'"), traceback);
        assertTrue(result.getArrayElement(2).asBoolean(), "outer xpcall succeeds");
        assertFalse(result.getArrayElement(3).asBoolean(), "inner pcall reports the error");
        String nestedMessage = result.getArrayElement(4).asString();
        assertTrue(nestedMessage.endsWith("caught"), nestedMessage);
        assertFalse(nestedMessage.startsWith("handler:"), "pcall suppresses the outer handler");
        assertFalse(result.getArrayElement(5).asBoolean());
        assertEquals("error in error handling", result.getArrayElement(6).asString());
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
    public void testErrorNumberGetsPositionPrefixLikeLua530() {
        // Lua 5.3.0 的 lua_isstring 语义：数字消息同样加位置前缀，且前缀后的
        // 字符串成为错误对象（pcall 返回它）
        Value result = eval("""
                local ok, err = pcall(function() error(10) end)
                return ok, err, type(err)
                """);
        assertFalse(result.getArrayElement(0).asBoolean());
        assertTrue(result.getArrayElement(1).asString().endsWith(":1: 10"),
                result.getArrayElement(1).asString());
        assertEquals("string", result.getArrayElement(2).asString());
    }

    @Test
    public void testErrorObjectTostringSeesOriginalStack() {
        // error() 在抛出前（调用栈仍存活）经 tostring 内建格式化非字符串错误对象，
        // __tostring 内 debug.getinfo(4) 指向 error() 调用行（本片段第 4 行）
        org.graalvm.polyglot.PolyglotException exception = assertThrows(org.graalvm.polyglot.PolyglotException.class, () -> eval("""
                local m = setmetatable({x = 0}, {__tostring = function(x)
                  return tostring(debug.getinfo(4).currentline + x.x)
                end})
                error(m)
                """));
        assertEquals("4", exception.getMessage());
    }

    @Test
    public void testErrorObjectTostringPreservesPcallObject() {
        Value result = eval("""
                local m = setmetatable({}, {__tostring = function() return 'formatted' end})
                local ok, err = pcall(error, m)
                return not ok, err == m
                """);
        assertTrue(result.getArrayElement(0).asBoolean());
        assertTrue(result.getArrayElement(1).asBoolean());
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
                    local handle = io.stderr:write('error ', 42, '\\n')
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

    // --- 3. 尾调用优化 (Tail Call Optimization) ---

    /**
     * 尾调用不得增加栈深度：深层尾递归必须正常返回，而非 StackOverflowError
     * （Lua 5.3 手册 3.4.10）。这里使用一个累计计数器避免空转被 JIT 折叠。
     */
    @Test
    public void testDeepTailRecursionDoesNotOverflow() {
        Value result = eval("""
                local function sum(n, acc)
                    if n == 0 then return acc end
                    return sum(n - 1, acc + 1)
                end
                return sum(1000000, 0)
                """);
        assertEquals(1000000L, result.asLong());
    }

    /**
     * 尾调用语义的"透明替换"要求：当一个非尾位置的调用包裹了一个内部尾调用时，
     * 外层调用自身的返回值必须由其函数体决定，而内部尾调用抛出的
     * {@code LuaTailCallException} 不得逃逸到外层调用者之外而丢掉外层逻辑。
     * <p>
     * 复现 doDirect 路径缺失 TCO 捕获的 bug：B 内部尾调用 f，A 调用 B 后 +1。
     * 若 LuaTailCallException 从 B 冒泡越过 A 的 +1，结果会错误地等于 f 的返回值。
     */
    @Test
    public void testNonTailCallerAroundTailCallPreservesOuterComputation() {
        Value result = eval("""
                local function f(x) return x end
                local function B(v) return f(v) end      -- 尾调用
                local function A(v) return B(v) + 1 end  -- 非尾调用：B() 的结果必须参与 +1
                return A(41)
                """);
        assertEquals(42L, result.asLong());
    }

    /**
     * 顶层 main chunk 的尾调用也必须正确执行（顶层 LuaAstRootNode.doTailCall 路径）。
     */
    @Test
    public void testTopLevelTailCall() {
        Value result = eval("""
                local function id(x) return x end
                return id(7)  -- 顶层 return f(...) 形式
                """);
        assertEquals(7L, result.asLong());
    }

    /**
     * 尾调用 obj:method(...) 形式同样需要 TCO 处理。
     */
    @Test
    public void testTailMethodCallPreservesOuterComputation() {
        Value result = eval("""
                local obj = { base = 100 }
                function obj:inner(v) return v end                  -- 尾调用目标
                function obj:outer(v) return self:inner(v) + 1 end  -- 非尾调用包裹
                return obj:outer(41)
                """);
        assertEquals(42L, result.asLong());
    }

    // --- import：导入 Java 类（宿主符号） ---

    @Test
    public void testImportJavaClass() {
        Value result = eval("return import('java.lang.String')");
        assertTrue(result.isHostObject());
        assertEquals(String.class, result.asHostObject());
    }

    @Test
    public void testImportStaticMethod() {
        // Java 对象统一用 . 调用：成员读取返回（绑定的）宿主函数，直接执行
        assertEquals(42L, eval("return import('java.lang.Math').abs(-42)").asLong());
        assertEquals(4L, eval("return import('java.lang.Math').round(3.7)").asLong());
    }

    @Test
    public void testImportConstructorAndInstanceMethod() {
        Value result = eval("""
                local SB = import('java.lang.StringBuilder')
                local sb = SB('hello')          -- 类调用即实例化
                sb.append(' world')             -- 实例方法：. 调用（方法已绑定接收者）
                return sb.toString()
                """);
        assertEquals("hello world", result.asString());
    }

    @Test
    public void testImportInstanceFieldsAndChaining() {
        Value result = eval("""
                local URI = import('java.net.URI')
                local u = URI('https://example.com/path')
                return u.getHost()
                """);
        assertEquals("example.com", result.asString());
    }

    @Test
    public void testImportRepeatedLookupIsCached() {
        // 重复导入应命中缓存并返回同一宿主类对象
        Value result = eval("""
                local a = import('java.lang.StringBuilder')
                for i = 1, 1000 do a = import('java.lang.StringBuilder') end
                return a == import('java.lang.StringBuilder')
                """);
        assertTrue(result.asBoolean());
    }

    @Test
    public void testImportUnknownSymbolThrows() {
        org.graalvm.polyglot.PolyglotException e = assertThrows(org.graalvm.polyglot.PolyglotException.class,
                () -> eval("return import('java.lang.NoSuchClassXYZ')"));
        assertTrue(e.getMessage().contains("cannot import 'java.lang.NoSuchClassXYZ'"));
    }

    @Test
    public void testImportPolyglotBindingsFallback() {
        // 无宿主符号时回退到 polyglot bindings（其他语言/宿主导出的符号）
        context.getPolyglotBindings().putMember("myExportedValue", 42);
        assertEquals(42L, eval("return import('myExportedValue')").asLong());
    }

    // --- json.encode：Lua 值 → JSON 文本 ---

    @Test
    public void testJsonArray() {
        assertEquals("[1,2,3]", eval("return json.encode({1,2,3})").asString());
        // 字符串键/混合值
        assertEquals("{}", eval("return json.encode({})").asString());
        assertEquals("[0.5,1000.0,-7,40.0]", eval("return json.encode({0.5, 1e3, -7, 40.0})").asString());
    }

    @Test
    public void testJsonObject() {
        // 键按数字序/字典序确定输出;nil 键值对省略
        assertEquals("{\"a\":1,\"b\":\"x\",\"c\":true}", eval("return json.encode({b=\"x\", a=1, c=true, d=nil})").asString());
        // 数字键转字符串键
        assertEquals("{\"9\":2,\"1111\":{\"a\":1}}", eval("return json.encode({[1111]={a=1}, [9]=2})").asString());
    }

    @Test
    public void testJsonNestedAndChinese() {
        Value result = eval("""
                local t = {name = '张三', age = 30, scores = {98.5, 87}, addr = {city = '北京', zip = 100000}}
                return json.encode(t)
                """);
        // 返回值是字节模型字符串（Latin-1 字符 = UTF-8 字节），按 UTF-8 还原后比对
        String actual = new String(result.asString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        assertEquals("{\"addr\":{\"city\":\"北京\",\"zip\":100000},\"age\":30,\"name\":\"张三\",\"scores\":[98.5,87]}",
                actual);
    }

    @Test
    public void testJsonEscaping() {
        // Lua 单引号字符串内双引号无需转义；JSON 输出必须转义为 \"（4 字符）
        Value result = eval("return json.encode({s = 'a\"b'})");
        assertEquals("{\"s\":\"a\\\"b\"}", result.asString());
    }

    @Test
    public void testJsonCycleDetected() {
        org.graalvm.polyglot.PolyglotException e = assertThrows(org.graalvm.polyglot.PolyglotException.class,
                () -> eval("local t = {}; t.self = t; return json.encode(t)"));
        assertTrue(e.getMessage().contains("cycle"));
    }

    @Test
    public void testJsonUnsupportedValueThrows() {
        org.graalvm.polyglot.PolyglotException e = assertThrows(org.graalvm.polyglot.PolyglotException.class,
                () -> eval("return json.encode(function() end)"));
        assertTrue(e.getMessage().contains("Cannot encode value of type 'function'"));
    }

    @Test
    public void testTailCallsThroughBytecodeVm() {
        Value result = eval("""
                local function body()
                    local function sum(n, acc)
                        if n == 0 then return acc end
                        return sum(n - 1, acc + n)
                    end
                    local function multi(n)
                        if n == 0 then return 1, 2, 3 end
                        return multi(n - 1)
                    end
                    local function none() return end
                    local function taily()
                        local i = 0
                        local function step()
                            i = i + 1
                            if i < 3 then return step() end
                            return i
                        end
                        return step
                    end
                    local a = sum(100, 0)
                    local b, c, d = multi(5)
                    local s = taily()
                    return a, b, c, d, s(), select('#', none())
                end
                local vm = assert(load(string.dump(body)))
                return vm()
                """);
        assertEquals(5050L, result.getArrayElement(0).asLong());
        assertEquals(1L, result.getArrayElement(1).asLong());
        assertEquals(2L, result.getArrayElement(2).asLong());
        assertEquals(3L, result.getArrayElement(3).asLong());
        assertEquals(3L, result.getArrayElement(4).asLong());
        assertEquals(0L, result.getArrayElement(5).asLong());
    }
}
