package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringLibraryTest extends AbstractLuaTest {

    private Context context;

    @BeforeEach
    public void setUp() {
        context = newContextBuilder("lua").build();
    }

    @AfterEach
    public void tearDown() {
        context.close();
    }

    private Value eval(String code) {
        return context.eval("lua", code);
    }

    @Test
    public void testLibraryRegistration() {
        assertTrue(eval("""
                return type(string) == 'table'
                    and type(string.byte) == 'function'
                    and type(string.char) == 'function'
                    and type(string.find) == 'function'
                    and type(string.format) == 'function'
                    and type(string.dump) == 'function'
                    and type(string.gmatch) == 'function'
                    and type(string.gsub) == 'function'
                    and type(string.len) == 'function'
                    and type(string.lower) == 'function'
                    and type(string.match) == 'function'
                    and type(string.pack) == 'function'
                    and type(string.packsize) == 'function'
                    and type(string.rep) == 'function'
                    and type(string.reverse) == 'function'
                    and type(string.sub) == 'function'
                    and type(string.unpack) == 'function'
                    and type(string.upper) == 'function'
                """).asBoolean());
    }

    @Test
    public void testByteOrientedOperationsAndArity() {
        assertTrue(eval("""
                local a, b, c = string.byte('你', 1, 3)
                return string.len('你好') == 6
                    and a == 228 and b == 189 and c == 160
                    and string.char(a, b, c) == '你'
                    and select('#', string.byte('', 1)) == 0
                    and string.sub('你好', 1, 3) == '你'
                    and string.rep('ab', 3, '-') == 'ab-ab-ab'
                    and string.reverse('abc') == 'cba'
                    and string.upper('aZ9') == 'AZ9'
                    and string.lower('Az9') == 'az9'
                """).asBoolean());
    }

    @Test
    public void testFindPlainPatternAndCaptures() {
        assertTrue(eval("""
                local ps, pe = string.find('a.b.c', '.b', 1, true)
                local s, e, word, digits = string.find('xx abc-123 yy', '(%a+)%-(%d+)')
                return ps == 2 and pe == 3
                    and s == 4 and e == 10
                    and word == 'abc' and digits == '123'
                    and string.find('abc', 'z') == nil
                """).asBoolean());
    }

    @Test
    public void testPatternClassesQuantifiersAndCaptures() {
        assertTrue(eval("""
                local whole = string.match('abc123', '%a+%d+')
                local letters, digits = string.match('abc123', '(%a+)(%d+)')
                local shortest = string.match('a123b456b', 'a.-b')
                local repeated = string.match('hello hello', '(%a+)%s+%1')
                local position, value = string.match('abc123', '()%d+')
                local balanced = string.match('x(a(b)c)y', '%b()')
                local frontier = string.match('one two', '%f[%a]two%f[%A]')
                return whole == 'abc123'
                    and letters == 'abc' and digits == '123'
                    and shortest == 'a123b'
                    and repeated == 'hello'
                    and position == 4 and value == nil
                    and balanced == '(a(b)c)'
                    and frontier == 'two'
                """).asBoolean());
    }

    @Test
    public void testGmatchIteratorWithAndWithoutCaptures() {
        assertTrue(eval("""
                local words = ''
                for word in string.gmatch('one two three', '%a+') do
                    words = words .. '[' .. word .. ']'
                end

                local pairs = ''
                for key, value in string.gmatch('a=1,b=22', '(%a)=(%d+)') do
                    pairs = pairs .. key .. value
                end
                return words == '[one][two][three]' and pairs == 'a1b22'
                """).asBoolean());
    }

    @Test
    public void testGsubStringTableAndFunctionReplacement() {
        assertTrue(eval("""
                local text, count = string.gsub('hello Lua Lua', '(%a+)', '[%1]', 2)
                local tableText, tableCount = string.gsub('a b c', '%a', {a = 'A', b = false})
                local functionText, functionCount = string.gsub('a1 b22', '(%a)(%d+)', function(letter, digits)
                    return letter .. ':' .. digits
                end)
                local whole = string.gsub('abc', '%a+', '<%0>%%')
                return text == '[hello] [Lua] Lua' and count == 2
                    and tableText == 'A b c' and tableCount == 3
                    and functionText == 'a:1 b:22' and functionCount == 2
                    and whole == '<abc>%'
                """).asBoolean());
    }

    @Test
    public void testFormatConversionsAndQuotedStrings() {
        // 标准 Lua 5.3 的 %q 将换行引用为反斜杠 + 真实换行（不是两字符 \n）。
        assertTrue(eval("""
                return string.format('%s:%04d:%x:%.2f', 'v', 7, 255, 1.25) == 'v:0007:ff:1.25'
                    and string.format('%q', 'a\\nb') == string.char(34, 97, 92, 10, 98, 34)
                    and string.format('%% %i %u', -2, 3) == '% -2 3'
                """).asBoolean());
    }

    @Test
    public void testNumberFormattingMatchesC() {
        assertTrue(eval("""
                return tostring(1/3) == '0.33333333333333'
                    and tostring(2^63) == '9.2233720368548e+18'
                    and tostring(math.huge) == 'inf'
                    and tostring(-math.huge) == '-inf'
                    and tostring(0/0) == 'nan'
                    and tostring(1.0) == '1.0'
                    and string.format('%q', 1/3) == '0x1.5555555555555p-2'
                    and string.format('%q', -0.0) == '-0x0p+0'
                    and string.format('%q', math.huge) == '1e9999'
                    and string.format('%q', 0/0) == '(0/0)'
                    and string.format('%a', 1.0) == '0x1p+0'
                    and string.format('%a', 0.1) == '0x1.999999999999ap-4'
                    and string.format('%.17g', 0.1) == '0.10000000000000001'
                    and string.format('%08.3g', 1.5) == '000001.5'
                    and string.format('%G', 0/0) == 'NAN'
                    and string.format('%s', nil) == 'nil'
                    and string.format('%s', 1/3) == '0.33333333333333'
                    and string.format('%p', 1) == '(null)'
                    and #string.format('%p', {}) == 16
                """).asBoolean());
    }

    @Test
    public void testInvalidArgumentsAreCatchable() {
        assertFalse(eval("local ok = pcall(function() string.char(256) end); return ok").asBoolean());
        assertFalse(eval("local ok = pcall(function() string.match('a', '[') end); return ok").asBoolean());
        assertFalse(eval("local ok = pcall(function() string.gsub('a', 'a', true) end); return ok").asBoolean());
        assertFalse(eval("local ok = pcall(function() string.dump(print) end); return ok").asBoolean());
    }

    @Test
    public void testPackPacksizeAndUnpack() {
        assertTrue(eval("""
                local packed = string.pack('<i2I2fdc3z', -123, 60000, 1.5, 2.25, 'abc', 'end')
                local signed, unsigned, floatValue, doubleValue, fixed, zero, nextPosition =
                    string.unpack('<i2I2fdc3z', packed)

                local lengthPacked = string.pack('>s2', 'hello')
                local lengthValue, lengthNext = string.unpack('>s2', lengthPacked)

                local high, low = string.byte(string.pack('>I2', 0x1234), 1, 2)

                return signed == -123 and unsigned == 60000
                    and floatValue == 1.5 and doubleValue == 2.25
                    and fixed == 'abc' and zero == 'end'
                    and nextPosition == string.len(packed) + 1
                    and lengthValue == 'hello' and lengthNext == string.len(lengthPacked) + 1
                    and high == 0x12 and low == 0x34
                    and string.packsize('<i2I2fdc3') == 19
                """).asBoolean());
    }

    @Test
    public void testStringMethodSyntaxUsesStringMetatable() {
        assertEquals("ABC", eval("return ('abc'):upper()").asString());
        assertEquals("bc", eval("return ('abc'):sub(2)").asString());
    }
}
