package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaNil;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName;
import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.isTruthy;
import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

/**
 * Lua 标准库 'string' 的实现。
 * <p>
 * 注意：Lua 的模式匹配 (Pattern Matching) 与 Java 的 Regex 不完全相同。
 * 这里的部分实现（如 find, match, gsub）为了简化，使用了 Java 的 Regex 或者简单的字符串操作。
 * 如果需要 100% 兼容 Lua 的模式匹配（如 %a, %d, 平衡匹配等），需要编写专门的 Lua 模式解析器。
 */
public final class StringLibrary {

    // --- 基础操作 ---

    @LuaBuiltin(name = "len")
    @TruffleBoundary
    public static long len(Object[] args) {
        String str = ensureString(args, 0);
        // Lua 的 len 通常指字节长度，但在 Java/Truffle 中通常指字符长度
        // 如果需要精确的字节长度，应该使用 getByteCount
        return str.length();
    }

    @LuaBuiltin(name = "upper")
    @TruffleBoundary
    public static TruffleString upper(Object[] args) {
        String str = ensureString(args, 0);
        return toTruffleString(str.toUpperCase());
    }

    @LuaBuiltin(name = "lower")
    @TruffleBoundary
    public static TruffleString lower(Object[] args) {
        String str = ensureString(args, 0);
        return toTruffleString(str.toLowerCase());
    }

    @LuaBuiltin(name = "reverse")
    @TruffleBoundary
    public static TruffleString reverse(Object[] args) {
        String str = ensureString(args, 0);
        return toTruffleString(new StringBuilder(str).reverse().toString());
    }

