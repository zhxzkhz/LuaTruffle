/*
 * Copyright (c) 2012, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags.ReadVariableTag;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.interop.NodeObjectDescriptor;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 读取局部变量的节点。
 * <p>
 * 这个节点使用 {@link NodeField} 来存储变量在 Frame 中的索引（slotIndex）。
 * 它展示了 Truffle 的类型特化（Type Specialization）机制，能够根据变量当前的实际类型
 * （long, double, boolean 或 Object）生成最高效的机器码。
 */
@NodeField(name = "slotIndex", type = int.class)
public abstract class LuaReadLocalVariableNode extends LuaExpressionNode {

    /**
     * 返回被访问局部变量的槽位索引。
     * <p>
     * 这个方法的具体实现由 Truffle DSL 根据类上的 {@link NodeField} 注解自动生成。
     * 我们不需要自己编写实现。
     */
    protected abstract int getSlotIndex();

    /**
     * 特化 1：读取 Long 类型。
     * <p>
     * 仅当守卫条件 {@link #isLongOrIllegal(VirtualFrame)} 返回 true 时，才会激活此路径。
     * 这意味着 Frame 中该槽位的类型目前被标记为 Long。
     * 这是最快的路径之一，直接读取原生 long，无装箱开销。
     */
    @Specialization(guards = "isLongOrIllegal(frame)")
    protected long readLong(VirtualFrame frame) {

        return frame.getLong(getSlotIndex());
    }

    /**
     * 特化 2：读取 Double 类型。
     * <p>
     * 类似于 readLong，仅当槽位类型为 Double 时激活。
     */
    @Specialization(guards = "isDoubleOrIllegal(frame)")
    protected double readDouble(VirtualFrame frame) {
        return frame.getDouble(getSlotIndex());
    }

    /**
     * 特化 3：读取 Boolean 类型。
     * <p>
     * 仅当槽位类型为 Boolean 时激活。
     */
    @Specialization(guards = "isBooleanOrIllegal(frame)")
    protected boolean readBoolean(VirtualFrame frame) {
        return frame.getBoolean(getSlotIndex());
    }

    /**
     * 通用特化：读取 Object 类型。
     * <p>
     * {@code replaces} 属性表示：如果此特化被激活，它将替换掉列表中指定的其他特化（readLong 等）。
     * 这通常发生在变量类型发生变化（例如从 long 变成了 String）时。
     * 一旦进入这个状态，该节点通常会保持在 Object 模式，处理所有类型的值。
     */
    @Specialization(replaces = {"readLong", "readDouble", "readBoolean"})
    protected Object readObject(VirtualFrame frame) {
        // 检查 FrameSlot 的实际类型标记是否已经是 Object
        if (!frame.isObject(getSlotIndex())) {
            /*
             * 这里的逻辑处理一种边缘情况：
             * FrameSlotKind 可能已经被更新为了 Object（可能由其他的 Write 节点触发），
             * 但是当前 Frame 实例中该位置存储的实际上还是旧的原生值（例如 long）。
             *
             * 这是一个“慢路径”操作（Slow-Path）。我们需要：
             * 1. 读取这个非 Object 的原始值 (frame.getValue)。
             * 2. 立即将其作为 Object 写回 Frame (frame.setObject)。
             *
             * 这样做是为了确保下次访问该变量时，Frame 的状态是一致的，避免重复进入这个慢路径。
             */

            // 告诉编译器这是不常发生的路径，不要进行内联优化，而是切回解释器执行
            CompilerDirectives.transferToInterpreter();

            Object result = frame.getValue(getSlotIndex());
            frame.setObject(getSlotIndex(), result);
            return result;
        }
        Object result = frame.getObject(getSlotIndex());
        if (result == null){
            return LuaNil.SINGLETON;
        }
        // 正常路径：直接读取 Object
        return result;
    }

    /**
     * 用于工具支持（如调试器）。
     * 标记此节点为“读取变量”的操作。
     */
    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        return tag == ReadVariableTag.class || super.hasTag(tag);
    }

    /**
     * 守卫方法：检查局部变量是否为 {@code long} 类型（或未定义类型）。
     *
     * @param frame 虽然在这个方法体里看起来可能没直接用（如果只看 kind），但它是必须的。
     *              如果没有这个参数，Truffle DSL 可能会假设这是一个纯函数（Pure Function），
     *              从而不会在每次执行特化时重新检查。但实际上 FrameSlotKind 是会动态变化的，
     *              所以我们需要依赖 frame 来获取最新的 Descriptor。
     */
    protected boolean isLongOrIllegal(VirtualFrame frame) {
        final FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(getSlotIndex());
        // Illegal 通常表示变量刚声明但尚未赋值，或者是死代码消除后的状态，通常兼容所有基本类型
        return kind == FrameSlotKind.Long || kind == FrameSlotKind.Illegal;
    }

    protected boolean isDoubleOrIllegal(VirtualFrame frame) {
        final FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(getSlotIndex());
        return kind == FrameSlotKind.Double || kind == FrameSlotKind.Illegal;
    }

    protected boolean isBooleanOrIllegal(VirtualFrame frame) {
        final FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(getSlotIndex());
        return kind == FrameSlotKind.Boolean || kind == FrameSlotKind.Illegal;
    }

    /**
     * 提供给调试器的元数据对象。
     * 这里返回的是变量的名称。
     */
    @Override
    public Object getNodeObject() {
        // 获取变量名并封装为 NodeObjectDescriptor
        return NodeObjectDescriptor.readVariable((TruffleString) getRootNode().getFrameDescriptor().getSlotName(getSlotIndex()));
    }
}