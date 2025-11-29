package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LuaExpressionTest extends AbstractLuaTest {

    private Context context;

    @BeforeEach
    public void setUp() {
        // 创建一个新的上下文，并指定允许访问我们的 "lua" 语言
        // 根据你的具体实现，可能需要 .allowAllAccess(true) 或其他选项
        this.context = Context.create("lua");
    }

    @AfterEach
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    /**
     * 辅助方法，用于执行Lua代码并返回结果
     * @param code 要执行的Lua代码字符串，必须是一个返回值的表达式
     * @return 执行结果，封装在Truffle的Value对象中
     */
    protected Value run(String code) {
        // 我们通过 "return " 来确保代码块是一个返回值的表达式
        return context.eval("lua", "return " + code);
    }

    // --- 1. 基础字面量测试 (Literals) ---

    @Test
    public void testAtoms() {
        // Nil
        assertTrue(run("nil").isNull(), "nil should be null");

        // Booleans
        Value trueVal = run("true");
        assertTrue(trueVal.isBoolean());
        assertTrue(trueVal.asBoolean());

        Value falseVal = run("false");
        assertTrue(falseVal.isBoolean());
        assertFalse(falseVal.asBoolean());

        // Strings
        Value strVal = run("'hello world'");
        assertTrue(strVal.isString());
        assertEquals("hello world", strVal.asString());
    }

    // --- 2. 数值类型测试 (Numbers) ---

    @Test
    public void testNumbers() {
        // Integer (Long)
        Value intVal = run("12345");
        assertTrue(intVal.isNumber());
        assertTrue(intVal.fitsInLong());
        assertEquals(12345L, intVal.asLong());

        // Float (Double)
        Value floatVal = run("123.45");
        assertTrue(floatVal.isNumber());
        assertEquals(123.45, floatVal.asDouble(), 0.0001);

        // Scientific notation
        Value sciVal = run("1.2e3");
        assertEquals(1200.0, sciVal.asDouble(), 0.0001);

        // Hex
        Value hexVal = run("0xFF");
        assertEquals(255L, hexVal.asLong());
    }

    // --- 3. 算术运算测试 (Arithmetic) ---

    @Test
    public void testArithmetic() {
        // 加法
        assertEquals(30L, run("10 + 20").asLong());
        assertEquals(30.5, run("10.0 + 20.5").asDouble(), 0.001);

        // 减法
        assertEquals(5L, run("10 - 5").asLong());
        assertEquals(-5L, run("5 - 10").asLong());

        // 乘法
        assertEquals(20L, run("4 * 5").asLong());

        // 除法 (Lua 的 / 总是返回浮点数)
        assertEquals(2.5, run("5 / 2").asDouble(), 0.001);
        assertEquals(2.0, run("4 / 2").asDouble(), 0.001); // 即使整除也是 double

        // 整除 (//)
        assertEquals(2L, run("5 // 2").asLong());
        assertEquals(2.0, run("5.5 // 2").asDouble(), 0.001); // 浮点数整除返回浮点数

        // 取模 (%)
        assertEquals(1L, run("10 % 3").asLong());

        // 幂运算 (^)
        assertEquals(8.0, run("2 ^ 3").asDouble(), 0.001);

        // 一元负号 (-)
        assertEquals(-10L, run("-10").asLong());
    }

    // --- 4. 位运算测试 (Bitwise - Lua 5.3+) ---

    @Test
    public void testBitwise() {
        // And
        assertEquals(1L, run("3 & 5").asLong()); // 011 & 101 = 001

        // Or
        assertEquals(7L, run("3 | 5").asLong()); // 011 | 101 = 111

        // Xor
        assertEquals(6L, run("3 ~ 5").asLong()); // 011 ^ 101 = 110

        // Shift
        assertEquals(8L, run("2 << 2").asLong());
        assertEquals(2L, run("8 >> 2").asLong());

        // Unary Not
        // ~0 = -1 (in 2's complement)
        assertEquals(-1L, run("~0").asLong());
    }

    // --- 5. 关系运算测试 (Relational) ---

    @Test
    public void testComparison() {
        // Equality
        assertTrue(run("1 == 1").asBoolean());
        assertFalse(run("1 == 2").asBoolean());
        assertFalse(run("1 == '1'").asBoolean()); // 类型不同不相等

        // Inequality
        assertTrue(run("1 ~= 2").asBoolean());
        assertFalse(run("1 ~= 1").asBoolean());

        // Order
        assertTrue(run("1 < 2").asBoolean());
        assertTrue(run("2 > 1").asBoolean());
        assertTrue(run("1 <= 1").asBoolean());
        assertTrue(run("1 >= 1").asBoolean());

        // String comparison (dictionary order)
        assertTrue(run("'a' < 'b'").asBoolean());
    }

    // --- 6. 逻辑运算测试 (Logical) ---

    @Test
    public void testLogical() {
        // And (返回第一个 falsy 或最后一个值)
        assertEquals(20L, run("10 and 20").asLong());
        assertTrue(run("nil and 20").isNull());

        // Or (返回第一个 truthy 或最后一个值)
        assertEquals(10L, run("10 or 20").asLong());
        assertEquals(20L, run("nil or 20").asLong());
        assertEquals(20L, run("false or 20").asLong());

        // Not (总是返回 boolean)
        assertTrue(run("not false").asBoolean());
        assertTrue(run("not nil").asBoolean());
        assertFalse(run("not true").asBoolean());
        assertFalse(run("not 0").asBoolean()); // Lua 中 0 是真值
    }

    // --- 7. 字符串操作测试 ---

    @Test
    public void testStringOperations() {
        // Concatenation
        assertEquals("hello world", run("'hello' .. ' ' .. 'world'").asString());

        // Coercion (数字转字符串)
        assertEquals("10001", run("1000 .. 1").asString());

        // Length operator (#)
        assertEquals(5L, run("#'hello'").asLong());
    }

    // --- 8. 运算符优先级测试 (Precedence) ---

    @Test
    public void testPrecedence() {
        // * 优先于 +
        assertEquals(7L, run("1 + 2 * 3").asLong());

        // 括号改变优先级
        assertEquals(9L, run("(1 + 2) * 3").asLong());

        // 幂运算右结合
        // 2 ^ 3 ^ 2 = 2 ^ 9 = 512 (不是 8^2 = 64)
        assertEquals(512.0, run("2 ^ 3 ^ 2").asDouble(), 0.001);

        // 连接符右结合
        // 虽然结果一样，但结合性很重要
        assertEquals("abc", run("'a' .. 'b' .. 'c'").asString());
    }

    // --- 9. 表构造器测试 (Table Constructor) ---

    @Test
    public void testTableConstructor() {
        // 空表
        Value empty = run("{}");
        assertTrue(empty.hasMembers());

        // 列表式 (Array-like)
        Value list = run("{10, 20, 30}");
        // 注意：具体访问方式取决于你是否实现了 InteropLibrary 的 Array 消息
        // 如果按标准 Lua table 实现：
        assertEquals(10L, list.getArrayElement(0).asLong()); // 或者 list.getArrayElement(0) 如果你支持 0-based array interop

        // 记录式 (Map-like)
        Value map = run("{a=1, b=2}");
        assertEquals(1L, map.getMember("a").asLong());
        assertEquals(2L, map.getMember("b").asLong());

        // 混合式
        // {10, a=1} -> t[1]=10, t.a=1
        Value mixed = run("{10, a=1}");
        assertEquals(10L, mixed.getArrayElement(0).asLong());
        assertEquals(1L, mixed.getMember("a").asLong());
    }
}