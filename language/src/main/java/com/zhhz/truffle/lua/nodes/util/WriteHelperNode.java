package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.*;

// --- 内部辅助节点 ---
// 这个节点只负责接收具体的值，并根据类型进行特化写入。
// 它与你的整体架构完全解耦。
@SuppressWarnings("deprecation")
@GenerateInline(false)
@NodeInfo(language = "Lua")
public abstract class WriteHelperNode extends Node {

    public abstract void executeWrite(Object table, Object key, Object value);

    @Specialization(limit = "3")
    protected void writeNonTable(LuaTable table, Object key, Object value,
                                 @CachedLibrary("table") DynamicObjectLibrary objLib,
                                 @CachedLibrary(limit = "3") InteropLibrary interop) {
        slowPathWrite(this, table, key, value, objLib, interop);
    }

    // 宿主对象（Java 对象）成员写入，如 param.size = 32。
    // 与 LuaReadTableNode.doHostObject 的读路径对称。
    // 注意：LuaTable 也实现 TruffleObject，LuaValue（nil/boolean/number/string 等）同样如此，
    // 因此本特化必须带 isForeignObject 卫语句，避免把 Lua 值误当成宿主对象。
    @Specialization(guards = "isForeignObject(object)", limit = "3")
    protected void writeHostObject(TruffleObject object, Object key, Object value,
                                   @CachedLibrary("object") InteropLibrary objects) {
        long arrayIndex = LuaTypesUtil.hostArrayIndex(key);
        if (arrayIndex != Long.MIN_VALUE && objects.hasArrayElements(object)) {
            long hostIndex = arrayIndex - 1;
            try {
                if (arrayIndex < 1 || hostIndex >= objects.getArraySize(object)
                        || !objects.isArrayElementWritable(object, hostIndex)) {
                    throw cannotWriteArrayElement(this, key);
                }
                objects.writeArrayElement(object, hostIndex, value);
                return;
            } catch (UnsupportedMessageException e) {
                throw cannotWriteMember(this, key);
            } catch (com.oracle.truffle.api.interop.InvalidArrayIndexException e) {
                throw cannotWriteArrayElement(this, key);
            } catch (UnsupportedTypeException e) {
                throw unsupportedTypeError(this, key, value);
            }
        }
        // 宿主 Map 等以 hash entries 暴露键值，如 config.name = 'x'。
        if (objects.hasHashEntries(object)) {
            Object hashKey = LuaTypesUtil.hostHashKey(key);
            try {
                if (objects.isHashEntryModifiable(object, hashKey)
                        || objects.isHashEntryInsertable(object, hashKey)) {
                    objects.writeHashEntry(object, hashKey, value);
                    return;
                }
            } catch (UnsupportedMessageException e) {
                throw cannotWriteMember(this, key);
            } catch (com.oracle.truffle.api.interop.UnknownKeyException e) {
                throw LuaException.undefinedProperty(this, key);
            } catch (UnsupportedTypeException e) {
                throw unsupportedTypeError(this, key, value);
            }
        }
        try {
            objects.writeMember(object, toMemberName(key), value);
        } catch (UnsupportedMessageException e) {
            throw cannotWriteMember(this, key);
        } catch (UnknownIdentifierException e) {
            throw LuaException.undefinedProperty(this, key);
        } catch (UnsupportedTypeException e) {
            throw unsupportedTypeError(this, key, value);
        }
    }

    @CompilerDirectives.TruffleBoundary
    private static RuntimeException cannotWriteArrayElement(Node node, Object key) {
        return LuaException.create("cannot write to host object array element '" + key + "'", node);
    }

    @CompilerDirectives.TruffleBoundary
    private static RuntimeException cannotWriteMember(Node node, Object key) {
        return LuaException.create("cannot write to host object member '" + toMemberName(key) + "'", node);
    }

    @CompilerDirectives.TruffleBoundary
    private static RuntimeException unsupportedTypeError(Node node, Object key, Object value) {
        return LuaException.create("unsupported value type for host object member '" + toMemberName(key)
            + "': " + (value == null ? "null" : LuaException.simpleNameOf(value)), node);
    }

    @CompilerDirectives.TruffleBoundary
    private static String toMemberName(Object key) {
        if (key instanceof TruffleString ts) {
            return ts.toJavaStringUncached();
        }
        return String.valueOf(key);
    }

    /** 宿主对象：非 Lua 值且非 Lua 字符串（Java 对象等）。 */
    static boolean isForeignObject(Object value) {
        return !(value instanceof LuaValue) && !(value instanceof TruffleString);
    }

