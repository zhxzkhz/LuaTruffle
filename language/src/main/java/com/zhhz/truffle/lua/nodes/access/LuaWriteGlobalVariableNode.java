package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaTable;


/**
 * 将值写入一个全局变量。
 * 这是一个“语句”节点，它消费一个值并执行写入操作，没有返回值。
 */
public abstract class LuaWriteGlobalVariableNode extends LuaWriteVariableNode {

    private final TruffleString name;

    public LuaWriteGlobalVariableNode(TruffleString name) {
        this.name = name;
    }

    protected TruffleString getName() {
        return this.name;
    }

    /**
     * 主特化方法，实现 execute(frame, value) 的逻辑。
     *
     * @param value 要写入的值，由父节点（如 LuaMultiAssignmentNode）在调用 execute 时传入。
     * @param globals 从当前上下文中缓存的全局变量表。
     */
    @Specialization
    protected void doWrite(Object value,
                           @Cached(value = "getContext().getGlobals()", allowUncached = true) LuaTable globals,
                           @CachedLibrary(limit = "3") DynamicObjectLibrary tableLib) {
        // 将实际的表写入操作隔离到 @TruffleBoundary 方法中
        writeToGlobalsBoundary(globals, getName(), value,tableLib);
    }


    @TruffleBoundary
    private static void writeToGlobalsBoundary(LuaTable globals, TruffleString name, Object value,DynamicObjectLibrary tableLib) {
        globals.rawset(name, value,tableLib);
    }
}