package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.access.LuaWriteLocalVariableNode;
import com.zhhz.truffle.lua.nodes.expression.LuaMultiValueNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * Lua 语言中 "泛型 for 循环" (Generic For Loop) 的 AST 节点实现。
 * <p>
 * 语法结构: {@code for var_1, ..., var_n in explist do block end}
 * <p>
 * 该节点负责：
 * 1. 执行 {@code in} 后面的表达式列表 (explist)。
 * 2. 获取迭代器三元组 (Iterator Triplet)：函数、状态、初始控制变量。
 * 3. 初始化循环执行体 (RepeatingNode)。
 * 4. 启动 Truffle 框架管理的循环 (LoopNode)。
 */
@NodeInfo(shortName = "for .. in", description = "The node implementing the generic for loop")
public class LuaGenericForNode extends LuaStatementNode {

    /**
     * 负责计算 {@code in} 后面表达式的节点。
     * 例如在 {@code pairs(t)} 中，它负责执行函数调用并返回多值结果。
     */
    @Child
    private LuaMultiValueNode expListNode;

    /**
     * Truffle 框架提供的循环包装节点。
     * <p>
     * 使用 LoopNode 而不是简单的 Java while 循环非常关键，
     * 因为它支持 <b>OSR (On-Stack Replacement)</b> 编译优化，
     * 能让长时间运行的循环在解释执行期间就被动态编译为机器码。
     */
    @Child
    private LoopNode loopNode;

    public LuaGenericForNode(LuaMultiValueNode expListNode, LuaWriteLocalVariableNode[] writeNodes, LuaStatementNode bodyNode) {
        this.expListNode = expListNode;
        // 创建重复执行节点 (RepeatingNode)，并将其包装在 LoopNode 中
        this.loopNode = Truffle.getRuntime().createLoopNode(new LuaGenericForRepeatingNode(writeNodes, bodyNode));
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        // 1. 【初始化阶段】执行 in 后面的表达式列表
        // Lua 语义规定：先对 explist 求值，得到迭代器函数、不变量状态 (State) 和控制变量初始值
        Object[] iterators = (Object[]) expListNode.executeMultiValue(frame);
        // 2. 【提取三元组】
        // 根据 Lua 规范，explist 返回的前三个值分别作为：
        // [0]: 迭代器函数 (Iterator Function) - 每次循环都会调用它
        // [1]: 不变状态 (State) - 传给迭代器函数的第一个参数 (通常是 Table)
        // [2]: 控制变量 (Control Variable) - 传给迭代器函数的第二个参数 (通常是索引或 Key)
        Object iteratorFunc = iterators.length > 0 ? iterators[0] : LuaNil.SINGLETON;
        Object state = iterators.length > 1 ? iterators[1] : LuaNil.SINGLETON;
        Object controlVar = iterators.length > 2 ? iterators[2] : LuaNil.SINGLETON;

        // 3. 【注入状态】将运行时获取的迭代器三元组注入到 RepeatingNode 中
        // ⚠️警告：这种直接修改 Node 字段的做法在多线程共享 AST 场景下是不安全的（Not Thread-Safe）。(待优化)
        // 注意：RepeatingNode 负责具体的单次迭代逻辑（调用函数 -> 赋值 -> 执行 Body -> 判断结束）
        if (loopNode.getRepeatingNode() instanceof LuaGenericForRepeatingNode luaGenericForRepeatingNode){
            // 设置初始控制变量 (第一次迭代前的 seed)
            luaGenericForRepeatingNode.setControlVar(controlVar);
            // 设置状态常量
            luaGenericForRepeatingNode.setState(state);
            // 设置迭代器函数
            luaGenericForRepeatingNode.setIteratorFunc(iteratorFunc);
        }

        // 4. 【启动循环】执行 LoopNode
        // 这会不断调用 RepeatingNode.executeRepeating(frame) 直到其返回 false
        loopNode.execute(frame);
    }
}
