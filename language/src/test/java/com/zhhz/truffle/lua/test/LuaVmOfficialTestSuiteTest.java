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
            "strings.lua",
            "literals.lua",
            "tpack.lua",
            "locals.lua",
            "code.lua",
            "nextvar.lua",
            "pm.lua",
            "utf8.lua",
            "events.lua",
            "vararg.lua",
            "goto.lua",
            "math.lua",
            "coroutine.lua",
            "attrib.lua",
            "sort.lua",
            "gc.lua",
    };

    /** Same preamble all.lua installs before running the individual test files. */
    private static final String PREAMBLE = """
            _soft = false
            _port = true
            _nomsg = true
            Message = function () end
            math.randomseed(0)
            debug = nil
            """;

    @Test
    void officialTestFilesRunThroughBytecodeVm() {
        Path dir = Path.of(System.getProperty("opencode.testdir", "E:\\lua\\test"));
        if (!Files.isDirectory(dir)) {
            System.out.println("skip: testdir=" + dir + " not found");
            return;
        }
        for (String name : FILES) {
            if (!Files.exists(dir.resolve(name))) {
                System.out.println("skip: missing " + name);
                return;
            }
        }
        try (Context context = newContextBuilder("lua")
                .out(java.io.OutputStream.nullOutputStream())
                .err(java.io.OutputStream.nullOutputStream())
                .build()) {
            context.eval("lua", PREAMBLE);
            int executed = 0;
            for (String name : FILES) {
                String driver = """
                        local f = assert(loadfile([[%s]]), 'loadfile failed')
                        local b = string.dump(f)
                        local g = assert(load(b), 'reload failed')
                        g()
                        """.formatted(dir.resolve(name).toString().replace('\\', '/'));
                try {
                    context.eval("lua", driver);
                } catch (PolyglotException e) {
                    fail("bytecode VM test failed in " + name + ": " + e.getMessage(), e);
                }
                executed++;
            }
            assertEquals(FILES.length, executed);
        }
    }
}
