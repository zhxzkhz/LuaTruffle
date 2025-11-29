package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

/**
 * 所有“写入变量”操作节点的抽象基类。
 * <p>
 * 这个类本身不可实例化。它的作用是为所有子类（如 LuaWriteLocalVariableNode,
 * LuaWriteGlobalVariableNode, LuaWriteTableNode 等）定义一个统一的接口。
 * <p>
 * 这个统一的接口（或称“契约”）是 execute(VirtualFrame frame, Object value) 方法。
 * 像 LuaMultiAssignmentNode 这样的父节点就可以依赖这个契约，而不必关心它
 * 到底是在写入哪种类型的变量。
 */
public abstract class LuaWriteVariableNode extends LuaStatementNode {

    /**
     * 这是所有写入节点必须实现的“契约”方法。
     * 它接收从赋值语句右侧计算出的值，并将其写入到该节点所代表的目标位置。
     *
     * @param frame 当前的执行帧，用于访问局部变量等。
     * @param value 要写入变量的值。
     */
    public abstract void execute(VirtualFrame frame,Object value);


    /**
     * 我们必须实现从 LuaStatementNode 继承的 executeVoid 方法。
     * 然而，对于一个写入节点来说，在没有提供值的情况下直接执行它，在逻辑上是错误的。
     * 因此，我们通过抛出一个异常来明确禁止这种用法，这有助于在开发早期发现 AST 构建错误。
     * <p>
     * 这个方法被声明为 final，以防止子类意外地重写它并引入不正确的行为。
     */
    @Override
    public final void executeVoid(VirtualFrame frame) {
        throw new UnsupportedOperationException("A write node cannot be executed without providing a value to write.");
    }
}
