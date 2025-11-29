package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.*;

// --- 内部辅助节点 ---
// 这个节点只负责接收具体的值，并根据类型进行特化写入。
// 它与你的整体架构完全解耦。
@NodeInfo(language = "Lua")
public abstract class WriteHelperNode extends Node {

    public abstract void executeWrite(Object table, Object key, Object value);

    @Specialization(limit = "3")
    protected void writeNonTable(LuaTable table, Object key, Object value,
                                 @CachedLibrary("table") DynamicObjectLibrary objLib,
                                 @CachedLibrary(limit = "3") InteropLibrary interop) {
        slowPathWrite(table, key, value, objLib, interop);
    }

    /**
     * 【核心慢路径】处理 __newindex 逻辑。
     *
     * @return {@code true} 如果一个元方法被成功调用，否则 {@code false}。
     */
    @CompilerDirectives.TruffleBoundary
    private boolean notHandleNewIndex(Object table, Object key, Object value, DynamicObjectLibrary objLib, InteropLibrary interop) {
        // 1. 获取有效元表 (自身的或类型的)
        LuaTable metatable = LuaContext.get(this).getEffectiveMetatable(table);
        if (metatable == null) {
            return true;
        }

        // 2. 查找 __newindex
        Object newIndexMeta = metatable.rawget(LuaMetatables.__NEWINDEX, objLib);

        if (newIndexMeta == null || newIndexMeta == LuaNil.SINGLETON) {
            return true;
        }

        // 3. 处理 __newindex 的两种情况
        if (newIndexMeta instanceof LuaFunction) {
            // 【情况 A】__newindex 是一个函数
            try {
                interop.execute(newIndexMeta, table, key, value);
                return false; // 元方法被调用
            } catch (Exception e) {
                throw LuaException.create("error in __newindex metamethod > " + e.getMessage(), this);
            }
        }

        if (newIndexMeta instanceof LuaTable) {
            // 【情况 B】__newindex 是一个 table (重定向)
            // 在那个 table 上【递归地】进行写入操作。
            // 为了安全，我们再次调用慢路径写入。
            slowPathWrite((LuaTable) newIndexMeta, key, value, objLib, interop);
            return false; // 元方法被处理
        }

        return true;
    }

    // 我们需要一个可以从任何地方调用的慢路径写入，用于递归
    @CompilerDirectives.TruffleBoundary
    private void slowPathWrite(LuaTable table, Object key, Object value, DynamicObjectLibrary objLib, InteropLibrary interop) {
        // 这个方法包含了与 writeTable 中完全相同的逻辑
        Object oldValue = table.rawget(key, objLib);
        if (oldValue != LuaNil.SINGLETON || notHandleNewIndex(table, key, value, objLib, interop)) {
            table.rawset(key, value);
        }
    }
}