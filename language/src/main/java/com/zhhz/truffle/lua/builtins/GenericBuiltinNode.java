package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.zhhz.truffle.lua.LuaException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 一个通用的内建函数节点，通过 Java 反射来调用一个静态方法。<br>
 * <strong>警告：</strong> 这个节点使用了反射，对于性能至关重要的、频繁调用的内建函数来说，
 * 这是一个反模式（anti-pattern）。<br>
 * 它最适合用于快速实现大量不常用或非性能关键的内建函数。<br>
 * 对于核心函数，应为其创建专用的、不使用反射的节点。
 */
@NodeField(name = "targetMethod", type = Method.class)
public abstract class GenericBuiltinNode extends LuaBuiltinNode {

    protected abstract Method getTargetMethod();

    @Specialization
    @CompilerDirectives.TruffleBoundary
    public Object doExecute(Object[] arguments){

        try {
            // 通过反射调用静态方法
            // 我们需要处理有参和无参的情况
            if (getTargetMethod().getParameterCount() == 0) {
                return getTargetMethod().invoke(null);
            } else {
                return getTargetMethod().invoke(null, new Object[]{arguments});
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            // 包装反射异常
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw invalidZeroArgument(e);
        }
    }

    RuntimeException invalidZeroArgument(Exception e) {
        return LuaException.create("Error executing builtin: " + getTargetMethod().getName() + "\n" + e.getMessage(), this);
    }

}
