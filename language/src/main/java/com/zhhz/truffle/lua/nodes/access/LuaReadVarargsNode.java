package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

@NodeInfo(shortName = "...", description = "Reads variable arguments")
public final class LuaReadVarargsNode extends LuaExpressionNode {

    // 固定参数的个数 (例如 `function(a, b, ...)` 的固定参数个数是 2)
    private final int fixedParamCount;

    public LuaReadVarargsNode(int fixedParamCount) {
        this.fixedParamCount = fixedParamCount;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object[] args = frame.getArguments();

        // 计算可变参数在 args 数组中的起始位置
        // 索引 0 是 Parent Frame
        // 索引 1 到 fixedParamCount 是固定参数
        // 所以 Varargs 从 1 + fixedParamCount 开始
        int startIndex = 1 + fixedParamCount;

        // 如果没有可变参数传入
        if (startIndex >= args.length) {
            return LuaNil.SINGLETON;
        }

        // 截取剩余的参数
        int varargsLength = args.length - startIndex;
        Object[] varargs = new Object[varargsLength];
        System.arraycopy(args, startIndex, varargs, 0, varargsLength);

        // 返回数组，这符合我们“统一使用 Object[] 表示多值”的约定
        // 多值修改使用 LuaMultiValue包裹
        return new LuaMultiValue(varargs);
    }
}
