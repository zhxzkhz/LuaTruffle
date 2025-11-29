package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

/**
 * 一个“空操作”语句节点。
 * 它的 `executeVoid` 方法是空的，什么也不做。
 * <p>
 * 这个节点非常有用，可以作为：
 * - 空语句 ';' 的 AST 表示。
 * - if 语句缺失的 `else` 分支的占位符。
 * - 空的 `do...end` 块的表示。
 * - 任何在语法上需要一个语句，但逻辑上不需要任何操作的地方。
 */
@NodeInfo(shortName = "nop", description = "A no-operation statement node")
public final class LuaNopNode extends LuaStatementNode {

    /**
     * 【单例模式】
     * 所有的空操作都是一样的，所以我们只需要一个共享的实例。
     */
    public static final LuaNopNode INSTANCE = new LuaNopNode();

    /**
     * 构造函数设为 private，以强制使用单例。
     */
    private LuaNopNode() {
        // 私有构造函数
    }

    /**
     * executeVoid 方法的实现。
     * 【核心】这个方法的方法体是空的。
     *
     * @param frame The current execution frame (ignored).
     */
    @Override
    public void executeVoid(VirtualFrame frame) {
        // Do nothing.
    }
}