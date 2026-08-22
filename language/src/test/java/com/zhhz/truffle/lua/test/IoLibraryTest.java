package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IoLibraryTest extends AbstractLuaTest {

    @Test
    void supportsDefaultFilesReadWriteSeekAndClose() throws Exception {
        var file = Files.createTempFile("lua-io-", ".txt");
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local path = [[%s]]
                    local output = assert(io.output(path))
                    assert(io.write('alpha', 42, '\n', 'beta'))
                    assert(io.close())
                    local input = assert(io.input(path))
                    local first = io.read('l')
                    local rest = io.read('a')
                    input:close()

                    local file = assert(io.open(path, 'r+'))
                    local start = assert(file:seek('set', 0))
                    local prefix = file:read(5)
                    assert(file:seek('end', 0))
                    assert(file:write('!'))
                    assert(file:close())
                    return io.type(output), io.type(input), first, rest, start, prefix,
                        io.type(file), io.type(io.stdin), io.stdin:close() == false
                    """.formatted(file));
            assertEquals("closed file", result.getArrayElement(0).asString());
            assertEquals("closed file", result.getArrayElement(1).asString());
            assertEquals("alpha42", result.getArrayElement(2).asString());
            assertEquals("beta", result.getArrayElement(3).asString());
            assertEquals(0, result.getArrayElement(4).asInt());
            assertEquals("alpha", result.getArrayElement(5).asString());
            assertEquals("closed file", result.getArrayElement(6).asString());
            assertEquals("file", result.getArrayElement(7).asString());
            assertTrue(result.getArrayElement(8).asBoolean());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void supportsStandardStreamsAndLineIterators() throws Exception {
        var file = Files.createTempFile("lua-lines-", ".txt");
        Files.writeString(file, "one\ntwo\nthree\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        try (var context = newContextBuilder("lua").out(output).err(error).build()) {
            Value result = context.eval("lua", """
                    io.write('out', 7)
                    io.stderr:write('err', 8)
                    local values = {}
                    for line in io.lines([[%s]]) do values[#values + 1] = line end
                    local file = assert(io.open([[%s]], 'r'))
                    local iterator = file:lines()
                    local first, second = iterator(), iterator()
                    file:close()
                    return table.concat(values, ','), first, second
                    """.formatted(file, file));
            assertEquals("one,two,three", result.getArrayElement(0).asString());
            assertEquals("one", result.getArrayElement(1).asString());
            assertEquals("two", result.getArrayElement(2).asString());
        } finally {
            Files.deleteIfExists(file);
        }
        assertEquals("out7", output.toString(StandardCharsets.UTF_8));
        assertEquals("err8", error.toString(StandardCharsets.UTF_8));
    }

    @Test
    void supportsTmpfileNumbersAndFailureResults() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    local file = assert(io.tmpfile())
                    assert(file:write('10 2.5\ntext'))
                    assert(file:seek('set', 0) == 0)
                    local integer, float = file:read('n', 'n')
                    local line = file:read('l')
                    local text = file:read('a')
                    assert(file:setvbuf('no'))
                    assert(file:close())
                    local missing, message, code = io.open('__missing_lua_io_file__', 'r')
                    return integer, float, line, text, missing == nil,
                        type(message), type(code), io.type(file)
                    """);
            assertEquals(10, result.getArrayElement(0).asInt());
            assertEquals(2.5, result.getArrayElement(1).asDouble());
            assertEquals("", result.getArrayElement(2).asString());
            assertEquals("text", result.getArrayElement(3).asString());
            assertTrue(result.getArrayElement(4).asBoolean());
            assertEquals("string", result.getArrayElement(5).asString());
            assertEquals("number", result.getArrayElement(6).asString());
            assertEquals("closed file", result.getArrayElement(7).asString());
        }
    }
}
