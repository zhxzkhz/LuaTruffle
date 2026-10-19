package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Regression test: run official Lua 5.3 test files through the bytecode VM by
 * dumping each chunk with {@code string.dump} and reloading it with
 * {@code load}, mirroring the dump/undump path used by {@code all.lua}.
 *
 * <p>Skipped when the external Lua test suite (default {@code E:\lua\test}) is
 * not present; override with {@code -Dopencode.testdir=...}.
 */
class LuaVmOfficialTestSuiteTest extends AbstractLuaTest {

    private static final String[] FILES = {
            "constructs.lua",
            "closure.lua",
            "bitwise.lua",
            "calls.lua",
            "errors.lua",
            "db.lua",
    };

    @Test
    void officialTestFilesRunThroughBytecodeVm() {
        Path dir = Path.of(System.getProperty("opencode.testdir", "E:\\lua\\test"));
        if (!Files.isDirectory(dir)) {
            System.out.println("skip: testdir=" + dir + " not found");
            return;
        }
        StringBuilder list = new StringBuilder();
        for (String name : FILES) {
            if (!Files.exists(dir.resolve(name))) {
                System.out.println("skip: missing " + name);
                return;
            }
            if (!list.isEmpty()) {
                list.append(", ");
            }
            list.append('\'').append(name).append('\'');
        }
        String driver = """
                local dir = [[%s]]
                local files = { %s }
                for _, name in ipairs(files) do
                    local f = assert(loadfile(dir .. '/' .. name), 'loadfile failed: ' .. name)
                    local b = string.dump(f)
                    local g = assert(load(b), 'reload failed: ' .. name)
                    g()
                end
                return #files
                """.formatted(dir.toString().replace('\\', '/'), list);
        try (Context context = newContextBuilder("lua")
                .out(java.io.OutputStream.nullOutputStream())
                .err(java.io.OutputStream.nullOutputStream())
                .build()) {
            var result = context.eval("lua", driver);
            assertEquals(FILES.length, result.asInt());
        } catch (PolyglotException e) {
            fail("bytecode VM test suite failed: " + e.getMessage(), e);
        }
    }
}
