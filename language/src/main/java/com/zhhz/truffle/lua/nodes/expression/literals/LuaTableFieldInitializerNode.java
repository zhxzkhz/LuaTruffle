package com.zhhz.truffle.lua.nodes.expression.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.zhhz.truffle.lua.runtime.LuaTable;


/**
 * table 构造器中【单个字段初始化器】的抽象基类。
 * <p>
 * 它定义了一个契约：任何子类都必须实现一个 `executeVoid` 方法，
 * 该方法接收一个正在被构建的 `LuaTable` 对象，并负责向其中添加一个字段。
 * <p>
 * 这个节点本身不可实例化。
 */
public abstract class LuaTableFieldInitializerNode extends Node {

    /**
     * 这是所有字段初始化器必须实现的“合同”方法。
     *
     * @param frame The current execution frame, used to evaluate key/value expressions.
     * @param table The `LuaTable` object that is currently being constructed.
     */
    public abstract void executeVoid(VirtualFrame frame, LuaTable table);
}
