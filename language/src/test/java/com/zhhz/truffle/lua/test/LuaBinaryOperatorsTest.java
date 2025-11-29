package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LuaBinaryOperatorsTest  extends AbstractLuaTest {
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

    @Test
    public void testAddition() {
        // 整数相加
        assertEquals(15.0, runLua("10 + 5").asDouble());
        // 浮点数相加
        assertEquals(15.5, runLua("10.5 + 5").asDouble());
        // 字符串自动转换为数字
        assertEquals(25.0, runLua("'10' + '15'").asDouble());
        // 混合类型
        assertEquals(25.5, runLua("10.5 + '15'").asDouble());

        // 测试错误情况
        PolyglotException e = assertThrows(PolyglotException.class, () -> runLua("10 + nil"));
        assertTrue(e.getMessage().contains("attempt to perform arithmetic"));
    }

    @Test
    public void testSubtraction() {
        assertEquals(5.0, runLua("10 - 5").asDouble());
        assertEquals(5.5, runLua("10.5 - 5").asDouble());
        assertEquals(-5.0, runLua("'10' - '15'").asDouble());
    }

    @Test
    public void testMultiplication() {
        assertEquals(50.0, runLua("10 * 5").asDouble());
        assertEquals(52.5, runLua("10.5 * 5").asDouble());
        assertEquals(150.0, runLua("'10' * '15'").asDouble());
    }

    @Test
    public void testDivision() {
        // 在Lua中，'/' 总是执行浮点除法
        assertEquals(2.0, runLua("10 / 5").asDouble());
        assertEquals(2.5, runLua("12.5 / 5").asDouble());
        assertEquals(0.5, runLua("'5' / '10'").asDouble());

        // 测试除以零，应得到 Infinity
        assertTrue(runLua("1 / 0").isNumber() && Double.isInfinite(runLua("1 / 0").asDouble()));
        assertTrue(runLua("-1 / 0").isNumber() && Double.isInfinite(runLua("-1 / 0").asDouble()));
        // 测试 0/0，应得到 NaN (Not a Number)
        assertTrue(runLua("0 / 0").isNumber() && Double.isNaN(runLua("0 / 0").asDouble()));
    }

    @Test
    public void testRelationalOperators() {
        // 小于 <
        assertTrue(runLua("5 < 10").asBoolean());
        assertFalse(runLua("10 < 5").asBoolean());
        assertTrue(runLua("'apple' < 'banana'").asBoolean());

        // 大于 >
        assertTrue(runLua("10 > 5").asBoolean());
        assertFalse(runLua("5 > 10").asBoolean());

        // 小于等于 <=
        assertTrue(runLua("5 <= 5").asBoolean());
        assertTrue(runLua("5 <= 10").asBoolean());
        assertTrue(runLua("'a' <= 'a'").asBoolean());

        // 测试错误情况：不同类型比较
        assertThrows(PolyglotException.class, () -> runLua("10 < '5'"));
    }

    @Test
    public void testEqualityOperators() {
        // 等于 ==
        assertTrue(runLua("5 == 5.0").asBoolean()); // 数字类型内部比较
        assertFalse(runLua("5 == '5'").asBoolean()); // Lua中，数字和字符串不相等
        assertTrue(runLua("'hello' == 'hello'").asBoolean());
        assertTrue(runLua("nil == nil").asBoolean());
        assertFalse(runLua("nil == false").asBoolean());
        assertFalse(runLua("0 == nil").asBoolean());

        // 不等于 ~=
        assertTrue(runLua("5 ~= 6").asBoolean());
        assertTrue(runLua("5 ~= '5'").asBoolean());
        assertTrue(runLua("nil ~= false").asBoolean());

        // 特殊情况：NaN 不等于任何东西，包括它自己
        assertTrue(runLua("(0/0) ~= (0/0)").asBoolean());
        assertFalse(runLua("(0/0) == (0/0)").asBoolean());
    }

    @Test
    public void testLogicalAnd() {
        // and 返回第一个 "falsy" (false 或 nil) 操作数，否则返回第二个操作数
        assertFalse(runLua("false and 10").asBoolean());
        assertTrue(runLua("nil and 10").isNull());
        assertEquals(10, runLua("true and 10").asInt());
        assertEquals("hello", runLua("5 and 'hello'").asString());

        // 测试短路行为
        // 如果 `false and error()` 不短路，它会抛出异常
        assertDoesNotThrow(() -> {
            assertFalse(runLua("false and error('should not be called')").asBoolean());
        });
    }

    @Test
    public void testLogicalOr() {
        // or 返回第一个 "truthy" 操作数，否则返回第二个操作数
        assertEquals(10, runLua("10 or false").asInt());
        assertTrue(runLua("true or 10").asBoolean());
        assertEquals(10, runLua("false or 10").asInt());
        assertEquals("hello", runLua("nil or 'hello'").asString());
        assertTrue(runLua("false or nil").isNull()); // 假设 runLua("nil") 返回 null 或一个代表 nil 的 Value

        // 测试短路行为
        assertDoesNotThrow(() -> {
            assertEquals(123, runLua("123 or error('should not be called')").asInt());
        });
    }

    /**
     * 辅助方法，用于执行Lua代码并返回结果
     * @param code 要执行的Lua代码字符串，必须是一个返回值的表达式
     * @return 执行结果，封装在Truffle的Value对象中
     */
    protected Value runLua(String code) {
        // 我们通过 "return " 来确保代码块是一个返回值的表达式
        var value = context.eval("lua", "return " + code);
        return value;
    }
}
