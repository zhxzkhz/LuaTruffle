package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.runtime.LuaCell;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 写入一个 upvalue（外层作用域被捕获的局部变量）。
 *
 * <p>通过当前闭包的 {@code upvalues} 数组（元素为 {@link LuaCell}）间接写入，
 * 使共享同一 cell 的所有闭包都能观察到更新。
 */
@NodeInfo(description = "用于从外部作用域（一个上值）写入变量的节点")
@NodeField(name = "index", type = int.class)
public abstract class LuaWriteUpvalueNode extends LuaWriteVariableNode {

    /** 该 upvalue 在闭包 upvalue 数组中的下标。 */
    protected abstract int getIndex();

    @Specialization
    public void writeUpvalue(VirtualFrame frame, Object value) {
        Object normalized = value == null ? LuaNil.SINGLETON : value;
        Object[] args = frame.getArguments();
        if (args.length == 0 || !(args[0] instanceof LuaFunction function)) {
            return;
        }
        Object[] upvalues = function.getUpvalues();
        int index = getIndex();
        if (index < 0 || index >= upvalues.length) {
            return;
        }
        if (upvalues[index] instanceof LuaCell cell) {
            cell.value = normalized;
        } else {
            upvalues[index] = new LuaCell(normalized);
        }
    }
}
