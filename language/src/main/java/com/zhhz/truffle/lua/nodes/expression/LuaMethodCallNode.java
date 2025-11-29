package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaFunction;

/**
 * 代表 Lua 方法调用表达式的 AST 节点，例如 't:foo(a, b)'。
 * 这个节点负责处理 ':' 语法糖，即自动将 t 作为第一个参数传入。
 */
public class LuaMethodCallNode extends LuaExpressionNode {

    @Child private LuaExpressionNode targetNode; // 用于计算被调用对象 (例如 't') 的节点
    private final String methodName;    // 方法名 (例如 "foo")
    @Children private final LuaExpressionNode[] argumentNodes; // 用于计算显式参数 (例如 a, b) 的节点数组

    public LuaMethodCallNode(LuaExpressionNode targetNode, String methodName, LuaExpressionNode[] argumentNodes) {
        this.targetNode = targetNode;
        this.methodName = methodName;
        this.argumentNodes = argumentNodes;

        // 我们需要一个子节点来从 targetNode 中读取方法函数
        // 这里为了简化，我们先在 execute 方法里直接处理。
        // 一个更优化的设计会在这里创建一个 ReadPropertyNode。
    }

    @Override
    @ExplodeLoop // 告诉 Truffle 展开这个循环，以提高性能
    public Object executeGeneric(VirtualFrame frame) {
        // 1. 计算出目标对象的值，例如执行完代表 't' 的节点后得到 table 对象。
        Object targetValue = targetNode.executeGeneric(frame);

        // 2. 从目标对象中查找方法函数。
        // 这通常需要一个慢速路径（@TruffleBoundary）的操作来访问对象的属性。
        // 这里我们假设有一个辅助方法 `lookupMethod`。
        LuaFunction function = lookupMethod(targetValue, methodName);

        // --- 这就是与 LuaFunctionCallNode 最关键的区别 ---
        // 3. 准备最终传递给函数的参数数组。
        // 数组的长度是显式参数的长度 + 1。
        final int numArgs = argumentNodes.length;
        Object[] finalArguments = new Object[numArgs + 1];

        // 4. 将目标对象自身作为第一个参数（self）放入数组。
        finalArguments[0] = targetValue;

        // 5. 计算并放入其他的显式参数。
        for (int i = 0; i < numArgs; i++) {
            finalArguments[i + 1] = argumentNodes[i].executeGeneric(frame);
        }

        // 6. 调用函数，并传入包含了 'self' 的完整参数列表。
        return function.getCallTarget().call(finalArguments);
    }

    @TruffleBoundary
    private LuaFunction lookupMethod(Object target, String key) {
        // 在这里实现从一个 Lua 对象（比如 table）中查找函数的逻辑。
        // 这可能涉及到元表（metatable）的查找。
        // if (target instanceof LuaTable) {
        //     return (LuaFunction) ((LuaTable) target).get(key);
        // }
        // ...
        // throw new RuntimeException("Attempt to call a method on a non-table value");
        /*
        // 这是一个伪实现
        if (target instanceof YourLuaTableClass) {
            return (LuaFunction) ((YourLuaTableClass)target).read(key);
        }

         */
        throw new UnsupportedOperationException("Method call on non-table object not implemented");
    }

}