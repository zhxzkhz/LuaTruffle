package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 一个辅助节点，封装了查找和调用【二元】元方法（如 __add, __concat）的逻辑。
 * 它应该在所有快速路径特化都失败后，在 @Fallback 方法中被调用。
 */
@GenerateUncached
public abstract class LuaMetatableNode extends Node {

    public static LuaMetatableNode create() {
        return LuaMetatableNodeGen.create();
    }

    /**
     * 节点的执行入口。
     * @param left      左操作数
     * @param right     右操作数
     * @param eventName 元方法的名字 (e.g., "__add")
     * @return 元方法的执行结果，如果找不到或无法执行元方法，则返回 null。
     */
    public abstract Object execute(Object left, Object right, TruffleString eventName);

    /**
     * 这个节点只有一个特化，因为它总是处理通用 Object。
     * 整个方法都在 @TruffleBoundary 保护的慢路径中执行。
     */
    @Specialization
    @TruffleBoundary
    protected Object doMetatableOp(Object left, Object right, TruffleString eventName,
                                   // 我们需要库来操作 table 和调用函数
                                   @CachedLibrary(limit = "3") DynamicObjectLibrary objLib,
                                   @CachedLibrary(limit = "3") InteropLibrary interop) {

        // 1. 【查找元方法】
        //    这个查找逻辑本身很复杂，我们把它封装到另一个辅助方法中。
        Object metamethod = findMetamethod(left, right, eventName, objLib);

        // 2. 【检查并调用】
        if (metamethod instanceof LuaFunction) {
            try {
                // 如果找到了一个可执行的元方法，就调用它
                // 参数是 (left, right)
                return interop.execute(metamethod, left, right);
            } catch (LuaException e){
                throw e;
            } catch (Exception e) {
                // 包装在元方法内部发生的错误
                throw LuaException.create("error in metamethod '" + eventName + "'" + "\n" + e.getMessage(),this);
            }
        }

        // 3. 【失败】如果找不到元方法，或者找到的不是一个函数
        return null;
    }

    /**
     * 辅助方法，按照 Lua 规则查找二元元方法。
     */
    private Object findMetamethod(Object o1, Object o2, TruffleString eventName, DynamicObjectLibrary objLib) {
        // 尝试从第一个操作数获取元方法
        Object metamethod1 = getMetamethod(o1, eventName, objLib);
        if (metamethod1 != null) {
            return metamethod1;
        }

        // 如果第一个没有，尝试从第二个操作数获取
        return getMetamethod(o2, eventName, objLib);

        // 都找不到
    }

    /**
     * 辅助方法，从【单个】对象中获取指定的元方法。
     */
    private Object getMetamethod(Object o, TruffleString eventName, DynamicObjectLibrary objLib) {
        if (o == null) return null;

        LuaTable metatable = LuaContext.get(this).getEffectiveMetatable(o);

        if (metatable == null) {
            return null;
        }

        // 从元表中读取对应的字段
        return objLib.getOrDefault(metatable, eventName, null);
    }
}
