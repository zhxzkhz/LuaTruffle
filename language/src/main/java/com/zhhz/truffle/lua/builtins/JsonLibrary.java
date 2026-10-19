package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

/**
 * `json.encode(value)`：把 Lua 值编码为 JSON 文本。
 * <p>
 * 规则（与常见 Lua-JSON 库一致）：
 * <ul>
 *   <li>nil → {@code null}；boolean → {@code true}/{@code false}；数字按十进制输出
 *       （NaN / Infinity 不可表示，报错）；字符串按 UTF-8 还原并做 JSON 转义；</li>
 *   <li>table：若为从 1 开始的连续非 nil 序列（纯数组）则输出 JSON 数组；
 *       否则（稀疏整数键或混合表）整个表输出为 JSON 对象——字符串键原样，数字键转为字符串键
 *       （如 {@code [1111]=x} → {@code "1111": ...}），其他类型的键（boolean/table 等）跳过，
 *       以保证稀疏数组的条目不丢失；</li>
 *   <li>循环引用（table 直接或间接包含自身）报错；function / userdata 等不可编码值报错。</li>
 * </ul>
 */
@GenerateInline(false)
public final class JsonLibrary {

    private JsonLibrary() {
    }

    @LuaBuiltin(name = "encode")
    @TruffleBoundary
    public static Object encode(Object[] arguments) {
        Object value = arguments.length > 0 ? arguments[0] : LuaNil.SINGLETON;
        StringBuilder out = new StringBuilder();
        encodeValue(value, out, new IdentityHashMap<>());
        // 转回字节模型（UTF-8 字节 → Latin-1 字符）：JSON 文本以 UTF-8 字节存储，
        // 经 print 直通输出还原正确的中文；直接 toTruffleString 会把真实 Unicode
        // 字符按 ISO-8859-1 压成 '?'。
        String byteModel = new String(out.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        return toTruffleString(byteModel);
    }

    private static void encodeValue(Object value, StringBuilder out, Map<LuaTable, Boolean> visiting) {
        if (value == null || value == LuaNil.SINGLETON) {
            out.append("null");
        } else if (value instanceof LuaBoolean bool) {
            out.append(bool.getValue());
        } else if (value instanceof Boolean javaBool) {
            out.append(javaBool);
        } else if (value instanceof Long longValue) {
            out.append(longValue);
        } else if (value instanceof Double doubleValue) {
            if (!Double.isFinite(doubleValue)) {
                throw LuaException.create("Cannot encode number in JSON: " + doubleValue, null);
            }
            out.append(doubleValue);
        } else if (value instanceof TruffleString string) {
            appendJsonString(out, decodeUtf8(string));
        } else if (value instanceof String javaString) {
            appendJsonString(out, javaString);
        } else if (value instanceof LuaTable table) {
            if (visiting.put(table, Boolean.TRUE) != null) {
                throw LuaException.create("Cannot encode table with a cycle in JSON", null);
            }
            try {
                encodeTable(table, out, visiting);
            } finally {
                visiting.remove(table);
            }
        } else {
            throw LuaException.create("Cannot encode value of type '" + typeName(value) + "' in JSON", null);
        }
    }

    private static void encodeTable(LuaTable table, StringBuilder out, Map<LuaTable, Boolean> visiting) {
        // 数组部分：从 1 起的连续非 nil 序列
        List<Object> sequence = new ArrayList<>();
        for (long i = 1; ; i++) {
            Object item = table.rawget(i);
            if (item == LuaNil.SINGLETON) {
                break;
            }
            sequence.add(item);
        }
        // 收集连续序列之外的所有键：稀疏整数键（如 [1]=x, [3]=y 的 [3]）与哈希键
        List<String> jsonKeys = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        collectEntriesOutsideSequence(table, sequence.size(), jsonKeys, values);
        if (!jsonKeys.isEmpty()) {
            // 存在序列之外的键（稀疏数组 / 混合表）：整个表输出为 JSON 对象，
            // 数字键转为字符串键，保证条目不丢失。纯数组时才输出 JSON 数组。
            for (int i = 0; i < sequence.size(); i++) {
                jsonKeys.add(Long.toString(i + 1));
                values.add(sequence.get(i));
            }
            sortByKey(jsonKeys, values);
            out.append('{');
            for (int i = 0; i < jsonKeys.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                appendJsonString(out, jsonKeys.get(i));
                out.append(':');
                encodeValue(values.get(i), out, visiting);
            }
            out.append('}');
            return;
        }
        if (!sequence.isEmpty()) {
            out.append('[');
            for (int i = 0; i < sequence.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                encodeValue(sequence.get(i), out, visiting);
            }
            out.append(']');
            return;
        }
        // 空 table → 空对象
        out.append('{');
        out.append('}');
    }

    /**
     * 收集表中位于“从 1 起连续序列”之外的所有可编码键值对：
     * <ul>
     *   <li>数组部分中空洞之后的非 null 槽位（稀疏整数键）；</li>
     *   <li>哈希部分的字符串键与数字键（数字键转为字符串）。</li>
     * </ul>
     * boolean/table 等不可作为 JSON 键的类型跳过。
     */
    private static void collectEntriesOutsideSequence(LuaTable table, int sequenceLen,
                                                      List<String> jsonKeys, List<Object> values) {
        // 1) 数组部分：跳过连续序列已覆盖的槽位，收集其后的稀疏条目
        Object[] arrayPart = table.getArrayPart();
        int arraySize = table.getArraySize();
        for (int i = sequenceLen; i < arraySize; i++) {
            Object value = arrayPart[i];
            if (value == null) {
                continue; // 稀疏空洞本身
            }
            jsonKeys.add(Long.toString(i + 1L));
            values.add(value);
        }
        // 2) 哈希部分
        Object[] keys = com.oracle.truffle.api.object.DynamicObjectLibrary.getUncached().getKeyArray(table);
        for (Object key : keys) {
            if (table.rawget(key) == LuaNil.SINGLETON) {
                continue; // 跳过“死键”（值为 nil 的保留键）
            }
            String jsonKey;
            if (key instanceof TruffleString ts) {
                jsonKey = decodeUtf8(ts);
            } else if (key instanceof Long number) {
                if (number >= 1 && number <= sequenceLen) {
                    continue; // 已被连续序列覆盖，避免重复
                }
                int idx = number.intValue();
                if (number == idx && idx >= 1 && idx <= arraySize && arrayPart[idx - 1] != null) {
                    continue; // 数组部分已收集该键，避免重复
                }
                jsonKey = number.toString();
            } else if (key instanceof Double number) {
                double d = number;
                if (d >= 1 && d <= sequenceLen && d == Math.rint(d)) {
                    continue; // 已被连续序列覆盖，避免重复
                }
                int idx = (int) d;
                if (d == idx && idx >= 1 && idx <= arraySize && arrayPart[idx - 1] != null) {
                    continue; // 数组部分已收集该键，避免重复
                }
                jsonKey = number.toString();
            } else {
                continue; // boolean/table 等非 string/number 键：跳过
            }
            jsonKeys.add(jsonKey);
            values.add(table.rawget(key));
        }
    }

    /** 键排序：数字键数字序在前，字符串键按字典序（保证输出确定，方便测试比对）。 */
    private static void sortByKey(List<String> jsonKeys, List<Object> values) {
        Integer[] indices = new Integer[jsonKeys.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        List<String> keys = jsonKeys;
        java.util.Arrays.sort(indices, (a, b) -> {
            boolean na = isNumericKey(keys.get(a));
            boolean nb = isNumericKey(keys.get(b));
            if (na != nb) {
                return na ? -1 : 1;
            }
            if (na) {
                return Double.compare(Double.parseDouble(keys.get(a)), Double.parseDouble(keys.get(b)));
            }
            return keys.get(a).compareTo(keys.get(b));
        });
        List<String> sortedKeys = new ArrayList<>(jsonKeys.size());
        List<Object> sortedValues = new ArrayList<>(jsonKeys.size());
        for (Integer index : indices) {
            sortedKeys.add(jsonKeys.get(index));
            sortedValues.add(values.get(index));
        }
        jsonKeys.clear();
        jsonKeys.addAll(sortedKeys);
        values.clear();
        values.addAll(sortedValues);
    }

    private static boolean isNumericKey(String key) {
        return key.chars().allMatch(Character::isDigit) && !key.isEmpty();
    }

    /** 字节模型字符串（Latin-1 存储）按 UTF-8 还原为文本。 */
    private static String decodeUtf8(TruffleString string) {
        String latin1 = string.toJavaStringUncached();
        return new String(latin1.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    /** JSON 字符串转义：引号、反斜杠与控制字符（&lt;0x20）转义为 \\uXXXX。 */
    private static void appendJsonString(StringBuilder out, String text) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    private static String typeName(Object value) {
        return com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName(value);
    }

}