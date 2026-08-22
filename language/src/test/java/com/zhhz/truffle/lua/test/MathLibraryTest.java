package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathLibraryTest extends AbstractLuaTest {

    @Test
    void exposesLua53MathConstantsAndTypes() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    return math.pi > 3 and math.huge > math.maxinteger
                        and math.mininteger < 0 and math.type(1) == 'integer'
                        and math.type(1.5) == 'float'
                    """);
            assertTrue(result.asBoolean());
        }
    }

    @Test
    void supportsCoreMathFunctions() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    return math.floor(-3.4), math.ceil(-3.4), math.fmod(-10, 3),
                        math.log(8, 2), math.atan(1, 0), math.abs(-7)
                    """);
            assertEquals(-4L, result.getArrayElement(0).asLong());
            assertEquals(-3L, result.getArrayElement(1).asLong());
            assertEquals(-1L, result.getArrayElement(2).asLong());
            assertEquals(3.0, result.getArrayElement(3).asDouble());
            assertEquals(Math.PI / 2, result.getArrayElement(4).asDouble(), 1.0e-12);
            assertEquals(7L, result.getArrayElement(5).asLong());
        }
    }

    @Test
    void supportsModfAndSeededRandomRanges() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    math.randomseed(42)
                    local a, b = math.modf(-2.5)
                    local value = math.random(10, 20)
                    return a, b, value >= 10 and value <= 20
                    """);
            assertEquals(-2.0, result.getArrayElement(0).asDouble());
            assertEquals(-0.5, result.getArrayElement(1).asDouble());
            assertTrue(result.getArrayElement(2).asBoolean());
        }
    }

    @Test
    void handlesIntegerBoundariesWithoutOverflowOrPrecisionLoss() {
        try (var context = newContextBuilder("lua").build()) {
            Value result = context.eval("lua", """
                    math.randomseed(42)
                    local random = math.random(math.maxinteger - 1, math.maxinteger)
                    return math.fmod(math.mininteger, -1),
                        math.tointeger(9223372036854775808.0) == nil,
                        math.ceil(9223372036854775808.0) == 9223372036854775808.0,
                        math.max(9007199254740993, 9007199254740992.0),
                        math.min(9007199254740993, 9007199254740992.0),
                        random >= math.maxinteger - 1 and random <= math.maxinteger,
                        math.ult(0, -1) and not math.ult(-1, 0)
                    """);
            assertEquals(0L, result.getArrayElement(0).asLong());
            assertTrue(result.getArrayElement(1).asBoolean());
            assertTrue(result.getArrayElement(2).asBoolean());
            assertEquals(9_007_199_254_740_993L, result.getArrayElement(3).asLong());
            assertEquals(9_007_199_254_740_992L, result.getArrayElement(4).asLong());
            assertTrue(result.getArrayElement(5).asBoolean());
            assertTrue(result.getArrayElement(6).asBoolean());
        }
    }
}
