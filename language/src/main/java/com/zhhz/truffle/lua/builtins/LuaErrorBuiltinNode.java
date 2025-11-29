package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;

/**
 * 实现了Lua的 'error' 内置函数。
 */
@NodeInfo(shortName = "error")
public abstract class LuaErrorBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary // 异常创建和栈遍历是慢速路径
    protected Object doError(Object[] arguments) {
        Object message = LuaNil.SINGLETON;
        long level = 1; // 默认 level 是 1

        // 1. 解析参数
        if (arguments.length >= 1) {
            message = arguments[0];
        }
        if (arguments.length >= 2) {
            if (arguments[1] instanceof LuaNumber) {
                level = (long) ((LuaNumber) arguments[1]).getValue();
            }
            // 如果 level 不是 number，Lua 会报错，我们这里简化为忽略
        }

        // 2. 根据 level 查找错误位置
        Node errorLocation = findErrorLocation(level);

        // 3. 抛出我们统一的运行时异常
        //    我们将 message 对象直接传递给异常
        throw LuaException.create(message.toString(), errorLocation);
    }

    /**
     * 辅助方法，用于根据 'level' 参数在Truffle调用栈中查找错误报告的源节点。
     * 这是一个慢速路径操作。
     *
     * @param level The stack level to report the error at.
     * @return The AST Node at the specified stack level.
     */
    private Node findErrorLocation(long level) {
        if (level == 0) {
            return null; // level 0 表示不报告位置
        }
        if (level == 1) {
            // level 1 (默认) 表示错误位置就是当前节点 (error函数自身)
            // 但在Truffle中，我们更关心调用它的地方，所以向上找一层更有用
            // 不过为了简单，我们先返回 this
            return this;
        }

        // 对于 level > 1，我们需要遍历调用栈
        // Truffle.getRuntime().iterateFrames() 是一个强大的API
        final int[] frameLevel = {0};
        Node[] location = {this}; // 默认位置是当前节点

        Truffle.getRuntime().iterateFrames(frameInstance -> {
            frameLevel[0]++;
            // frameLevel 1 是 error 函数自身
            // frameLevel 2 是调用 error 的函数
            // frameLevel 3 是调用了调用error的函数的函数...
            // 所以，我们要找的栈深度是 level + 1
            if (frameLevel[0] == level + 1) {
                // frameInstance.getCallNode() 返回导致这次调用的AST节点
                Node callNode = frameInstance.getCallNode();
                if (callNode != null) {
                    location[0] = callNode;
                }
                // 找到后可以提前终止遍历
                return false;
            }
            // 继续遍历
            return true;
        });

        return location[0];
    }
}