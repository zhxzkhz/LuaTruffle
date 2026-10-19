package com.zhhz.truffle.lua.runtime;

/**
 * 一个可变的闭包变量单元（upvalue cell）。
 *
 * <p>当某个局部变量被内层函数捕获为 upvalue 时，它不再直接存放在帧槽中，而是把
 * 一个 {@code LuaCell} 对象放进槽里。闭包在创建时把该 cell 快照进自己的 upvalue
 * 数组，从而：
 * <ul>
 *   <li>同一个声明实例所创建的所有闭包共享同一个 cell（读/写互相可见）；</li>
 *   <li>每次执行声明语句都会创建新的 cell，因此循环体每次迭代捕获的局部变量互相独立；</li>
 *   <li>{@code debug.upvalueid} 可以直接返回 cell 的对象标识。</li>
 * </ul>
 */
public final class LuaCell {

    /** 被捕获变量的当前值。 */
    public Object value;

    /** {@code debug.upvalueid} 使用的稳定身份对象（同一 cell 始终返回同一对象）。 */
    public Object upvalueId;

    public LuaCell(Object value) {
        this.value = value;
    }
}
