package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugLibraryTest extends AbstractLuaTest {

    @Test
    void getmetatableReturnsRawMetatableIgnoringProtection() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local t = {}
                    local mt = {__metatable = 'locked'}
                    local protected = setmetatable({}, mt)
                    return debug.getmetatable(t) == nil, debug.getmetatable(protected) == mt,
                        debug.getmetatable(42) == nil
                    """);
            assertTrue(result.getArrayElement(0).asBoolean());
            assertTrue(result.getArrayElement(1).asBoolean());
            assertTrue(result.getArrayElement(2).asBoolean());
        }
    }

    @Test
    void setmetatableAssignsAndClearsMetatables() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local t = {}
                    local mt = {__index = function(_, k) return k .. '!' end}
                    debug.setmetatable(t, mt)
                    local via = t.hello
                    debug.setmetatable(t, nil)
                    return via, getmetatable(t)
                    """);
            assertEquals("hello!", result.getArrayElement(0).asString());
            assertTrue(result.getArrayElement(1).isNull());
        }
    }

    @Test
    void getregistryReturnsGlobalsTable() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local registry = debug.getregistry()
                    registry.custom_marker = 42
                    return custom_marker, registry == _G
                    """);
            assertEquals(42L, result.getArrayElement(0).asLong());
            assertTrue(result.getArrayElement(1).asBoolean());
        }
    }

    @Test
    void tracebackIncludesStackFrames() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function inner()
                        return debug.traceback('oops')
                    end
                    local function outer()
                        return inner() .. ''
                    end
                    return outer()
                    """);
            String traceback = result.asString();
            assertTrue(traceback.startsWith("oops\nstack traceback:"), traceback);
            assertTrue(traceback.contains("in function 'inner'"), traceback);
            assertTrue(traceback.contains("in function 'outer'"), traceback);
        }
    }

    @Test
    void tracebackPassesThroughNonStringMessage() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    return debug.traceback({code = 7})
                    """);
            assertTrue(result.hasMembers());
            assertEquals(7L, result.getMember("code").asLong());
        }
    }

    @Test
    void getinfoByLevelDescribesLuaFrames() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function target(a, b)
                        local info = debug.getinfo(1, 'Sl')
                        return info.what, info.currentline > 0, info.short_src ~= nil,
                            info.linedefined > 0, info.source ~= nil
                    end
                    return target(1, 2)
                    """);
            assertEquals("Lua", result.getArrayElement(0).asString());
            assertTrue(result.getArrayElement(1).asBoolean());
            assertTrue(result.getArrayElement(2).asBoolean());
            assertTrue(result.getArrayElement(3).asBoolean());
            assertTrue(result.getArrayElement(4).asBoolean());
        }
    }

    @Test
    void getinfoByLevelCountsCallerLevels() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function target()
                        return debug.getinfo(2).what
                    end
                    local function caller()
                        return target() .. ''
                    end
                    return caller()
                    """);
            assertEquals("Lua", result.asString());
        }
    }

    @Test
    void getinfoForInvalidLevelReturnsNil() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", "return debug.getinfo(42)");
            assertTrue(result.isNull());
        }
    }

    @Test
    void getinfoAcceptsFunctionValue() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local f = function() end
                    local info = debug.getinfo(f, 'f')
                    return info.func == f
                    """);
            assertTrue(result.asBoolean());
        }
    }

    @Test
    void getinfoActivelinesListsFunctionLines() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function target()
                        local info = debug.getinfo(1, 'L')
                        local count = 0
                        for _ in pairs(info.activelines) do count = count + 1 end
                        return count > 0
                    end
                    return target()
                    """);
            assertTrue(result.asBoolean());
        }
    }

    @Test
    void getlocalReturnsNameAndValue() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function target(a, b)
                        local x = 99
                        local y = 'str'
                        local nx, vx = debug.getlocal(1, 1)
                        local ny, vy = debug.getlocal(1, 2)
                        return nx, vx, ny, vy
                    end
                    return target(1, 2)
                    """);
            assertEquals("a", result.getArrayElement(0).asString());
            assertEquals(1L, result.getArrayElement(1).asLong());
            assertEquals("b", result.getArrayElement(2).asString());
            assertEquals(2L, result.getArrayElement(3).asLong());
        }
    }

    @Test
    void getlocalForMissingIndexReturnsNil() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", "return debug.getlocal(1, 100)");
            assertTrue(result.isNull());
        }
    }

    @Test
    void setlocalUpdatesVariableValue() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function target()
                        local x = 1
                        debug.setlocal(1, 1, 777)
                        return x
                    end
                    return target()
                    """);
            assertEquals(777L, result.asLong());
        }
    }

    @Test
    void getupvalueReadsCapturedVariable() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local up = 'captured'
                    local function reader()
                        return up
                    end
                    local name, value = debug.getupvalue(reader, 1)
                    return name, value
                    """);
            assertEquals("up", result.getArrayElement(0).asString());
            assertEquals("captured", result.getArrayElement(1).asString());
        }
    }

    @Test
    void setupvalueWritesCapturedVariable() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local counter = 1
                    local function bump()
                        counter = counter + 1
                    end
                    local function rebase()
                        debug.setupvalue(bump, 1, 100)
                    end
                    rebase()
                    bump()
                    return counter
                    """);
            assertEquals(101L, result.asLong());
        }
    }

    @Test
    void sethookRecordsHookAndMask() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    debug.sethook(print, 'crl')
                    local h, m, c = debug.gethook()
                    debug.debug()
                    return h == print, m, c
                    """);
            assertTrue(result.getArrayElement(0).asBoolean());
            assertEquals("crl", result.getArrayElement(1).asString());
            assertEquals(0L, result.getArrayElement(2).asLong());
        }
    }

    @Test
    void sethookCountRecordsCount() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    debug.sethook(print, '', 4)
                    local h, m, c = debug.gethook()
                    return m, c
                    """);
            assertEquals("", result.getArrayElement(0).asString());
            assertEquals(4L, result.getArrayElement(1).asLong());
        }
    }

    @Test
    void countHookFiresPerInstructionThroughVm() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function measure(count)
                        local fired = 0
                        debug.sethook(function () fired = fired + 1 end, '', count)
                        fired = 0
                        for i = 1, 1000 do end
                        debug.sethook()
                        return fired
                    end
                    local vm = assert(load(string.dump(measure)))
                    return vm(1), vm(4), vm(4000)
                    """);
            long once = result.getArrayElement(0).asLong();
            long everyFour = result.getArrayElement(1).asLong();
            long everyFourThousand = result.getArrayElement(2).asLong();
            // 与 db.lua 的 count hook 断言一致：1000 次迭代约 1000 条指令。
            assertTrue(once > 1000 && once < 1012, "count=1 fired " + once);
            assertTrue(everyFour > 250 && everyFour < 255, "count=4 fired " + everyFour);
            assertEquals(0L, everyFourThousand);
        }
    }

    @Test
    void sethookClearsHookWithNil() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    debug.sethook(print, 'l')
                    debug.sethook(nil)
                    local h, m, c = debug.gethook()
                    return h, m, c
                    """);
            assertTrue(result.getArrayElement(0).isNull());
            assertEquals("", result.getArrayElement(1).asString());
            assertEquals(0L, result.getArrayElement(2).asLong());
        }
    }

    @Test
    void sethookCallReturnEventsAreStrings() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local events = {}
                    debug.sethook(function (e) events[#events + 1] = e end, 'crl')
                    local function f() return 1 end
                    f()
                    debug.sethook()
                    return events
                    """);
            // 至少应观察到事件（line/call/return）
            Value events = result;
            long size = events.getArraySize();
            assertTrue(size >= 1, "expected at least 1 event, got " + size);
            // 事件类型必须是字符串
            for (long i = 0; i < size; i++) {
                assertTrue(events.getArrayElement(i).isString());
            }
        }
    }

    @Test
    void debugLibraryIsRequireable() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", "return require('debug') == debug");
            assertTrue(result.asBoolean());
        }
    }

    @Test
    void setHookOnCoroutineFiresForThatCoroutineOnly() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local events = {}
                    local lines = {}
                    local function hook(e, l)
                        events[#events + 1] = e
                        if l then lines[#lines + 1] = l end
                    end

                    local co = coroutine.create(function (x)
                        local a = x + 1
                        coroutine.yield(a)
                        return a + 1
                    end)

                    local mainBefore = debug.gethook()
                    debug.sethook(co, hook, 'lcr')
                    local coHook = debug.gethook(co)
                    local mainAfter = debug.gethook()

                    local ok, v = coroutine.resume(co, 10)
                    local status = coroutine.status(co)
                    local ok2, v2 = coroutine.resume(co)

                    return mainBefore == nil, coHook == hook, mainAfter == nil,
                        ok, v, status, ok2, v2, #events > 0, #lines > 0
                    """);
            assertTrue(result.getArrayElement(0).asBoolean(), "main had no hook before");
            assertTrue(result.getArrayElement(1).asBoolean(), "gethook(co) returns the hook");
            assertTrue(result.getArrayElement(2).asBoolean(), "main unaffected by coroutine hook");
            assertTrue(result.getArrayElement(3).asBoolean(), "first resume ok");
            assertEquals(11L, result.getArrayElement(4).asLong());
            assertEquals("suspended", result.getArrayElement(5).asString());
            assertTrue(result.getArrayElement(6).asBoolean(), "second resume ok");
            assertEquals(12L, result.getArrayElement(7).asLong());
            assertTrue(result.getArrayElement(8).asBoolean(), "coroutine hook emitted events");
            assertTrue(result.getArrayElement(9).asBoolean(), "coroutine hook emitted line events");
        }
    }

    @Test
    void setHookOnCoroutineFiresForVmClosure() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local events = {}
                    local function hook(e) events[#events + 1] = e end
                    local body = [[
                        local x = ...
                        local a = x + 1
                        coroutine.yield(a)
                        return a + 1
                    ]]
                    local f = assert(load(string.dump(assert(load(body)))))
                    local co = coroutine.create(f)
                    debug.sethook(co, hook, 'lcr')
                    local ok, v = coroutine.resume(co, 20)
                    local ok2, v2 = coroutine.resume(co)
                    return ok, v, ok2, v2, #events > 0
                    """);
            assertTrue(result.getArrayElement(0).asBoolean(), "first resume ok");
            assertEquals(21L, result.getArrayElement(1).asLong());
            assertTrue(result.getArrayElement(2).asBoolean(), "second resume ok");
            assertEquals(22L, result.getArrayElement(3).asLong());
            assertTrue(result.getArrayElement(4).asBoolean(), "VM coroutine hook emitted events");
        }
    }

    @Test
    void callHooksSeeTheCalleeFrameThroughVm() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local events = {}
                    local names = {}
                    local function hook(e)
                        events[#events + 1] = e
                        if e == 'call' or e == 'tail call' then
                            local i = debug.getinfo(2, 'fSn')
                            names[#names + 1] = i and i.what or 'nil'
                            names[#names + 1] = (i and i.name) or ''
                        end
                    end
                    local body = [[
                        local function inner(x) return x + 1 end
                        local function outer(y) return inner(y) * 2 end
                        return outer(3)
                    ]]
                    local vm = assert(load(string.dump(assert(load(body)))))
                    debug.sethook(hook, 'cr')
                    local r = vm()
                    debug.sethook()
                    return r, events, names
                    """);
            assertEquals(8L, result.getArrayElement(0).asLong());
            Value events = result.getArrayElement(1);
            // 第 1 个 return 是 debug.sethook 自身返回时（钩子刚装上，参考实现同样如此）；
            // 最后 1 个 call 是 debug.sethook() 清除钩子（调用前钩子仍在）。
            // vm 尾调用 outer，因此 outer 报告 "tail call"，且链尾只报一次 return（与参考实现一致）。
            assertEquals(7, events.getArraySize(), "sethook 自身 + vm/outer/inner 事件");
            String[] expectedEvents = {
                            "return",    // debug.sethook 装上钩子后自身返回
                            "call",      // vm
                            "tail call", // outer（return outer(3)）
                            "call",      // inner
                            "return",    // inner
                            "return",    // outer（复用 vm 的 CallInfo，不再单独报 vm）
                            "call"       // debug.sethook() 清除钩子
            };
            for (int i = 0; i < expectedEvents.length; i++) {
                assertEquals(expectedEvents[i], events.getArrayElement(i).asString(), "event " + i);
            }
            Value names = result.getArrayElement(2);
            assertEquals(8, names.getArraySize());
            String[] expectedNames = {
                            "main", "",      // vm 是主块（linedefined==0），调用点无名字
                            "Lua", "outer",  // outer，局部变量名
                            "Lua", "inner",  // inner，upvalue 名
                            "C", "sethook"   // 内建调用，what=C
            };
            for (int i = 0; i < expectedNames.length; i++) {
                assertEquals(expectedNames[i], names.getArrayElement(i).asString(), "name " + i);
            }
        }
    }

    @Test
    void callHooksSeeTheCalleeFrameForAstFunctions() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local function f(x) return x + 1 end
                    local calls, rets, what = 0, 0, nil
                    local function hook(e)
                        local i = debug.getinfo(2, 'fS')
                        if i.func == f then
                            what = i.what
                            if e == 'call' then
                                calls = calls + 1
                            elseif e == 'return' then
                                rets = rets + 1
                            end
                        end
                    end
                    debug.sethook(hook, 'cr')
                    local r = f(1)
                    debug.sethook()
                    return r, calls, rets, what
                    """);
            assertEquals(2L, result.getArrayElement(0).asLong());
            assertEquals(1L, result.getArrayElement(1).asLong(), "call hook sees f");
            assertEquals(1L, result.getArrayElement(2).asLong(), "return hook sees f");
            assertEquals("Lua", result.getArrayElement(3).asString());
        }
    }

    @Test
    void getinfoReportsCFunctionAtBuiltinLevel() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local what, source, line, ispcall
                    local function target()
                        local info = debug.getinfo(2, 'Sfl')
                        what, source, line, ispcall = info.what, info.source, info.currentline, info.func == pcall
                    end
                    pcall(target)
                    return what, source, line, ispcall
                    """);
            // level 2 from inside target(): target 的调用者是 pcall（C 函数）
            assertEquals("C", result.getArrayElement(0).asString());
            assertEquals("=[C]", result.getArrayElement(1).asString());
            assertEquals(-1L, result.getArrayElement(2).asLong(), "C functions have no current line");
            assertTrue(result.getArrayElement(3).asBoolean(), "func is the Lua function that was called");
        }
    }

    @Test
    void tailCallHooksSkipTheCallerReturnForAstFunctions() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local events = {}
                    local function hook(e) events[#events + 1] = e end
                    local function inner(x) return x end
                    local function outer(x) return inner(x) end
                    debug.sethook(hook, 'cr')
                    outer(1)
                    debug.sethook()
                    return table.concat(events, ',')
                    """);
            // 与参考实现一致：outer 的 return 被 tail call 取代，链尾只报一次 return。
            assertEquals("return,call,tail call,return,call", result.asString());
        }
    }

    @Test
    void getinfoReportsMainForAstChunks() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local f = function(x) return x * 2 end
                    local chunk = load("return f(21)", "chunk", "t", {f = f})
                    local what
                    debug.sethook(function(e)
                        local info = debug.getinfo(2, 'Sf')
                        if info and info.func == chunk then what = info.what end
                    end, 'c')
                    local r = chunk()
                    debug.sethook()
                    return r, what
                    """);
            assertEquals(42L, result.getArrayElement(0).asLong());
            assertEquals("main", result.getArrayElement(1).asString());
        }
    }
}
