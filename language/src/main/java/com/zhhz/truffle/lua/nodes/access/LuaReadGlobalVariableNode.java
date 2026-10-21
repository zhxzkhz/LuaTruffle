package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.expression.LuaStringLiteralNode;

/**
 * 读取一个全局变量的值。
 *
 * <p>Lua 5.3 中，读取全局 {@code x} 等价于读取 {@code _ENV.x}，因此本节点内部持有
 * 一个 {@link LuaReadTableNode}，其表子节点求值为当前环境（{@code _ENV}），键为变量名。
 * 这保证了 {@code __index} 元方法语义。保留独立的节点类型是为了让 {@code debug.getinfo}
 * 仍能把该访问识别为 {@code global}。
 */
@NodeInfo(shortName = "read_global")
public final class LuaReadGlobalVariableNode extends LuaExpressionNode {

    private final TruffleString name;

    @Child
    private LuaReadTableNode tableReadNode;

    public LuaReadGlobalVariableNode(LuaExpressionNode envNode, TruffleString name) {
        this.name = name;
        this.tableReadNode = LuaReadTableNodeGen.create(envNode, new LuaStringLiteralNode(name));
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return tableReadNode.executeGeneric(frame);
    }

    @Override
    public TruffleString getUnresolvedVariableName() {
        return this.name;
    }

    private String errorSuffix;

    @Override
    public String getErrorSuffix() {
        String suffix = errorSuffix;
        if (suffix == null) {
            suffix = computeErrorSuffix();
            errorSuffix = suffix;
        }
        return suffix;
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private String computeErrorSuffix() {
        return " (global '" + name.toJavaStringUncached() + "')";
    }
}
