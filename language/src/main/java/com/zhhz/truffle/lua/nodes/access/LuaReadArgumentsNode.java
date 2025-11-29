package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;

/**
 * 一个特殊的节点，用于从 VirtualFrame 中提取所有用户参数，
 * 并将它们打包成一个 Object[] 数组。
 */
public class LuaReadArgumentsNode extends LuaExpressionNode {

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object[] arguments = frame.getArguments();

        // 跳过第一个参数 (父Frame)，只返回用户参数
        Object[] userArgs = new Object[Math.max(0, arguments.length - 1)];
        System.arraycopy(arguments, 1, userArgs, 0, userArgs.length);
        return userArgs;
    }
}