    @LuaBuiltin(name = "rep")
    @TruffleBoundary
    public static TruffleString rep(Object[] args) {
        String str = ensureString(args, 0);
        long n = ensureLong(args, 1);
        String sep = (args.length > 2) ? ensureString(args, 2) : "";

        if (n <= 0) return toTruffleString("");
        if (n == 1) return toTruffleString(str);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(sep);
            sb.append(str);
        }
        return toTruffleString(sb.toString());
    }

    @LuaBuiltin(name = "sub")
    @TruffleBoundary
    public static TruffleString sub(Object[] args) {
        String str = ensureString(args, 0);
        int len = str.length();

        long i = ensureLong(args, 1);
        long j = (args.length > 2) ? ensureLong(args, 2) : -1;

        // 处理 Lua 的索引规则 (1-based, 负数代表倒数)
        int start = normalizeIndex(i, len);
        int end = normalizeIndex(j, len);

        // Lua sub 的范围是闭区间 [start, end]
        if (start > end) {
            return toTruffleString("");
        }

        // 转换为 Java 的 substring (0-based, end exclusive)
        // Lua index 1 -> Java index 0
        int javaStart = Math.max(0, start - 1);
        int javaEnd = Math.min(len, end);

        if (javaStart >= javaEnd) {
            return toTruffleString("");
        }

        return toTruffleString(str.substring(javaStart, javaEnd));
    }

    // --- 字符与编码 ---

    @LuaBuiltin(name = "char")
    @TruffleBoundary
    public static TruffleString charFunc(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            long code = ensureLong(args, i);
            sb.append((char) code);
        }
        return toTruffleString(sb.toString());
    }

    @LuaBuiltin(name = "byte")
    @TruffleBoundary
    public static Object byteFunc(Object[] args) {
        String str = ensureString(args, 0);
        int len = str.length();

        long i = (args.length > 1) ? ensureLong(args, 1) : 1;
        long j = (args.length > 2) ? ensureLong(args, 2) : i;

        int start = normalizeIndex(i, len);
        int end = normalizeIndex(j, len);

        if (start > end) {
            return LuaNil.SINGLETON;
        }

        // 限制在有效范围内
        start = Math.max(1, start);
        end = Math.min(len, end);

        if (start > end) return LuaNil.SINGLETON;

        // 返回多个值 (Object[])
        int count = end - start + 1;
        Object[] result = new Object[count];
        for (int k = 0; k < count; k++) {
            result[k] = (long) str.charAt((start - 1) + k);
        }

        // 如果只有一个结果，返回单个值，否则返回数组
        if (result.length == 1) return result[0];
        return result;
    }

    // --- 格式化 ---

    @LuaBuiltin(name = "format")
    @TruffleBoundary
    public static TruffleString format(Object[] args) {
        String formatStr = ensureString(args, 0);
        Object[] formatArgs = new Object[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof TruffleString) {
                formatArgs[i - 1] = ((TruffleString) arg).toJavaStringUncached();
            } else {
                formatArgs[i - 1] = arg;
            }
        }

        // 注意：Java 的 String.format 与 Lua 的 printf 规范大部分兼容，
        // 但有些细微差别（例如 %q, %p）。这里直接使用 Java 的实现。
        try {
            return toTruffleString(String.format(formatStr, formatArgs));
        } catch (Exception e) {
            throw LuaException.create("bad argument #1 to 'format' (" + e.getMessage() + ")",null);
        }
    }

    // --- 查找与匹配 (简化版) ---
    // TODO: 后续待实现完整版
    @LuaBuiltin(name = "find")
    @TruffleBoundary
    public static Object find(Object[] args) {
        String s = ensureString(args, 0);
        String pattern = ensureString(args, 1);
        long init = (args.length > 2) ? ensureLong(args, 2) : 1;
        boolean plain = (args.length > 3) && isTruthy(args[3]);

        int startInit = normalizeIndex(init, s.length());
        int javaStartInit = Math.max(0, startInit - 1);

        int foundStart = -1;
        int foundEnd = -1;

        if (plain) {
            // 纯文本查找
            foundStart = s.indexOf(pattern, javaStartInit);
            if (foundStart != -1) {
                foundEnd = foundStart + pattern.length();
            }
        } else {
            // 使用 Java Regex 模拟 Lua Pattern (注意：不完全兼容)
            // 实际的 Lua 实现需要一个专门的 Pattern 编译器
            try {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(s);
                if (m.find(javaStartInit)) {
                    foundStart = m.start();
                    foundEnd = m.end();
                    // TODO: 处理捕获组
                }
            } catch (Exception e) {
                throw LuaException.create("invalid pattern",null);
            }
        }

        if (foundStart != -1) {
            // 返回起始索引和结束索引 (Lua 1-based)
            return new Object[]{ (long)(foundStart + 1), (long)foundEnd };
        } else {
            return LuaNil.SINGLETON;
        }
    }


    @LuaBuiltin(name = "gsub")
    @TruffleBoundary
    public static Object gsub(Object[] args) {

        // 1. 参数校验
        if (args.length < 3) {
            throw LuaException.create("bad argument #1 to 'gsub' (string expected)", null);
        }

        // TODO: 后续待实现gsub
        throw LuaException.create("gsub 暂未实现", null);
    }



    private static String ensureString(Object[] args, int index) {
        if (index >= args.length) {
            throw LuaException.create("bad argument #" + (index + 1) + " (string expected, got no value)",null);
        }
        Object val = args[index];
        if (val instanceof TruffleString) {
            return ((TruffleString) val).toJavaStringUncached();
        } else if (val instanceof String) {
            return (String) val;
        } else if (val instanceof Long || val instanceof Double) {
            return String.valueOf(val);
        }
        throw LuaException.create("bad argument #" + (index + 1) + " (string expected, got " + getTypeName(val) + ")",null);
    }

    private static long ensureLong(Object[] args, int index) {
        if (index >= args.length) {
            throw LuaException.create("bad argument #" + (index + 1) + " (number expected, got no value)",null);
        }
        Object val = args[index];
        if (val instanceof Long) {
            return (Long) val;
        } else if (val instanceof Double) {
            return ((Double) val).longValue();
        } else if (val instanceof TruffleString) {
            try {
                return Long.parseLong(((TruffleString) val).toJavaStringUncached());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        throw LuaException.create("bad argument #" + (index + 1) + " (number expected, got " + getTypeName(val) + ")",null);
    }

    /**
     * 处理 Lua 的索引转换逻辑：
     * 1. 负数表示从末尾倒数
     * 2. 结果可能超出范围 [1, len]
     */
    private static int normalizeIndex(long index, int len) {
        if (index >= 0) {
            return (int) index;
        } else {
            return len + (int) index + 1;
        }
    }
}