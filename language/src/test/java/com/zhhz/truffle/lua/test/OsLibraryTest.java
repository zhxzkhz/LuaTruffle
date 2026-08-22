package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsLibraryTest extends AbstractLuaTest {

    @Test
    void supportsTimeDateAndDifftime() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local epoch = os.time({year = 2000, month = 1, day = 2, hour = 3, min = 4, sec = 5})
                    local utc = os.date('!*t', 0)
                    return type(epoch), os.difftime(epoch, epoch - 10),
                        utc.year, utc.month, utc.day, utc.hour, utc.wday, utc.yday,
                        os.date('!%Y-%m-%d %H:%M:%S', 0)
                    """);
            assertEquals("number", result.getArrayElement(0).asString());
            assertEquals(10.0, result.getArrayElement(1).asDouble());
            assertEquals(1970, result.getArrayElement(2).asInt());
            assertEquals(1, result.getArrayElement(3).asInt());
            assertEquals(1, result.getArrayElement(4).asInt());
            assertEquals(0, result.getArrayElement(5).asInt());
            assertEquals(5, result.getArrayElement(6).asInt());
            assertEquals(1, result.getArrayElement(7).asInt());
            assertEquals("1970-01-01 00:00:00", result.getArrayElement(8).asString());
        }
    }

    @Test
    void supportsEnvironmentLocaleAndClock() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    return os.getenv('__LUA_TRUFFLE_MISSING_ENV__') == nil,
                        type(os.setlocale(nil)) == 'string', os.clock() >= 0,
                        type(os.tmpname()) == 'string', os.setlocale(nil, 'invalid') == nil
                    """);
            assertTrue(result.getArrayElement(0).asBoolean());
            assertTrue(result.getArrayElement(1).asBoolean());
            assertTrue(result.getArrayElement(2).asBoolean());
            assertTrue(result.getArrayElement(3).asBoolean());
            assertTrue(result.getArrayElement(4).asBoolean());
        }
    }

    @Test
    void supportsRenameRemoveAndExecuteResults() throws Exception {
        var source = Files.createTempFile("lua-os-source-", ".tmp");
        var target = source.resolveSibling(source.getFileName() + ".renamed");
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local source = [[%s]]
                    local target = [[%s]]
                    local renamed = os.rename(source, target)
                    local removed = os.remove(target)
                    local missing, message, code = os.remove(target)
                    local ok, reason, status = os.execute('exit 0')
                    return renamed, removed, missing == nil, type(message), type(code),
                        ok, reason, status
                    """.formatted(source, target));
            assertTrue(result.getArrayElement(0).asBoolean());
            assertTrue(result.getArrayElement(1).asBoolean());
            assertTrue(result.getArrayElement(2).asBoolean());
            assertEquals("string", result.getArrayElement(3).asString());
            assertEquals("number", result.getArrayElement(4).asString());
            assertTrue(result.getArrayElement(5).asBoolean());
            assertEquals("exit", result.getArrayElement(6).asString());
            assertEquals(0, result.getArrayElement(7).asInt());
            assertTrue(Files.notExists(target));
        } finally {
            Files.deleteIfExists(source);
            Files.deleteIfExists(target);
        }
    }
}
