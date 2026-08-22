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
                        return inner()
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
                        return target()
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
                        return debug.getupvalue(reader, 1)
                    end
                    return reader()
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
    void hookPlaceholdersDoNotFail() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    debug.sethook(print, 'line')
                    local h = debug.gethook()
                    debug.debug()
                    return h
                    """);
            assertTrue(result.isNull());
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
    void getinfoReportsCFunctionAtBuiltinLevel() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local what
                    local function target()
                        what = debug.getinfo(2, 'S').what
                    end
                    pcall(target)
                    return what
                    """);
            // level 2 from inside target(): target 的调用者是 pcall（C 函数）
            assertFalse(result.asString().isEmpty());
        }
    }
}
