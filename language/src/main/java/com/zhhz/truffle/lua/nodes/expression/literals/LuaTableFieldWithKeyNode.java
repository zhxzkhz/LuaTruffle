package com.zhhz.truffle.lua.nodes.expression.literals;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaTable;


/**
 * 处理 Lua 表构造器中显式指定 Key 的字段初始化。
 * <p>
 * 语法形式: {@code { key = value }} 或 {@code { ["key"] = value }}
 * <p>
 * 这个类是一个 <b>Truffle DSL Node</b>。
 * 它定义了抽象方法 {@code executeWithTable}，Truffle 注解处理器会自动生成该类的子类，
 * 并根据 {@code @Specialization} 提供的方法体填充具体的执行逻辑。
 */
public abstract class LuaTableFieldWithKeyNode extends LuaTableFieldInitializerNode {

    /**
     * 内联缓存 (Inline Cache) 的上限。
     * 这意味着对于同一个节点，Truffle 最多缓存 4 种不同的对象布局（Shapes）或属性访问策略。
     * 超过这个限制后，通常会回退到慢速路径（Generic / Megamorphic）。
     */
    static final int CACHE_LIMIT = 4;

    @Child private LuaExpressionNode keyNode;
    @Child private LuaExpressionNode valueNode;

    public LuaTableFieldWithKeyNode(LuaExpressionNode keyNode, LuaExpressionNode valueNode) {
        this.keyNode = keyNode;
        this.valueNode = valueNode;
    }

    /**
     * 节点的入口方法。
     * <p>
     * 负责先计算 Key 和 Value 的表达式结果，然后调用 DSL 生成的逻辑进行写入。
     */
    @Override
    public final void executeVoid(VirtualFrame frame, LuaTable table) {
        // 先对 key 和 value 求值
        Object keyResult = keyNode.executeGeneric(frame);
        Object valueResult = valueNode.executeGeneric(frame);

        // 调用由 Truffle DSL 生成的 executeWithTable 具体实现
        executeWithTable(frame, table, keyResult, valueResult);
    }

    /**
     * 抽象方法，作为 DSL 的“蓝图”。
     * <p>
     * Truffle DSL 会根据这个签名生成代码，并在其中调用下方的 @Specialization 方法。
     *
     * @param table 目标 Lua 表
     * @param key   已求值的 Key
     * @param value 已求值的 Value
     */
    protected abstract void executeWithTable(VirtualFrame frame, LuaTable table, Object key, Object value);

    // --- 特化 (Specializations) ---

    /**
     * 【核心写入逻辑】使用 DynamicObjectLibrary 进行属性写入。
     * <p>
     * <b>技术原理解析：</b>
     * 1. <b>DynamicObjectLibrary</b>: 这是 GraalVM 用于操作动态对象（DynamicObject）的标准库。
     *    它比普通的 HashMap 操作快得多，因为它利用了 <b>Shape Analysis (形状分析)</b>。
     * 2. <b>@CachedLibrary</b>: 这里的 library 实例会被缓存。
     *    它会记住对象的 "Shape"（字段布局）。如果下次进来的 table 具有相同的 Shape，
     *    Library 就可以直接通过内存偏移量（Offset）写入数据，通过汇编指令实现，而不需要查找哈希表。
     * <p>
     * limit = "CACHE_LIMIT": 限制缓存的多态深度。
     */
    @Specialization(limit = "CACHE_LIMIT")
    protected void writeObjectProperty(LuaTable table, Object key, Object value,
                                       @CachedLibrary(value = "table.getProperties()") DynamicObjectLibrary objLib) {
        // table.getProperties() 返回的是 DynamicObject 存储后端
        // objLib.put 会尝试根据缓存的 Shape 快速写入
        objLib.put(table.getProperties(), key, value);
    }


}