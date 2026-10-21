package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuaFileLoadingTest extends AbstractLuaTest {

    @Test
    void emptyChunkExecutesWithoutParserFailure() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", "");
            assertTrue(result.isNull());
        }
    }

    @Test
    void loadfileCompilesWithoutExecutingAndAcceptsArguments() throws Exception {
        var file = Files.createTempFile("lua-loadfile-", ".lua");
        Files.writeString(file, "loaded = true\nlocal a, b = ...\nreturn a + b, 'ok'");
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local chunk, err = loadfile([[%s]], 't')
                    local before = loaded == nil
                    local sum, text = chunk(20, 22)
                    return type(chunk), err == nil, before, loaded, sum, text
                    """.formatted(file));
            assertEquals("function", result.getArrayElement(0).asString());
            assertTrue(result.getArrayElement(1).asBoolean());
            assertTrue(result.getArrayElement(2).asBoolean());
            assertTrue(result.getArrayElement(3).asBoolean());
            assertEquals(42, result.getArrayElement(4).asInt());
            assertEquals("ok", result.getArrayElement(5).asString());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void dofileExecutesAndPropagatesAllResults() throws Exception {
        var file = Files.createTempFile("lua-dofile-", ".lua");
        Files.writeString(file, "return 7, 'done', true");
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", "return dofile([[" + file + "]])");
            assertEquals(7, result.getArrayElement(0).asInt());
            assertEquals("done", result.getArrayElement(1).asString());
            assertTrue(result.getArrayElement(2).asBoolean());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void relativeDofileUsesEntryScriptDirectory() throws Exception {
        var directory = Files.createTempDirectory("lua-relative-dofile-");
        var entry = directory.resolve("all.lua");
        var child = directory.resolve("main.lua");
        Files.writeString(entry, "return dofile('main.lua')");
        Files.writeString(child, "return 42, 'relative'");
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval(Source.newBuilder("lua", entry.toFile()).build());
            assertEquals(42, result.getArrayElement(0).asInt());
            assertEquals("relative", result.getArrayElement(1).asString());
        } finally {
            Files.deleteIfExists(child);
            Files.deleteIfExists(entry);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    void errorMessageUsesNormalizedChunkId() throws Exception {
        var file = Files.createTempFile("lua-error-", ".lua");
        Files.writeString(file, "error('boom')");
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local ok, err = pcall(dofile, [[%s]])
                    return ok, err
                    """.formatted(file));
            assertEquals(false, result.getArrayElement(0).asBoolean());
            String message = result.getArrayElement(1).asString();
            // luaL_where 经 luaO_chunkid 规范化源码名：不带 '@' 前缀，过长路径按 LUA_IDSIZE 截断。
            assertFalse(message.startsWith("@"), message);
            assertTrue(message.endsWith(":1: boom"), message);
            assertTrue(message.contains(file.getFileName().toString()), message);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void loadfileReturnsErrorsAndDofileRaisesThem() throws Exception {
        var invalid = Files.createTempFile("lua-invalid-", ".lua");
        Files.writeString(invalid, "local = broken");
        var missing = invalid.resolveSibling(invalid.getFileName() + ".missing");
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local syntaxChunk, syntaxError = loadfile([[%s]])
                    local missingChunk, missingError = loadfile([[%s]])
                    local binaryChunk, binaryError = loadfile([[%s]], 'b')
                    local ok, runtimeError = pcall(dofile, [[%s]])
                    return syntaxChunk == nil, type(syntaxError),
                        missingChunk == nil, type(missingError),
                        binaryChunk == nil, type(binaryError),
                        not ok, type(runtimeError),
                        string.find(missingError, 'No such file or directory', 1, true) ~= nil,
                        string.find(runtimeError, 'No such file or directory', 1, true) ~= nil
                    """.formatted(invalid, missing, invalid, missing));
            for (int i = 0; i < 8; i++) {
                if (i % 2 == 0) assertTrue(result.getArrayElement(i).asBoolean());
                else assertEquals("string", result.getArrayElement(i).asString());
            }
            assertTrue(result.getArrayElement(8).asBoolean());
            assertTrue(result.getArrayElement(9).asBoolean());
        } finally {
            Files.deleteIfExists(invalid);
        }
    }
}
