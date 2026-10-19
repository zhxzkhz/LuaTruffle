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

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags.WriteVariableTag;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.interop.NodeObjectDescriptor;
import com.zhhz.truffle.lua.parser.LuaBaseParser.LocalSlot;
import com.zhhz.truffle.lua.runtime.LuaCell;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 将值写入一个局部变量 (通过其索引) 的语句节点。
 * <p>
 * 该节点用于【声明】语义（{@code local x = v}、循环变量、参数、{@code _ENV} 序言）：
 * 若该变量被内层闭包捕获，则每次执行都会创建一个新的 {@link LuaCell} 存入槽位，
 * 从而保证每次进入声明（例如循环体每次迭代）都是一个独立变量；否则按类型化槽位直接写入。
 * 普通赋值 {@code x = v} 请使用 {@link LuaWriteAssignedLocalVariableNode}。
 */
@NodeField(name = "slotIndex", type = int.class)
@NodeField(name = "slot", type = LocalSlot.class)
@NodeField(name = "declare", type = boolean.class)
public abstract class LuaWriteLocalVariableNode extends LuaWriteVariableNode {

    /**
     * 由 DSL 自动生成，用于获取存储的 slotIndex。
     */
    protected abstract int getSlotIndex();

    /** 该局部变量声明的可变句柄（用于判断是否被内层闭包捕获为 cell）。 */
    protected abstract LocalSlot getSlot();

    /**
     * 是否为【声明】写入（{@code true}：每次执行创建新 cell；{@code false}：普通赋值，
     * 写入已存在的 cell 或类型化槽位）。
     */
    protected abstract boolean isDeclare();

    public final TruffleString getSlotName() {
        return (TruffleString) getRootNode().getFrameDescriptor().getSlotName(getSlotIndex());
    }

    protected boolean isCaptured() {
        LocalSlot slot = getSlot();
        return slot != null && slot.captured;
    }

    @Specialization(guards = {"isCaptured()", "isDeclare()"})
    protected void writeCellDeclare(VirtualFrame frame, Object value) {
        frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Object);
        frame.setObject(getSlotIndex(), new LuaCell(value == null ? LuaNil.SINGLETON : value));
    }

    @Specialization(guards = {"isCaptured()", "!isDeclare()"})
    protected void writeCellAssign(VirtualFrame frame, Object value) {
        Object normalized = value == null ? LuaNil.SINGLETON : value;
        if (frame.isObject(getSlotIndex())) {
            Object current = frame.getObject(getSlotIndex());
            if (current instanceof LuaCell cell) {
                cell.value = normalized;
                return;
            }
        }
        frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Object);
        frame.setObject(getSlotIndex(), new LuaCell(normalized));
    }

    @Specialization(guards = {"!isCaptured()", "isLongOrIllegal(frame)"})
    protected void writeLong(VirtualFrame frame, long value) {
        frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Long);
        frame.setLong(getSlotIndex(), value);
    }

    @Specialization(guards = {"!isCaptured()", "isDoubleOrIllegal(frame)"})
    protected void writeDouble(VirtualFrame frame, double value) {
        frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Double);
        frame.setDouble(getSlotIndex(), value);
    }


    /**
     * 特化：写入 boolean 类型的值。
     */
    @Specialization(guards = {"!isCaptured()", "isBooleanOrIllegal(frame)"})
    protected void writeBoolean(VirtualFrame frame, boolean value) {
        frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Boolean);
        frame.setBoolean(getSlotIndex(), value);
    }

    /**
     * 通用特化：写入任何 Object 类型的值。
     */
    @Specialization(guards = "!isCaptured()")
    protected void writeObject(VirtualFrame frame, Object value) {
        frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Object);
        frame.setObject(getSlotIndex(), value);
    }

    public abstract void executeWrite(VirtualFrame frame, Object value);

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

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        return tag == WriteVariableTag.class || super.hasTag(tag);
    }

    /**
     * 【重构后的 getNodeObject】
     * 现在直接使用存储在节点字段中的信息。
     */
    @Override
    public Object getNodeObject() {
        // 3. 【修改】直接从 FrameDescriptor 获取变量名
        Object slotName = getRootNode().getFrameDescriptor().getSlotName(getSlotIndex());

        // 4. 【修改】直接使用存储的 SourceSection
        SourceSection sourceSection = getSourceSection();

        // 使用 Truffle 提供的 NodeObjectDescriptor 来为调试器创建描述对象

        return NodeObjectDescriptor.writeVariable(
                (TruffleString) slotName,
                sourceSection
        );
    }

}
