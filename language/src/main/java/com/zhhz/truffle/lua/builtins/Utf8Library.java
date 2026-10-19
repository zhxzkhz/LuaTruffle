package com.zhhz.truffle.lua.builtins;

import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.io.ByteArrayOutputStream;

/**
 * Lua 5.3 标准库 {@code utf8} 的字节级实现。
 *
 * <p>Lua 字符串是字节串，因此这里全部基于 {@code byte[]} 操作，UTF-8 编解码严格遵循
 * lutf8lib.c 中 {@code utf8_decode}/{@code pushutfchar} 的校验规则（拒绝过长编码与越界码点）。
 */
public final class Utf8Library {

    private static final long MAX_UNICODE = 0x10FFFFL;

    /** 与 lutf8lib.c 的 UTF8PATT 完全一致（Latin-1 字节串）。 */
    public static final String CHARPATTERN = "[\u0000-\u007F\u00C2-\u00F4][\u0080-\u00BF]*";

    private static final int[] LIMITS = {0xFF, 0x7F, 0x7FF, 0xFFFF};

    private Utf8Library() {
    }

    @LuaBuiltin(name = "char")
    public static Object utf8Char(Object[] arguments) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(arguments.length);
        for (int i = 0; i < arguments.length; i++) {
            long code = LuaStringSupport.ensureInteger(arguments, i, "char");
            if (code < 0 || code > MAX_UNICODE) {
                throw LuaException.create("bad argument #" + (i + 1) + " to 'char' (value out of range)", null);
            }
            pushUtf8Char(output, code);
        }
        return LuaStringSupport.fromBytes(output.toByteArray());
    }

    @LuaBuiltin(name = "codepoint")
    public static Object codepoint(Object[] arguments) {
        byte[] source = LuaStringSupport.ensureBytes(arguments, 0, "codepoint");
        long posi = relative(optionalInteger(arguments, 1, "codepoint", 1L), source.length);
        long pose = relative(optionalInteger(arguments, 2, "codepoint", posi), source.length);
        if (posi < 1) {
            throw LuaException.create("bad argument #2 to 'codepoint' (out of range)", null);
        }
        if (pose > source.length) {
            throw LuaException.create("bad argument #3 to 'codepoint' (out of range)", null);
        }
        if (posi > pose) {
            return new LuaMultiValue(new Object[0]);
        }
        int[] values = new int[(int) (pose - posi) + 1];
        int count = 0;
        int position = (int) (posi - 1);
        int end = (int) pose;
        int[] out = new int[1];
        while (position < end) {
            int next = decode(source, position, out);
            if (next < 0) {
                throw LuaException.create("invalid UTF-8 code", null);
            }
            values[count++] = out[0];
            position = next;
        }
        Object[] result = new Object[count];
        for (int i = 0; i < count; i++) {
            result[i] = (long) values[i];
        }
        return new LuaMultiValue(result);
    }

    @LuaBuiltin(name = "len")
    public static Object len(Object[] arguments) {
        byte[] source = LuaStringSupport.ensureBytes(arguments, 0, "len");
        long posi = relative(optionalInteger(arguments, 1, "len", 1L), source.length);
        long posj = relative(optionalInteger(arguments, 2, "len", -1L), source.length);
        if (posi < 1 || (posi - 1) > source.length) {
            throw LuaException.create("bad argument #2 to 'len' (initial position out of string)", null);
        }
        if ((posj - 1) >= source.length) {
            throw LuaException.create("bad argument #3 to 'len' (final position out of string)", null);
        }
        long count = 0;
        int position = (int) (posi - 1);
        int end = (int) (posj - 1);
        int[] out = new int[1];
        while (position <= end) {
            int next = decode(source, position, out);
            if (next < 0) {
                return new LuaMultiValue(new Object[]{LuaNil.SINGLETON, (long) position + 1});
            }
            position = next;
            count++;
        }
        return count;
    }

    @LuaBuiltin(name = "offset")
    public static Object offset(Object[] arguments) {
        byte[] source = LuaStringSupport.ensureBytes(arguments, 0, "offset");
        long n = LuaStringSupport.ensureInteger(arguments, 1, "offset");
        long defaultPos = n >= 0 ? 1L : (long) source.length + 1;
        long posi = relative(optionalInteger(arguments, 2, "offset", defaultPos), source.length);
        if (posi < 1 || (posi - 1) > source.length) {
            throw LuaException.create("bad argument #3 to 'offset' (position out of range)", null);
        }
        int position = (int) (posi - 1);
        if (n == 0) {
            while (position > 0 && isContinuation(source[position])) {
                position--;
            }
        } else {
            if (position < source.length && isContinuation(source[position])) {
                throw LuaException.create("initial position is a continuation byte", null);
            }
            if (n < 0) {
                while (n < 0 && position > 0) {
                    do {
                        position--;
                    } while (position > 0 && isContinuation(source[position]));
                    n++;
                }
            } else {
                n--;
                while (n > 0 && position < source.length) {
                    position++;
                    while (position < source.length && isContinuation(source[position])) {
                        position++;
                    }
                    n--;
                }
            }
        }
        return n == 0 ? (long) position + 1 : LuaNil.SINGLETON;
    }

    /** 将单个码点写入 UTF-8 字节序列。 */
    static void pushUtf8Char(ByteArrayOutputStream output, long code) {
        if (code < 0x80) {
            output.write((int) code);
        } else if (code < 0x800) {
            output.write((int) (0xC0 | (code >> 6)));
            output.write((int) (0x80 | (code & 0x3F)));
        } else if (code < 0x10000) {
            output.write((int) (0xE0 | (code >> 12)));
            output.write((int) (0x80 | ((code >> 6) & 0x3F)));
            output.write((int) (0x80 | (code & 0x3F)));
        } else {
            output.write((int) (0xF0 | (code >> 18)));
            output.write((int) (0x80 | ((code >> 12) & 0x3F)));
            output.write((int) (0x80 | ((code >> 6) & 0x3F)));
            output.write((int) (0x80 | (code & 0x3F)));
        }
    }

    /** 是否为 UTF-8 续接字节（0b10xxxxxx）。 */
    static boolean isContinuation(byte value) {
        return (value & 0xC0) == 0x80;
    }

    /**
     * 解码 {@code source[position]} 处的一个 UTF-8 序列。
     *
     * @return 下一个字节位置；{@code -1} 表示非法序列。解码出的码点写入 {@code out[0]}（可为 null）。
     */
    static int decode(byte[] source, int position, int[] out) {
        int first = source[position] & 0xFF;
        if (first < 0x80) {
            if (out != null) {
                out[0] = first;
            }
            return position + 1;
        }
        int result = 0;
        int count = 0;
        int current = first;
        while ((current & 0x40) != 0) {
            count++;
            if (position + count >= source.length) {
                return -1;
            }
            int continuation = source[position + count] & 0xFF;
            if ((continuation & 0xC0) != 0x80) {
                return -1;
            }
            result = (result << 6) | (continuation & 0x3F);
            current = (current << 1) & 0xFF;
        }
        result |= (current & 0x7F) << (count * 5);
        if (count > 3 || result > MAX_UNICODE || result <= LIMITS[count]) {
            return -1;
        }
        if (out != null) {
            out[0] = result;
        }
        return position + count + 1;
    }

    private static long optionalInteger(Object[] arguments, int index, String function, long fallback) {
        if (arguments.length <= index || LuaStringSupport.isNil(arguments[index])) {
            return fallback;
        }
        return LuaStringSupport.ensureInteger(arguments, index, function);
    }

    /** 对应 lutf8lib.c 的 u_posrelat：负索引从字符串末尾回数。 */
    static long relative(long pos, int length) {
        return pos >= 0 ? pos : (long) length + pos + 1;
    }
}