    // 非 table、非宿主对象（数字、字符串、函数等）不可写入成员；
    // LuaTable 走到此处说明上面的特化缓存已耗尽（Shape 变化导致），用 uncached 库兜底
    @Fallback
    protected void writeUnsupported(Object object, Object key, Object value) {
        if (object instanceof LuaTable luaTable) {
            slowPathWriteUncached(this, luaTable, key, value);
            return;
        }
        throw indexError(this, object);
    }

    @CompilerDirectives.TruffleBoundary
    private static RuntimeException indexError(Node node, Object object) {
        return LuaException.create("attempt to index a " + LuaTypesUtil.getRuntimeTypeName(object) + " value", node);
    }

    /**
     * 供内建库（table.insert/remove 等）以非缓存方式执行遵循 {@code __newindex} 的写入。
     */
    @CompilerDirectives.TruffleBoundary
    public static void writeUncached(Object table, Object key, Object value) {
        if (table instanceof LuaTable luaTable) {
            slowPathWrite(null, luaTable, key, value, DynamicObjectLibrary.getUncached(), InteropLibrary.getUncached());
            return;
        }
        throw LuaException.create("attempt to index a " + LuaTypesUtil.getRuntimeTypeName(table) + " value", null);
    }

    /**
     * 【核心慢路径】处理 {@code __newindex} 逻辑，等价于标准 Lua 的 {@code luaV_settable}。
     * 迭代（而非递归）地沿 __newindex 链前进，并以 MAX_TAG_LOOP 防止自引用死循环。
     */
    @CompilerDirectives.TruffleBoundary
    private static void slowPathWrite(Node node, Object table, Object key, Object value, DynamicObjectLibrary objLib, InteropLibrary interop) {
        // 标准 Lua 语义：table 索引不能为 nil（否则写入会被静默丢弃，难以排查）
        if (key == null || key == LuaNil.SINGLETON) {
            throw LuaException.create("table index is nil", node);
        }

        LuaContext context = LuaContext.get(node);
        Object current = table;
        for (int loop = 0; loop < MAX_TAG_LOOP; loop++) {
            Object newIndexMeta;
            if (current instanceof LuaTable luaTable) {
                Object oldValue = luaTable.rawget(key, objLib);
                if (oldValue != LuaNil.SINGLETON) {
                    luaTable.rawset(key, value, objLib);
                    return;
                }
                LuaTable metatable = context.getEffectiveMetatable(current);
                newIndexMeta = (metatable == null) ? LuaNil.SINGLETON : metatable.rawget(LuaMetatables.__NEWINDEX, objLib);
                if (newIndexMeta == null || newIndexMeta == LuaNil.SINGLETON) {
                    luaTable.rawset(key, value, objLib);
                    return;
                }
            } else {
                LuaTable metatable = context.getEffectiveMetatable(current);
                newIndexMeta = (metatable == null) ? LuaNil.SINGLETON : metatable.rawget(LuaMetatables.__NEWINDEX, objLib);
                if (newIndexMeta == null || newIndexMeta == LuaNil.SINGLETON) {
                    throw LuaException.create("attempt to index a " + LuaTypesUtil.getRuntimeTypeName(current) + " value", node);
                }
            }

            if (newIndexMeta instanceof LuaFunction) {
                try {
                    interop.execute(newIndexMeta, current, key, value);
                    return;
                } catch (Exception e) {
                    throw LuaException.create("error in metamethod '__newindex': " + LuaException.messageOf(e), node);
                }
            }

            // __newindex 是 table：沿链继续（与 luaV_settable 的 't = tm' 一致）
            current = newIndexMeta;
        }
        throw LuaException.create("settable chain too long; possible loop", node);
    }

    /** 对应 Lua 的 MAXTAGLOOP。 */
    private static final int MAX_TAG_LOOP = 2000;

    // LuaTable 专用慢路径（uncached 库版本）：供 Fallback 使用。
    // @CachedLibrary 对 DynamicObjectLibrary 按 Shape 缓存，每次 put 都会产生新 Shape，
    // 缓存条目耗尽 limit 后会落到 Fallback，这里必须保证写入语义与 writeNonTable 一致。
    @CompilerDirectives.TruffleBoundary
    private void slowPathWriteUncached(Node node, LuaTable table, Object key, Object value) {
        slowPathWrite(node, table, key, value, DynamicObjectLibrary.getUncached(), InteropLibrary.getUncached());
    }
}
