package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaRootNode;
import com.zhhz.truffle.lua.runtime.LuaCell;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 读取一个 upvalue（外层作用域被捕获的局部变量）。
 *
 * <p>调用约定：Lua 函数帧的 {@code arguments[0]} 存放被调用的 {@link LuaFunction}。
 * upvalue 值通过该闭包自己的 {@code upvalues} 数组（元素为 {@link LuaCell}）间接读取，
 * 从而保证每个闭包在创建时快照到的 cell 相互独立（循环体每次迭代捕获到不同的变量）。
 */
@NodeField(name = "index", type = int.class)
public abstract class LuaReadUpvalueNode extends LuaExpressionNode {

    /** 该 upvalue 在闭包 upvalue 数组中的下标。 */
    protected abstract int getIndex();

    @Specialization
    protected Object readUpvalue(VirtualFrame frame) {
        Object[] args = frame.getArguments();
        if (args.length == 0 || !(args[0] instanceof LuaFunction function)) {
            return LuaNil.SINGLETON;
        }
        Object[] upvalues = function.getUpvalues();
        int index = getIndex();
        if (index < 0 || index >= upvalues.length) {
            return LuaNil.SINGLETON;
        }
        Object entry = upvalues[index];
        Object value = entry instanceof LuaCell cell ? cell.value : entry;
        return value == null ? LuaNil.SINGLETON : value;
    }

    /**
     * 调试信息：从所在函数的 upvalue 描述符中解析名称。
     */
    @Override
    public TruffleString getUnresolvedVariableName() {
        if (getRootNode() instanceof LuaRootNode root) {
            LuaRootNode.UpvalueDescriptor[] descriptors = root.getUpvalueDescriptors();
            int index = getIndex();
            if (index >= 0 && index < descriptors.length) {
                return descriptors[index].name();
            }
        }
        return null;
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
        TruffleString name = getUnresolvedVariableName();
        return name == null ? "" : " (upvalue '" + name.toJavaStringUncached() + "')";
    }
}
