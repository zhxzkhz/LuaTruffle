package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.source.SourceSection;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.access.LuaWriteLocalVariableNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * Lua 泛型 For 循环的单次执行体。
 * <p>
 * 该节点实现了 {@link RepeatingNode} 接口，由 {@link LoopNode} 驱动。
 * 它的 executeRepeating 方法会被不断调用，直到返回 false。
 */
public class LuaGenericForRepeatingNode extends Node implements RepeatingNode {

    // =========================================================================
    // 迭代器状态 (Iterator State)
    // 警告：在生产级 Truffle 语言实现中，将运行时状态（如 controlVar）存储在 Node 字段中
    // 会导致 AST 无法在多线程间共享（非线程安全）。
    // 最佳实践是将这些状态存储在 VirtualFrame 的临时槽位（Frame Slots）或单独的 State 对象中。
    // =========================================================================

    private final int iteratorFuncIndex;
    private final int stateIndex;
    private final int controlVarIndex;

    /**
     * 负责将迭代产生的值（k, v）写入局部变量的节点数组。
     * 使用 @Children 标记以便框架进行优化。
     */
    @Children private final LuaWriteLocalVariableNode[] writeNodes;

    /** 循环体节点 */
    @Child
    private LuaStatementNode bodyNode;

    /**
     * 多语言互操作库 (Interop Library)。
     * <p>
     * 我们使用它来执行 `iteratorFunc`。这使得我们的 Lua 解释器非常强大，
     * 可以直接遍历其他语言（如 Java）提供的迭代器函数。
     * `createDispatched(3)` 表示为该调用点构建最多 3 层深度的多态内联缓存 (PIC)。
     */
    @Child
    private InteropLibrary funcLib;

    // debug 行钩子：for 头行（每次迭代触发，回边强制）与 end 行（迭代器耗尽退出触发；break 不触发）
    private final int headerLine;
    private final int endLine;
    /**
     * 迭代器调用（OP_TFORCALL）所在行：标准 Lua 中等于 {@code in} 之后表达式的首个记号所在行，
     * 用于“迭代器不可调用”错误的位置。
     */
    private final int callLine;

    public LuaGenericForRepeatingNode(int iteratorFuncIndex, int stateIndex, int controlVarIndex,
                                      LuaWriteLocalVariableNode[] writeNodes, LuaStatementNode bodyNode) {
        this(iteratorFuncIndex, stateIndex, controlVarIndex, writeNodes, bodyNode, 0, 0, 0);
    }

    public LuaGenericForRepeatingNode(int iteratorFuncIndex, int stateIndex, int controlVarIndex,
                                      LuaWriteLocalVariableNode[] writeNodes, LuaStatementNode bodyNode,
                                      int headerLine, int endLine, int callLine) {
        this.iteratorFuncIndex = iteratorFuncIndex;
        this.stateIndex = stateIndex;
        this.controlVarIndex = controlVarIndex;
        this.writeNodes = writeNodes;
        this.bodyNode = bodyNode;
        this.funcLib = InteropLibrary.getFactory().createDispatched(3);
        this.headerLine = headerLine;
        this.endLine = endLine;
        this.callLine = callLine;
    }

    /**
     * 执行单次循环迭代。
     *
     * @param frame 当前栈帧
     * @return boolean 如果循环应该继续，返回 true；如果循环结束（遇到 nil 或 break），返回 false。
     */
    @Override
    @ExplodeLoop
    public boolean executeRepeating(VirtualFrame frame) {
        TruffleSafepoint.poll(this);
        com.zhhz.truffle.lua.runtime.LuaInterrupt.check(this);
        if (com.zhhz.truffle.lua.runtime.LuaLineHook.isAnyActiveContext()) {
            com.zhhz.truffle.lua.runtime.LuaLineHook.fireLine(this, headerLine, true);
        }
        Object[] loopVarValues;
        Object iteratorFunc = frame.getObject(iteratorFuncIndex);
        Object state = frame.getObject(stateIndex);
        Object controlVar = frame.getObject(controlVarIndex);

        // 1. 【调用迭代器函数】
        // Lua 语义：func(state, control_var)
        try {
            // 使用 InteropLibrary 执行函数，传入状态和当前的控制变量
            Object result = funcLib.execute(iteratorFunc, state, controlVar);

            // 处理返回值：兼容返回单个值或多值数组的情况
            if (result instanceof LuaMultiValue luaMultiValue) {
                loopVarValues = luaMultiValue.values();
            } else {
                loopVarValues = new Object[]{result};
            }

        } catch (UnsupportedMessageException e) {
            // 迭代器不可调用：与标准 Lua 一致，报告 "attempt to call a <type> value"，
            // 位置取 for 头行（对应 OP_TFORCALL 指令所在行）。
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw callError(iteratorFunc);
        } catch (Exception e) {
            // 其它异常（含迭代器内部抛出的 Lua 错误）原样传播。
            CompilerDirectives.transferToInterpreterAndInvalidate();
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e);
        }

        // 2. 【更新控制变量】
        // Lua 核心语义：迭代器返回的 *第一个值* 更新为新的控制变量
        Object nextControlVar = loopVarValues.length > 0 ? loopVarValues[0] : LuaNil.SINGLETON;
        frame.getFrameDescriptor().setSlotKind(controlVarIndex, FrameSlotKind.Object);
        frame.setObject(controlVarIndex, nextControlVar);

        // 3. 【检查终止条件】
        // Lua 核心语义：如果第一个返回值为 nil，则循环终止
        if (nextControlVar == LuaNil.SINGLETON) {
            com.zhhz.truffle.lua.runtime.LuaLineHook.fireLine(this, endLine, false);
            return false; // 返回 false 通知 LoopNode 停止循环
        }


        // 4. 【赋值局部变量】
        // 将迭代器返回的所有值赋给循环变量（如 for k,v in ... 中的 k, v）
        for (int i = 0; i < writeNodes.length; i++) {
            // 处理多重赋值：如果返回值不够，用 nil 补齐
            Object valueToAssign = (i < loopVarValues.length) ? loopVarValues[i] : LuaNil.SINGLETON;
            writeNodes[i].execute(frame, valueToAssign);
        }

        // 5. 【执行循环体】
        try {
            bodyNode.executeVoid(frame);
        } catch (LuaContinueException e) {
            return true;
        } catch (LuaBreakException e) {
            // 6. 【处理 Break】
            // 捕获 break 异常，意味着用户想要跳出循环。
            // 返回 false 通知 LoopNode 停止。
            return false;
        }
        // 注意：如果有 continue 逻辑，应该在 bodyNode 内部处理，
        // 或者在这里捕获 LuaContinueException 并直接 swallow (什么都不做)，以便执行下一轮。

        // 7. 【继续下一轮】
        return true;
    }

    /**
     * 构造“迭代器不可调用”的运行时错误，位置取 for 头行。
     * 例如 {@code for k,v in 3 do end} → {@code <chunk>:<line>: attempt to call a number value}。
     */
    private RuntimeException callError(Object iteratorFunc) {
        String message = "attempt to call a " + LuaTypesUtil.getRuntimeTypeName(iteratorFunc) + " value";
        SourceSection root = getRootNode().getSourceSection();
        if (root != null && root.isAvailable() && root.getSource() != null && callLine > 0) {
            message = LuaException.chunkId(root.getSource().getName()) + ":" + callLine + ": " + message;
        }
        return LuaException.create(message, this);
    }
}
