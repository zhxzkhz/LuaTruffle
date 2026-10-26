package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LuaParserFixRegressionTest extends AbstractLuaTest {

    private Context context;

    @BeforeEach
    public void setUp() {
        this.context = newContextBuilder("lua").build();
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

    private Value run(String code) {
        return context.eval("lua", "return " + code);
    }

    // ===== P0 #3: createWriteNodeForFuncName NAME索引越界 =====

    @Test
    public void testFuncNameWithColonMethod() {
        String code = "local t = {}\n"
                + "function t:method()\n"
                + "    return 42\n"
                + "end\n"
                + "return t:method()\n";
        assertEquals(42L, eval(code).asLong());
    }

    @Test
    public void testFuncNameWithDotChain() {
        String code = "local a = {}\n"
                + "a.b = {}\n"
                + "function a.b.c()\n"
                + "    return 99\n"
                + "end\n"
                + "return a.b.c()\n";
        assertEquals(99L, eval(code).asLong());
    }

    // ===== P0 #4: visitStatFunctionDef 对非全局函数定义处理 =====

    @Test
    public void testFunctionDefOverridesLocal() {
        String code = "local f = 1\n"
                + "function f()\n"
                + "    return 10\n"
                + "end\n"
                + "return f()\n";
        assertEquals(10L, eval(code).asLong());
    }

    @Test
    public void testFunctionDefAsUpvalue() {
        String code = "local f\n"
                + "function f()\n"
                + "    return 77\n"
                + "end\n"
                + "return f()\n";
        assertEquals(77L, eval(code).asLong());
    }

    // ===== P0 #5: 十六进制浮点数解析 =====

    @Test
    public void testHexInteger() {
        assertEquals(255L, run("0xFF").asLong());
        assertEquals(16L, run("0x10").asLong());
    }

    @Test
    public void testHexFloatWithP() {
        assertEquals(15.0, run("0xfp0").asDouble(), 0.001);
        assertEquals(30.0, run("0xfp1").asDouble(), 0.001);
    }

    @Test
    public void testDecimalScientificNotation() {
        assertEquals(1200.0, run("1.2e3").asDouble(), 0.001);
        assertEquals(0.012, run("1.2e-2").asDouble(), 0.001);
    }

    // ===== P1 #1: 消除冗余 LuaNodeParser 实例 =====
    // (间接测试: 嵌套函数定义应该正常工作)

    @Test
    public void testNestedFunctionDefinitions() {
        String code = "function outer()\n"
                + "    function inner()\n"
                + "        function deepest()\n"
                + "            return 123\n"
                + "        end\n"
                + "        return deepest()\n"
                + "    end\n"
                + "    return inner()\n"
                + "end\n"
                + "return outer()\n";
        assertEquals(123L, eval(code).asLong());
    }

    // ===== P1 #2: Upvalue 链完整性 =====

    @Test
    public void testMultiLevelUpvalueRead() {
        String code = "local x = 10\n"
                + "function level1()\n"
                + "    function level2()\n"
                + "        function level3()\n"
                + "            return x\n"
                + "        end\n"
                + "        return level3()\n"
                + "    end\n"
                + "    return level2()\n"
                + "end\n"
                + "return level1()\n";
        assertEquals(10L, eval(code).asLong());
    }

    @Test
    public void testMultiLevelUpvalueWrite() {
        String code = "local x = 10\n"
                + "function level1()\n"
                + "    function level2()\n"
                + "        x = 99\n"
                + "    end\n"
                + "    level2()\n"
                + "end\n"
                + "level1()\n"
                + "return x\n";
        assertEquals(99L, eval(code).asLong());
    }

    // ===== P1 #10: 方法调用 receiver 不重复求值 =====

    @Test
    public void testMethodCallReceiverEvaluatedOnce() {
        String code = "local count = 0\n"
                + "local t = {}\n"
                + "t.value = 1\n"
                + "function t:getter()\n"
                + "    return self.value\n"
                + "end\n"
                + "return t:getter()\n";
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testMethodCallWithArgs() {
        String code = "local t = {x = 10}\n"
                + "function t:add(n)\n"
                + "    return self.x + n\n"
                + "end\n"
                + "return t:add(5)\n";
        assertEquals(15L, eval(code).asLong());
    }

    // ===== P1 #6: 转义序列 =====

    @Test
    public void testStringEscapeNewline() {
        assertEquals("a\nb", run("'a\\nb'").asString());
    }

    @Test
    public void testStringEscapeTab() {
        assertEquals("a\tb", run("'a\\tb'").asString());
    }

    @Test
    public void testStringEscapeBackslash() {
        assertEquals("a\\b", run("'a\\\\b'").asString());
    }

    @Test
    public void testStringEscapeDecimal() {
        assertEquals("a", run("'\\97'").asString());
    }

    @Test
    public void testStringEscapeHex() {
        assertEquals("A", run("'\\x41'").asString());
    }

    @Test
    public void testStringEscapeZSkipsWhitespace() {
        assertEquals("ab", run("'a\\z\nb'").asString());
    }

    // ===== P2 #8: GreaterThan / GreaterOrEqual 节点 =====

    @Test
    public void testGreaterThan() {
        assertTrue(run("10 > 5").asBoolean());
        assertFalse(run("5 > 10").asBoolean());
        assertFalse(run("5 > 5").asBoolean());
    }

    @Test
    public void testGreaterOrEqual() {
        assertTrue(run("10 >= 5").asBoolean());
        assertFalse(run("5 >= 10").asBoolean());
        assertTrue(run("5 >= 5").asBoolean());
    }

    @Test
    public void testGreaterThanDoubles() {
        assertTrue(run("10.5 > 10.0").asBoolean());
        assertTrue(run("10.0 >= 10.0").asBoolean());
    }

    @Test
    public void testGreaterThanStrings() {
        assertTrue(run("'b' > 'a'").asBoolean());
        assertFalse(run("'a' > 'b'").asBoolean());
    }

    // ===== P2 #19: LuaWriteArgumentNode DSL特化 =====
    // (间接测试: 函数参数传递应该正确工作)

    @Test
    public void testFunctionArgumentTypes() {
        String code = "function f(a, b, c)\n"
                + "    return a + b + c\n"
                + "end\n"
                + "return f(1, 2, 3)\n";
        assertEquals(6L, eval(code).asLong());
    }

    @Test
    public void testFunctionMixedArgTypes() {
        String code = "function f(a, b)\n"
                + "    return a + b\n"
                + "end\n"
                + "return f(1, 2.5)\n";
        assertEquals(3.5, eval(code).asDouble(), 0.001);
    }

    // ===== P0 #4 补充: 闭包内 upvalue 函数定义 =====

    @Test
    public void testFunctionDefAsUpvalueFromClosure() {
        String code = "function outer()\n"
                + "    local f\n"
                + "    function f()\n"
                + "        return 55\n"
                + "    end\n"
                + "    return f()\n"
                + "end\n"
                + "return outer()\n";
        assertEquals(55L, eval(code).asLong());
    }

    // ===== P0 #5 补充: 十六进制浮点数边界情况 =====

    @Test
    public void testHexFloatWithFractionAndP() {
        assertEquals(15.5, run("0xf.8p0").asDouble(), 0.001);
    }

    @Test
    public void testHexFloatFallbackToDouble() {
        assertEquals(255.5, run("0xff.8p0").asDouble(), 0.001);
    }

    @Test
    public void testLargeIntegerOverflowToDouble() {
        assertTrue(run("99999999999999999").isNumber());
    }

    // ===== P1 #1 补充: 验证函数体内 vararg 状态正确隔离 =====

    @Test
    public void testVarargIsolationAcrossFunctions() {
        String code = "function varfunc(...)\n"
                + "    return ...\n"
                + "end\n"
                + "function normalfunc(a)\n"
                + "    return a\n"
                + "end\n"
                + "local x = varfunc(1, 2, 3)\n"
                + "local y = normalfunc(10)\n"
                + "return y\n";
        assertEquals(10L, eval(code).asLong());
    }

    @Test
    public void testNestedVarargFunctions() {
        String code = """
                function outer(...)
                    function inner(...)
                        return ...
                    end
                    return inner(100)
                end
                return outer(1, 2, 3)
                """;
        assertEquals(100L, eval(code).asLong());
    }

    // ===== P1 #2 补充: Upvalue 类型正确传递 (修复 setSlotKind 操作错误 frame) =====

    @Test
    public void testUpvalueWriteDoubleFromNested() {
        String code = """
                local x = 1
                function level1()
                    function level2()
                        x = 3.14
                    end
                    level2()
                    return x
                end
                level1()
                return x
                """;
        assertEquals(3.14, eval(code).asDouble(), 0.001);
    }

    @Test
    public void testUpvalueWriteBoolFromNested() {
        String code = """
                local x = 1
                function level1()
                    function level2()
                        x = true
                    end
                    level2()
                end
                level1()
                return x
                """;
        assertTrue(eval(code).asBoolean());
    }

    // ===== P1 #10 补充: 方法调用 receiver 副作用验证 =====

    @Test
    public void testMethodCallReceiverNoDoubleEvaluation() {
        String code = """
                local callCount = 0
                local t = {}
                function t:method()
                    return self
                end
                local obj = t
                local result = obj:method()
                return result == t
                """;
        assertTrue(eval(code).asBoolean());
    }

    @Test
    public void testMethodCallReceiverExpressionEvaluatedOnce() {
        String code = """
                local callCount = 0
                local t = {}
                function t:method()
                    return callCount
                end
                function make()
                    callCount = callCount + 1
                    return t
                end
                return make():method()
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testMethodCallReceiverChain() {
        String code = """
                local t = {x = 5}
                function t:double()
                    return self.x * 2
                end
                function t:triple()
                    return self.x * 3
                end
                return t:double() + t:triple()
                """;
        assertEquals(25L, eval(code).asLong());
    }

    // ===== P1 #6 补充: 更多转义序列 =====

    @Test
    public void testStringEscapeCarriageReturn() {
        assertEquals("a\rb", run("'a\\rb'").asString());
    }

    @Test
    public void testStringEscapeBell() {
        assertEquals("\u0007", run("'\\a'").asString());
    }

    @Test
    public void testStringEscapeUnicode() {
        // 字节模型：字符串按原始字节存储（8-bit clean），Unicode 转义产生 UTF-8 字节
        // E4 B8 AD 共 3 字节。polyglot asString() 返回的是 Latin-1 字节模型字符串，
        // 与 Java 字符串"中"不可直接比较，因此断言原始字节。
        // （Java 源码中反斜杠与 u 相邻会触发 Unicode 转义预处理，故用字符拼接。）
        String escape = '\\' + "u{4e2d}";
        Value result = run("string.len('" + escape + "'), string.byte('" + escape + "', 1, -1)");
        assertEquals(3L, result.getArrayElement(0).asLong());
        assertEquals(0xE4, result.getArrayElement(1).asLong());
        assertEquals(0xB8, result.getArrayElement(2).asLong());
        assertEquals(0xAD, result.getArrayElement(3).asLong());
    }

    @Test
    public void testStringEscapeMixedDecimal() {
        assertEquals("AB", run("'\\65\\66'").asString());
    }

    @Test
    public void testStringEscapeHexTwoDigits() {
        // 标准 Lua：\xHH 产生单字节（低 8 位），与 string.char / \ddd 一致。
        // 官方测试套件 literals.lua 据此比较 \xff 与 string.char(255)/\255。
        // 因此 '\xff' 应为单字节 0xFF，且等于 string.char(255) 与 '\255'。
        Value result = run("string.byte('\\xff'), '\\xff' == string.char(255), '\\xff' == '\\255', string.len('\\xff')");
        assertEquals(255L, result.getArrayElement(0).asLong());
        assertTrue(result.getArrayElement(1).asBoolean());
        assertTrue(result.getArrayElement(2).asBoolean());
        assertEquals(1L, result.getArrayElement(3).asLong());
    }

    // ===== P2 #8 补充: GreaterOrEqual 边界 =====

    @Test
    public void testGreaterOrEqualMixedTypes() {
        assertTrue(run("5 >= 5.0").asBoolean());
        assertTrue(run("5.0 >= 5").asBoolean());
        assertFalse(run("4.9 >= 5").asBoolean());
    }

    // ===== P2 #13 补充: while/repeat/if SourceSection (通过执行间接验证) =====

    @Test
    public void testWhileSourceSectionNotCrash() {
        String code = """
                local i = 0
                while i < 3 do
                    i = i + 1
                end
                return i
                """;
        assertEquals(3L, eval(code).asLong());
    }

    @Test
    public void testRepeatSourceSectionNotCrash() {
        String code = """
                local i = 0
                repeat
                    i = i + 1
                until i >= 3
                return i
                """;
        assertEquals(3L, eval(code).asLong());
    }

    @Test
    public void testIfElseifElseSourceSectionNotCrash() {
        String code = """
                local x = 2
                if x == 1 then
                    return 10
                elseif x == 2 then
                    return 20
                else
                    return 30
                end
                """;
        assertEquals(20L, eval(code).asLong());
    }

    // ===== P2 #19 补充: WriteArgumentNode bool参数 =====

    @Test
    public void testFunctionBoolArgument() {
        String code = """
                function f(flag)
                    if flag then return 1 else return 0 end
                end
                return f(true)
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testFunctionStringArgument() {
        String code = """
                function f(s)
                    return s
                end
                return f('hello')
                """;
        assertEquals("hello", eval(code).asString());
    }

    @Test
    public void testFunctionNilArgument() {
        String code = """
                function f(x)
                    return x == nil
                end
                return f(nil)
                """;
        assertTrue(eval(code).asBoolean());
    }

    // ===== 综合测试 =====

    @Test
    public void testVarargsInMain() {
        String code = """
                local a, b = ...
                return a == nil and b == nil
                """;
        assertTrue(eval(code).asBoolean());
    }

    @Test
    public void testVarargsInFunction() {
        String code = """
                function f(...)
                    return ...
                end
                return f(42)
                """;
        assertEquals(42L, eval(code).asLong());
    }

    @Test
    public void testVarargFunctionCall() {
        String code = """
                function f(a, b, ...)
                    return a + b
                end
                return f(10, 20, 30)
                """;
        assertEquals(30L, eval(code).asLong());
    }

    @Test
    public void testGenericForWithPlainExplistValues() {
        String code = """
                local t = {a = 1, b = 2}
                local seen = 0
                for k, v in next, t, nil do
                    seen = seen + v
                end
                return seen
                """;
        assertEquals(3L, eval(code).asLong());
    }

    @Test
    public void testJavaObjectArrayRemainsSingleValue() {
        context.eval("lua", "return _G").putMember("javaArray", new Object[]{"a", "b"});

        Value result = eval("return javaArray");

        assertTrue(result.hasArrayElements());
        assertEquals(2, result.getArraySize());
        assertEquals("a", result.getArrayElement(0).asString());
        assertEquals("b", result.getArrayElement(1).asString());
    }

    @Test
    public void testJavaObjectArrayCanBeNonTrailingArgument() {
        context.eval("lua", "return _G").putMember("javaArray", new Object[]{"a", "b"});

        Value result = eval("return (function(value, suffix) return value end)(javaArray, 1)");

        assertTrue(result.hasArrayElements());
        assertEquals(2, result.getArraySize());
        assertEquals("a", result.getArrayElement(0).asString());
        assertEquals("b", result.getArrayElement(1).asString());
    }

    @Test
    public void testJavaObjectArrayCanBeTrailingArgument() {
        context.eval("lua", "return _G").putMember("javaArray", new Object[]{"a", "b"});

        Value result = eval("return (function(prefix, value) return value end)(1, javaArray)");

        assertTrue(result.hasArrayElements());
        assertEquals(2, result.getArraySize());
        assertEquals("a", result.getArrayElement(0).asString());
        assertEquals("b", result.getArrayElement(1).asString());
    }

    @Test
    public void testJavaObjectArraySurvivesMultiAssignment() {
        context.eval("lua", "return _G").putMember("javaArray", new Object[]{"a", "b"});

        Value result = eval("local value, other = javaArray, 1; return value");

        assertTrue(result.hasArrayElements());
        assertEquals(2, result.getArraySize());
        assertEquals("a", result.getArrayElement(0).asString());
        assertEquals("b", result.getArrayElement(1).asString());
    }

    @Test
    public void testJavaPrimitiveArrayRemainsSingleValue() {
        context.eval("lua", "return _G").putMember("javaArray", new int[]{1, 2});

        Value result = eval("return javaArray");

        assertTrue(result.hasArrayElements());
        assertEquals(2, result.getArraySize());
        assertEquals(1, result.getArrayElement(0).asInt());
        assertEquals(2, result.getArrayElement(1).asInt());
    }

    @Test
    public void testStringFindStillExposesLuaMultiValue() {
        Value result = eval("return string.find('abc', 'b')");

        assertTrue(result.hasArrayElements());
        assertEquals(2, result.getArraySize());
        assertEquals(2L, result.getArrayElement(0).asLong());
        assertEquals(2L, result.getArrayElement(1).asLong());
    }

    @Test
    public void testFunctionMultiReturnCollapsesInScalarExpression() {
        String code = """
                function values()
                    return 2, 99
                end
                return values() + 3
                """;
        assertEquals(5L, eval(code).asLong());
        assertTrue(eval("return string.find('abc', 'a') == 1").asBoolean());
    }

    @Test
    public void testMethodMultiReturnCollapsesInScalarExpression() {
        String code = """
                local object = {}
                function object:values()
                    return 4, 99
                end
                return object:values() * 2
                """;
        assertEquals(8L, eval(code).asLong());
    }

    @Test
    public void testVarargCollapsesInScalarExpression() {
        String code = """
                function firstPlusOne(...)
                    return ... + 1
                end
                return firstPlusOne(5, 99)
                """;
        assertEquals(6L, eval(code).asLong());
    }

    @Test
    public void testTableConstructorExpandsOnlyFinalListField() {
        String code = """
                function values()
                    return 1, 2, 3
                end
                local expanded = {0, values()}
                local collapsed = {values(), 9}
                local followedByKey = {values(), name = 'x'}
                return expanded[1] == 0
                    and expanded[2] == 1
                    and expanded[3] == 2
                    and expanded[4] == 3
                    and collapsed[1] == 1
                    and collapsed[2] == 9
                    and collapsed[3] == nil
                    and followedByKey[1] == 1
                    and followedByKey[2] == nil
                """;
        assertTrue(eval(code).asBoolean());
    }

    @Test
    public void testMultiReturnExpandsOnlyAtTrailingExpression() {
        String code = """
                function f()
                    return 1, 2, 3
                end
                local a, b, c = f()
                local x, y, z = 0, f()
                local first, last = f(), 9
                return a + b + c + x + y + z + first + last
                """;
        assertEquals(19L, eval(code).asLong());
    }

    @Test
    public void testMultiReturnThroughPcallAndXpcall() {
        String code = """
                function f()
                    return 2, 3
                end
                local ok1, a, b = pcall(f)
                local ok2, c, d = xpcall(f, function(message) return message end)
                return (ok1 and ok2) and a + b + c + d
                """;
        assertEquals(10L, eval(code).asLong());
    }

    @Test
    public void testMultiReturnMethodCall() {
        String code = """
                local object = {}
                function object:values()
                    return 4, 5
                end
                local a, b = object:values()
                return a + b
                """;
        assertEquals(9L, eval(code).asLong());
    }

    @Test
    public void testEmptyReturnFillsMissingAssignmentValues() {
        String code = """
                function f()
                    return
                end
                local a, b = f()
                return a == nil and b == nil
                """;
        assertTrue(eval(code).asBoolean());
    }

    @Test
    public void testVarargMultiReturnExpandsAtTrailingExpression() {
        String code = """
                function f(...)
                    return ...
                end
                local a, b, c = f(6, 7, 8)
                local x, y = 0, f(9, 10)
                return a + b + c + x + y
                """;
        assertEquals(30L, eval(code).asLong());
    }

    // ===== P0 #3 补充: 冒号定义带点链 =====

    @Test
    public void testFuncNameColonWithDotChain() {
        String code = """
                local a = {}
                a.b = {}
                function a.b:method()
                    return self.x
                end
                a.b.x = 7
                return a.b:method()
                """;
        assertEquals(7L, eval(code).asLong());
    }

    @Test
    public void testLocalInitializerResolvesOuterScope() {
        String code = """
                local x = 41
                do
                    local x = x + 1
                    return x
                end
                """;
        assertEquals(42L, eval(code).asLong());
    }

    @Test
    public void testIfLocalDoesNotLeak() {
        String code = """
                local x = 1
                if true then
                    local x = 2
                end
                return x
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testWhileLocalDoesNotLeak() {
        String code = """
                local x = 1
                while false do
                    local x = 2
                end
                return x
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testNumericForInitializersResolveOuterScope() {
        String code = """
                local i = 3
                local sum = 0
                for i = i, i do
                    sum = sum + i
                end
                return sum
                """;
        assertEquals(3L, eval(code).asLong());
    }

    // ===== P1 #6 补充: \\v 和 \\f 转义 =====

    @Test
    public void testStringEscapeVerticalTab() {
        assertEquals("\u000B", run("'\\v'").asString());
    }

    @Test
    public void testStringEscapeFormFeed() {
        assertEquals("\u000C", run("'\\f'").asString());
    }

    @Test
    public void testLongStringDoesNotUnescape() {
        assertEquals("a\\nb", run("[[a\\nb]]").asString());
    }

    @Test
    public void testLongStringWithEqualsDelimiter() {
        assertEquals("a [[ nested ]] b", run("[=[a [[ nested ]] b]=]").asString());
    }

    @Test
    public void testLongStringSkipsInitialNewline() {
        assertEquals("hello", run("[[\nhello]]").asString());
    }

    @Test
    public void testParenthesizedCallCollapsesMultiReturn() {
        String code = """
                function values()
                    return 1, 2
                end
                local a, b = (values())
                return a == 1 and b == nil
                """;
        assertTrue(eval(code).asBoolean());
    }

    @Test
    public void testAnonymousFunctionDebugNameDoesNotReadPastVarlist() {
        String code = """
                local f
                f = 1, function() return 2 end
                return f
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testGotoForward() {
        String code = """
                local x = 1
                goto done
                x = 2
                ::done::
                return x
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testGotoBackward() {
        String code = """
                local i = 0
                ::again::
                i = i + 1
                if i < 3 then
                    goto again
                end
                return i
                """;
        assertEquals(3L, eval(code).asLong());
    }

    @Test
    public void testGotoResolvesNearestVisibleLabel() {
        String code = """
                local result = 0
                local first = true
                ::again::
                result = result + 100
                do
                    ::again::
                    if first then
                        first = false
                        goto again
                    end
                    result = result + 1
                end
                return result
                """;
        assertEquals(101L, eval(code).asLong());
    }

    @Test
    public void testGotoUndefinedLabelFails() {
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval("goto missing"));
        assertTrue(ex.getMessage().contains("no visible label"));
    }

    @Test
    public void testDuplicateLabelFails() {
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval("::a:: ::a::"));
        assertTrue(ex.getMessage().contains("already defined"));
    }

    @Test
    public void testSiblingBlocksMayReuseLabelName() {
        String code = """
                do
                    ::again::
                end
                do
                    ::again::
                end
                return 1
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testBreakOutsideLoopFails() {
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval("break"));
        assertTrue(ex.getMessage().contains("break outside loop"));
    }

    @Test
    public void testConstLocalCannotBeReassigned() {
        String code = """
                local x <const> = 1
                x = 2
                return x
                """;
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval(code));
        assertTrue(ex.getMessage().contains("attempt to assign to const variable 'x'"), ex.getMessage());

        // 标准 Lua 在编译期（load）就拒绝，而不是运行到赋值语句才报错
        Value result = eval("""
                local f, e = load('local x <const> = 1; x = 2; return x')
                return f == nil, tostring(e)
                """);
        assertTrue(result.getArrayElement(0).asBoolean());
        assertTrue(result.getArrayElement(1).asString().contains("attempt to assign to const variable 'x'"));
    }

    @Test
    public void testCloseLocalCannotBeReassigned() {
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval("""
                local x <close> = setmetatable({}, {__close = function() end})
                x = 2
                """));
        assertTrue(ex.getMessage().contains("attempt to assign to const variable 'x'"), ex.getMessage());
    }

    @Test
    public void testGotoIntoNestedBlockFails() {
        String code = """
                goto inside
                do
                    ::inside::
                end
                """;
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval(code));
        assertTrue(ex.getMessage().contains("no visible label"));
    }

    @Test
    public void testGotoSkipsLocalDeclarationFails() {
        String code = """
                goto done
                local x = 1
                ::done::
                return 0
                """;
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval(code));
        assertTrue(ex.getMessage().contains("jumps into the scope of local"));
    }

    @Test
    public void testGotoOutOfNestedBlockClosesVariable() {
        String code = """
                local closed = 0
                local mt = { __close = function(v, err) closed = closed + 1 end }
                do
                    local x <close> = setmetatable({}, mt)
                    goto done
                end
                ::done::
                return closed
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testGotoOutOfNestedBlockClosesVariablesInReverseDeclarationOrder() {
        String code = """
                local order = ''
                local mt = { __close = function(v, err) order = order .. v.name end }
                do
                    local a <close> = setmetatable({name = 'a'}, mt)
                    local b <close> = setmetatable({name = 'b'}, mt)
                    goto done
                end
                ::done::
                return order
                """;
        assertEquals("ba", eval(code).asString());
    }

    @Test
    public void testCloseRunsOnNormalScopeExit() {
        String code = """
                local closed = 0
                local mt = { __close = function(v, err) closed = closed + 1 end }
                do
                    local x <close> = setmetatable({}, mt)
                end
                return closed
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testCloseRunsOnReturn() {
        String code = """
                local closed = 0
                local mt = { __close = function(v, err) closed = closed + 1 end }
                function f()
                    local x <close> = setmetatable({}, mt)
                    return 7
                end
                local result = f()
                return result + closed
                """;
        assertEquals(8L, eval(code).asLong());
    }

    @Test
    public void testCloseRunsOnErrorBeforePcallReturns() {
        String code = """
                local closed = 0
                local mt = { __close = function(v, err) closed = closed + 1 end }
                local ok = pcall(function()
                    local x <close> = setmetatable({}, mt)
                    error('boom')
                end)
                return (not ok) and closed
                """;
        assertEquals(1L, eval(code).asLong());
    }

    @Test
    public void testMultipleCloseAttributesInLocalDeclarationFails() {
        String code = """
                local a <close>, b <close> = nil, nil
                """;
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval(code));
        assertTrue(ex.getMessage().contains("multiple to-be-closed"));
    }

    @Test
    public void testInvalidStringEscapeReportsParseError() {
        PolyglotException ex = assertThrows(PolyglotException.class, () -> run("'\\256'"));
        assertTrue(ex.getMessage().contains("decimal escape too large"));
    }

    @Test
    public void testUtf8BomAtSourceStart() {
        assertEquals(42L, eval("\uFEFFreturn 42").asLong());
    }

    @Test
    public void testIntegerArithmeticWraps() {
        assertTrue(eval("""
                local max = 0x7fffffffffffffff
                local min = -0x7fffffffffffffff - 1
                return max + 1 == min
                    and min - 1 == max
                    and max * 2 == -2
                    and -min == min
                """).asBoolean());
    }

    @Test
    public void testNumericForSupportsFloatingPointValues() {
        assertEquals(5L, eval("""
                local count = 0
                for i = 0.5, 2.5, 0.5 do
                    count = count + 1
                end
                return count
                """).asLong());
    }

    @Test
    public void testMultipleAssignmentEvaluatesTableTargetBeforeWrites() {
        assertTrue(eval("""
                local i = 1
                local values = {}
                i, values[i] = 2, 10
                return i == 2 and values[1] == 10 and values[2] == nil
                """).asBoolean());
    }

    @Test
    public void testCallMetamethod() {
        assertEquals(5L, eval("""
                local value = setmetatable({}, {
                    __call = function(self, argument)
                        return argument + 1
                    end
                })
                return value(4)
                """).asLong());
    }

    @Test
    public void testRelationalMetamethodOperandOrderAndFallback() {
        assertTrue(eval("""
                local mt = {
                    __lt = function(a, b) return a.value < b.value end
                }
                local a = setmetatable({value = 1}, mt)
                local b = setmetatable({value = 2}, mt)
                return not (a > b) and b > a and a <= b and b >= a
                """).asBoolean());
    }

    @Test
    public void testEqualityUsesEitherMetamethod() {
        // Lua 5.3 luaV_equalobj：优先取左值的 __eq，缺失时再取右值的 __eq。
        assertTrue(eval("""
                local a = setmetatable({}, {__eq = function() return true end})
                local b = setmetatable({}, {__eq = function() return false end})
                return a == b
                """).asBoolean());
        assertTrue(eval("""
                local a = {}
                local b = setmetatable({}, {__eq = function() return true end})
                return a == b
                """).asBoolean());
    }

    @Test
    public void testStringLengthCountsUtf8Bytes() {
        // 字节模型：非 ASCII 字符按其 UTF-8 字节序列计数（Unicode 转义 e9 产生 C3 A9 两个字节）。
        String escape = '\\' + "u{e9}";
        assertEquals(2L, eval("return #('" + escape + "')").asLong());
    }

    @Test
    public void testChineseLiteralPreservesUtf8Bytes() {
        // 回归：中文字面量必须无损保留 UTF-8 字节（此前被 Latin-1 转换压成 '?'）。
        // Java 源字符串中的中文字符（>0xFF）按 UTF-8 编码为字节存储。
        assertEquals(6L, eval("return #'中文'").asLong());
        // 尾随位置的调用展开为全部字节值
        Value bytes = eval("return string.byte('中', 1, -1)");
        assertEquals(0xE4, bytes.getArrayElement(0).asLong());
        assertEquals(0xB8, bytes.getArrayElement(1).asLong());
        assertEquals(0xAD, bytes.getArrayElement(2).asLong());
        assertTrue(eval("return '中文' == string.char(228, 184, 173, 230, 150, 135)").asBoolean());
    }

    @Test
    public void testExplicitIntegerTableFieldsUseArrayPart() {
        assertTrue(eval("""
                local values = {[1] = 'a', [2] = 'b'}
                local count = 0
                for i, value in ipairs(values) do
                    count = count + 1
                end
                return #values == 2 and count == 2
                """).asBoolean());
    }

    @Test
    public void testContinueInNumericAndGenericFor() {
        assertTrue(eval("""
                local numeric = 0
                for i = 1, 3 do
                    if i == 2 then continue end
                    numeric = numeric + i
                end

                local generic = 0
                for i, value in ipairs({1, 2, 3}) do
                    if i == 2 then continue end
                    generic = generic + value
                end
                return numeric == 4 and generic == 4
                """).asBoolean());
    }

    @Test
    public void testUnaryMetamethodReceivesOneArgument() {
        assertEquals(1L, eval("""
                local value = setmetatable({}, {
                    __unm = function(...)
                        return select('#', ...)
                    end
                })
                return -value
                """).asLong());
    }

    @Test
    public void testIpairsRejectsNonTable() {
        PolyglotException ex = assertThrows(PolyglotException.class, () -> eval("return ipairs(42)"));
        assertTrue(ex.getMessage().contains("table expected"));
    }

    @Test
    public void testDirectTableFunctionAssignmentStillWrites() {
        assertTrue(eval("""
                local t = {}
                local f = function() return 1 end
                t.value = f
                return t.value == f
                """).asBoolean());
    }

    @Test
    public void testToNumberUsesLuaNumberSyntax() {
        assertTrue(eval("""
                return tonumber('-0x10') == -16
                    and tonumber('+0x10') == 16
                    and tonumber('0x1.8') == 1.5
                    and tonumber('0x10000000000000000') == 0
                    and tonumber('  \t123\\n') == 123
                """).asBoolean());
    }

    @Test
    public void testToNumberRejectsJavaOnlyNumberSyntax() {
        assertTrue(eval("""
                return tonumber('NaN') == nil
                    and tonumber('Infinity') == nil
                    and tonumber('-Infinity') == nil
                    and tonumber('1d') == nil
                    and tonumber('1f') == nil
                    and tonumber('١') == nil
                    and tonumber(string.char(1) .. '123') == nil
                """).asBoolean());
    }

    @Test
    public void testToNumberAcceptsNilButRequiresAnArgument() {
        assertTrue(eval("""
                local ok, err = pcall(tonumber)
                return tonumber(nil) == nil
                    and ok == false
                    and err:find('value expected') ~= nil
                    and tonumber('123') == 123
                """).asBoolean());
    }

    @Test
    public void testToNumberWithBaseRequiresString() {
        assertTrue(eval("""
                local ok1, err1 = pcall(tonumber, nil, 10)
                local ok2, err2 = pcall(tonumber, 123, 10)
                return ok1 == false and err1:find('string expected, got nil') ~= nil
                    and ok2 == false and err2:find('string expected, got number') ~= nil
                """).asBoolean());
    }

    @Test
    public void testToNumberBaseTypeCoercionMatchesLua() {
        assertTrue(eval("""
                local ok, err = pcall(tonumber, '10', '-2')
                return tonumber('10', nil) == 10
                    and tonumber('10', '2') == 2
                    and tonumber('10', 2.0) == 2
                    and tonumber('2', '  10  ') == 2
                    and ok == false and err:find('base out of range') ~= nil
                """).asBoolean());
    }

    @Test
    public void testToNumberBaseTypeErrorsMatchLua() {
        assertTrue(eval("""
                local ok1, e1 = pcall(tonumber, '10', 2.5)
                local ok2, e2 = pcall(tonumber, '10', {})
                local ok3, e3 = pcall(tonumber, '10', 'abc')
                local ok4, e4 = pcall(tonumber, 123, {})
                local ok5, e5 = pcall(tonumber, 123, 1)
                return ok1 == false and e1:find('integer representation') ~= nil
                    and ok2 == false and e2:find('number expected, got table') ~= nil
                    and ok3 == false and e3:find('number expected, got string') ~= nil
                    and ok4 == false and e4:find('number expected, got table') ~= nil
                    and ok5 == false and e5:find('string expected, got number') ~= nil
                """).asBoolean());
    }

    @Test
    public void testVarargsNotLeakedToFunctionsCalledWithTooFewArgs() {
        // 模拟 launcher 的 arguments 布局（launcher、脚本路径、脚本参数）：
        // 固定参数不足的函数其 ... 必须为空，不得回退读取 arg 表
        try (var context = newContextBuilder("lua").arguments("lua",
                new String[]{"lua", "script.lua", "subId=20000", "force=92"}).build()) {
            Value result = context.eval("lua", """
                    local forward = function(subId, force, ...)
                        return select('#', ...)
                    end
                    local chain = function(...)
                        return (function(...) return select('#', ...) end)(...)
                    end
                    return forward(), chain()
                    """);
            assertEquals(0L, result.getArrayElement(0).asLong());
            assertEquals(0L, result.getArrayElement(1).asLong());
        }
    }

    @Test
    public void testMainChunkVarargsReflectArgTableModifications() {
        // main chunk 的 ... 来自 arg 表（lua.c pushargs 语义）：-e 修改 arg[1] 后可见
        try (var context = newContextBuilder("lua").arguments("lua",
                new String[]{"lua", "script.lua", "a", "b"}).build()) {
            context.eval("lua", "arg[1] = 'MODIFIED'");
            Value result = context.eval("lua", "return select('#', ...), (select(1, ...))");
            assertEquals(2L, result.getArrayElement(0).asLong());
            assertEquals("MODIFIED", result.getArrayElement(1).asString());
        }
    }

    @Test
    public void testArithmeticStringCoercionProducesFloat() {
        assertEquals("3.0", eval("return tostring('1' + 2)").asString());
        assertEquals("-2.0", eval("return tostring(-'2')").asString());
        assertEquals("1", eval("return tostring(tonumber('1'))").asString());
    }

    @Test
    public void testNumericForWithFloatLimitKeepsIntegerControlVariable() {
        assertTrue(eval("""
                local count = 0
                local last
                for i = 1, 3.5, 1 do
                    count = count + 1
                    last = tostring(i)
                end
                return count == 3 and last == '3'
                """).asBoolean());
    }

    @Test
    public void testNumericForStopsBeforeIntegerOverflow() {
        assertEquals(1L, eval("""
                local count = 0
                for i = 0x7fffffffffffffff, 0x1p63, 1 do
                    count = count + 1
                end
                return count
                """).asLong());
    }

    @Test
    public void testNumericForWithNaNControlValueDoesNotIterate() {
        assertEquals(0L, eval("""
                local count = 0
                for i = 0 / 0, 1, 1 do
                    count = count + 1
                end
                for i = 1, 3, 0 / 0 do
                    count = count + 1
                end
                return count
                """).asLong());
    }

    @Test
    public void testLengthProducesLuaIntegerForNumericFor() {
        assertEquals(10L, eval("""
                local function varargSum(...)
                    local args = {...}
                    local sum = 0
                    for i = 1, #args do
                        sum = sum + args[i]
                    end
                    return sum
                end
                return varargSum(1, 2, 3, 4)
                """).asLong());
    }
}
