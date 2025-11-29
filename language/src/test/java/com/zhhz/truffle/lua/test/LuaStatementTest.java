package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LuaStatementTest extends AbstractLuaTest {

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

    // 辅助方法：执行代码块并返回结果
    private Value eval(String code) {
        return context.eval("lua", code);
    }

    // --- 1. 赋值语句测试 ---

    @Test
    public void testAssignment() {
        // 全局变量赋值
        assertEquals(10L, eval("a = 10; return a").asLong());

        // 局部变量赋值
        assertEquals(20L, eval("local a = 20; return a").asLong());

        // 作用域遮蔽 (Shadowing)
        String code = """
            a = 10
            do
                local a = 20
                if a ~= 20 then return false end
            end
            return a
        """;
        assertEquals(10L, eval(code).asLong());
    }

    @Test
    public void testMultiAssignment() {
        // 多重赋值
        Value res = eval("local a, b = 1, 2; return a + b");
        assertEquals(3L, res.asLong());

        // 交换变量 (Swap) - Lua 的经典特性
        String swap = """
            local a, b = 1, 2
            a, b = b, a
            return a
        """;
        assertEquals(2L, eval(swap).asLong());

        // 值不足补 nil
        assertTrue(eval("local a, b = 1; return b").isNull());
    }

    // --- 2. 控制流测试 (if, while, repeat) ---

    @Test
    public void testIfStatement() {
        assertEquals(1L, eval("if true then return 1 else return 2 end").asLong());
        assertEquals(2L, eval("if false then return 1 else return 2 end").asLong());
        assertEquals(3L, eval("if false then return 1 elseif true then return 3 else return 2 end").asLong());

        // 测试 Lua 的真值规则 (0 是真)
        assertEquals(1L, eval("if 0 then return 1 else return 2 end").asLong());
    }

    @Test
    public void testWhileLoop() {
        String code = """
            local i = 0
            while i < 5 do
                i = i + 1
            end
            return i
        """;
        assertEquals(5L, eval(code).asLong());
    }

    @Test
    public void testRepeatLoop() {
        // repeat 至少执行一次，且 until 条件可以访问循环体内的局部变量
        String code = """
            local a = 0
            repeat
                a = a + 1
                local inside = true
            until inside and a == 3
            return a
        """;
        assertEquals(3L, eval(code).asLong());
    }

    @Test
    public void testBreak() {
        String code = """
            local i = 0
            while true do
                i = i + 1
                if i > 2 then break end
            end
            return i
        """;
        assertEquals(3L, eval(code).asLong());
    }

    // --- 3. For 循环测试 ---

    @Test
    public void testNumericFor() {
        // 正向步长
        String code1 = """
            local sum = 0
            for i = 1, 5 do
                sum = sum + i
            end
            return sum
        """;
        assertEquals(15L, eval(code1).asLong());

        // 自定义步长
        String code2 = """
            local count = 0
            for i = 1, 10, 2 do
                count = count + 1
            end
            return count
        """;
        assertEquals(5L, eval(code2).asLong());
    }

    @Test
    public void testGenericFor() {
        // 需要你的解释器已经实现了 ipairs
        String code = """
            local t = {10, 20, 30}
            local sum = 0
            for i, v in ipairs(t) do
                sum = sum + v
            end
            return sum
        """;
        assertEquals(60L, eval(code).asLong());
    }

    // --- 4. 函数与闭包测试 ---

    @Test
    public void testFunctionDefAndCall() {
        String code = """
            function add(a, b)
                return a + b
            end
            return add(10, 20)
        """;
        assertEquals(30L, eval(code).asLong());
    }

    @Test
    public void testRecursion() {
        // 经典的斐波那契，测试递归调用
        String code = """
            function fib(n)
                if n < 2 then return n end
                return fib(n-1) + fib(n-2)
            end
            return fib(10)
        """;
        assertEquals(55L, eval(code).asLong());
    }

    @Test
    public void testClosureAndUpvalues() {
        // 测试闭包（Upvalue）读写
        String code = """
            function make_counter()
                local count = 0
                return function()
                    count = count + 1
                    return count
                end
            end
        
            local c1 = make_counter()
            local c2 = make_counter()
        
            c1() -- 1
            c1() -- 2
        
            local res2 = c2() -- 应该互不干扰，c2 是 1
            local res1 = c1() -- c1 是 3
        
            return res1 * 10 + res2
        """;
        // 期望 c1=3, c2=1 -> result = 31
        assertEquals(31L, eval(code).asLong());
    }
}