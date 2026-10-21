package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoroutineTest extends AbstractLuaTest {

    @Test
    void isyieldableDependsOnCoroutineAndCCallBoundary() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local inCo = coroutine.wrap(function () return coroutine.isyieldable() end)
                    local inGsub = coroutine.wrap(function ()
                        local seen
                        string.gsub('a', '.', function () seen = coroutine.isyieldable() end)
                        return seen
                    end)
                    local inSort = coroutine.wrap(function ()
                        local seen
                        table.sort({2, 1}, function (a, b)
                            seen = coroutine.isyieldable()
                            return a < b
                        end)
                        return seen
                    end)
                    return coroutine.isyieldable(), inCo(), inGsub(), inSort()
                    """);
            assertEquals(false, result.getArrayElement(0).asBoolean(), "main thread is not yieldable");
            assertEquals(true, result.getArrayElement(1).asBoolean(), "plain coroutine is yieldable");
            assertEquals(false, result.getArrayElement(2).asBoolean(), "gsub replacement is not yieldable");
            assertEquals(false, result.getArrayElement(3).asBoolean(), "sort comparator is not yieldable");
        }
    }

    @Test
    void yieldInsideCCallbackFails() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local co = coroutine.create(function ()
                        string.gsub('a', '.', function () coroutine.yield() end)
                    end)
                    local ok, err = coroutine.resume(co)
                    return ok, err
                    """);
            assertEquals(false, result.getArrayElement(0).asBoolean());
            assertTrue(result.getArrayElement(1).asString().contains("attempt to yield across a C-call boundary"),
                    result.getArrayElement(1).asString());
        }
    }

    @Test
    void closingContextReleasesSuspendedCoroutines() {
        var context = newContextBuilder("lua").build();
        context.eval("lua", """
                co = coroutine.create(function ()
                    coroutine.yield()
                end)
                assert(coroutine.resume(co))
                """);
        // 关闭时若协程线程未退出，Truffle 会抛 "did not complete all polyglot threads"。
        context.close();
    }

    @Test
    void resumingDeadCoroutineReportsError() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local co = coroutine.create(function () return 1 end)
                    coroutine.resume(co)
                    local ok, err = coroutine.resume(co)
                    return ok, err
                    """);
            assertEquals(false, result.getArrayElement(0).asBoolean());
            assertTrue(result.getArrayElement(1).asString().contains("dead"), result.getArrayElement(1).asString());
        }
    }

    @Test
    void yieldAcrossPcallIsAllowed() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local co = coroutine.create(function ()
                        local ok, v = pcall(function ()
                            coroutine.yield(7)
                            return 8
                        end)
                        return ok, v
                    end)
                    local ok, v = coroutine.resume(co)
                    local ok2, a, b = coroutine.resume(co)
                    return ok, v, ok2, a, b
                    """);
            assertTrue(result.getArrayElement(0).asBoolean());
            assertEquals(7L, result.getArrayElement(1).asLong());
            assertTrue(result.getArrayElement(2).asBoolean());
            assertTrue(result.getArrayElement(3).asBoolean());
            assertEquals(8L, result.getArrayElement(4).asLong());
        }
    }

    @Test
    void yieldInsideErrorHandlerIsRejectedByCallbackBoundary() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local co = coroutine.create(function ()
                        local ok, msg = xpcall(function ()
                            error('boom')
                        end, function (e)
                            return coroutine.isyieldable(), e
                        end)
                        return ok, msg
                    end)
                    local ok, coOk, msg = coroutine.resume(co)
                    return ok, coOk, msg
                    """);
            // xpcall 的消息处理函数经 C 边界调用：不可 yield（参考实现同样如此）。
            assertTrue(result.getArrayElement(0).asBoolean(), "resume succeeded");
            assertEquals(false, result.getArrayElement(1).asBoolean(), "xpcall reports the error");
            assertEquals(false, result.getArrayElement(2).asBoolean(), "handler is not yieldable");
        }
    }

    @Test
    void resumeFromInsideCoroutineWorks() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local inner = coroutine.create(function ()
                        coroutine.yield('i1')
                        return 'i2'
                    end)
                    local outer = coroutine.create(function ()
                        local a, b = coroutine.resume(inner)
                        coroutine.yield(a, b)
                        local c, d = coroutine.resume(inner)
                        return c, d
                    end)
                    local ok, x, y = coroutine.resume(outer)
                    local ok2, p, q = coroutine.resume(outer)
                    return ok, x, y, ok2, p, q
                    """);
            assertTrue(result.getArrayElement(0).asBoolean(), "outer resumes");
            assertEquals(true, result.getArrayElement(1).asBoolean(), "inner resumes");
            assertEquals("i1", result.getArrayElement(2).asString());
            assertTrue(result.getArrayElement(3).asBoolean(), "outer finishes");
            assertEquals(true, result.getArrayElement(4).asBoolean(), "inner finishes");
            assertEquals("i2", result.getArrayElement(5).asString());
        }
    }

    @Test
    void coroutineBodyMayTailCallWithoutLeakingControlException() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function target(x) return x * 2 end
                    local function body() return target(21) end
                    local ok, value = coroutine.resume(coroutine.create(body))
                    local wrapped = coroutine.wrap(body)
                    return ok, value, wrapped()
                    """);
            // 协程体的尾调用在固定深度循环里接续执行，resume 不应看到内部控制流异常。
            assertTrue(result.getArrayElement(0).asBoolean(), "resume succeeded");
            assertEquals(42L, result.getArrayElement(1).asLong());
            assertEquals(42L, result.getArrayElement(2).asLong());
        }
    }

    @Test
    void wrapPropagatesErrors() {
        try (var context = newContextBuilder("lua").build()) {
            var error = assertThrows(org.graalvm.polyglot.PolyglotException.class, () ->
                    context.eval("lua", """
                            local f = coroutine.wrap(function () error('from coroutine') end)
                            f()
                            """));
            assertTrue(error.getMessage().contains("from coroutine"), error.getMessage());
        }
    }
}
