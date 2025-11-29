/*
 * Copyright (c) 2012, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.zhhz.truffle.lua.nodes;

import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.strings.TruffleString;

/**
 * Base class for all SL nodes that produce a value and therefore benefit from type specialization.
 * The annotation {@link TypeSystemReference} specifies the SL types. Specifying it here defines the
 * type system for all subclasses.
 */
@TypeSystemReference(LuaTypes.class)
@NodeInfo(description = "The abstract base node for all expressions")
@GenerateWrapper
public abstract class LuaExpressionNode extends LuaStatementNode {

    private boolean hasExpressionTag;

    /**
     * The execute method when no specialization is possible. This is the most general case,
     * therefore it must be provided by all subclasses.
     */
    public abstract Object executeGeneric(VirtualFrame frame);

    /**
     * 当我们在一个只需要 {@link LuaStatementNode 语句} 就足够的位置上使用表达式时，
     * 其返回值会被直接丢弃。
     */
    @Override
    public void executeVoid(VirtualFrame frame) {
        executeGeneric(frame);
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probe) {
        return new LuaExpressionNodeWrapper(this, probe);
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.ExpressionTag.class) {
            return hasExpressionTag;
        }
        return super.hasTag(tag);
    }

    /**
     * Marks this node as being a {@link StandardTags.ExpressionTag} for instrumentation purposes.
     */
    public final void addExpressionTag() {
        hasExpressionTag = true;
    }

    /*
     * 针对特化类型的执行方法。它们都遵循相同的模式：
     * 调用通用的执行方法（generic execution method），然后期望得到一个与其返回类型相匹配的结果。
     * 类型特化的子类会覆写（overwrite）相应的方法。
     */

    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return LuaTypesGen.expectLong(executeGeneric(frame));
    }

    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        return LuaTypesGen.expectBoolean(executeGeneric(frame));
    }

    public Object executeObject(VirtualFrame frame) throws UnexpectedResultException {
        return LuaTypesGen.expectBoolean(executeGeneric(frame));
    }


    /**
     * 【新】一个用于错误报告的描述性方法。
     *
     * <p>如果这个表达式节点是一个简单的变量读取，它应该重写此方法，
     * 并返回它试图读取的变量名。
     *
     * <p>对于所有其他类型的表达式 (例如 a+b)，它应该返回 null。
     *
     * @return 变量名，如果适用；否则返回 null。
     */
    public TruffleString getUnresolvedVariableName() {
        return null;
    }

}
