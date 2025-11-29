package com.zhhz.truffle.lua.builtins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LuaBuiltin {
    /**
     * Lua函数的名称。如果未指定，则使用Java方法名。
     */
    String name() default "";
}