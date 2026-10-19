package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.nodes.ControlFlowException;

import java.io.Serial;

/**
 * 尾调用优化（Tail Call Optimization）的控制流异常。
 * <p>
 * 当 Lua 执行 {@code return f(args)} 形式的尾调用时，若直接递归调用会不断增长
 * Java 调用栈，最终导致 {@code StackOverflowError}。Lua 语义要求尾调用不增加
 * 栈深度（参考 Lua 5.3 参考手册 3.4.10）。
 * <p>
 * 本异常由尾位置的调用节点在执行前抛出，携带目标函数与实参数组，由
 * {@link com.zhhz.truffle.lua.nodes.LuaAstRootNode} 的顶层执行循环捕获，
 * 用新参数重新调用同一 {@link com.oracle.truffle.api.CallTarget}，从而在
 * 固定的 Java 栈深度下完成任意深度的尾递归。
 */
public final class LuaTailCallException extends ControlFlowException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final LuaFunction function;
    private final Object[] arguments;

    public LuaTailCallException(LuaFunction function, Object[] arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    public LuaFunction getFunction() {
        return function;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
