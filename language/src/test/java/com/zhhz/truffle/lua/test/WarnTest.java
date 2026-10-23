package com.zhhz.truffle.lua.test;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarnTest extends AbstractLuaTest {

    @Test
    void warningsAreOffUntilEnabled() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        var builder = newContextBuilder("lua");
        builder.err(err);
        try (var context = builder.build()) {
            context.eval("lua", """
                    warn('ignored')
                    warn('@on')
                    warn('a', 'b')
                    warn(12)
                    warn('@off')
                    warn('silent')
                    """);
        }
        assertEquals("Lua warning: ab\nLua warning: 12\n", normalize(err));
    }

    @Test
    void unknownControlMessagesAreIgnored() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        var builder = newContextBuilder("lua");
        builder.err(err);
        try (var context = builder.build()) {
            context.eval("lua", """
                    warn('@on')
                    warn('@unknown')
                    warn('visible')
                    """);
        }
        assertEquals("Lua warning: visible\n", normalize(err));
    }

    @Test
    void warnsRejectNonStringArguments() {
        try (var context = newContextBuilder("lua").build()) {
            assertThrows(org.graalvm.polyglot.PolyglotException.class,
                    () -> context.eval("lua", "warn({})"));
            assertThrows(org.graalvm.polyglot.PolyglotException.class,
                    () -> context.eval("lua", "warn()"));
        }
    }

    private static String normalize(ByteArrayOutputStream out) {
        return out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
