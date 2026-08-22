/*
 * Copyright (c) 2012, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 一个语句块的节点。
 * 它的唯一职责就是按顺序执行其所有的子语句节点。
 * 这个版本不使用高级的 com.oracle.truffle.api.nodes.BlockNode API，
 * 而是使用标准的 @Children + @ExplodeLoop 模式，这对于大多数语言来说
 * 已经足够高效且更容易理解。
 */
@NodeInfo(shortName = "block", description = "A block of statements")
public final class LuaBlockNode extends LuaStatementNode {

    @Children private final LuaStatementNode[] statements;
    @Children private final LuaCloseSlotNode[] closeNodes;
    private final Map<TruffleString, Integer> labelTargets;
    private final int[] blockPath;
    private final boolean hasCloseOrLabels;

    public LuaBlockNode(LuaStatementNode[] bodyNodes) {
        this(bodyNodes, new LuaCloseSlotNode[0], new int[0]);
    }

    public LuaBlockNode(LuaStatementNode[] bodyNodes, LuaCloseSlotNode[] closeNodes, int[] blockPath) {
        this.statements = bodyNodes;
        this.closeNodes = closeNodes;
        this.blockPath = blockPath;
        this.labelTargets = buildLabelTargets(bodyNodes);
        this.hasCloseOrLabels = closeNodes.length > 0 || !labelTargets.isEmpty();
    }


    @Override
    @ExplodeLoop
    public void executeVoid(VirtualFrame frame) {
        if (!hasCloseOrLabels) {
            for (int i = 0; i < statements.length; i++) {
                statements[i].executeVoid(frame);
            }
            return;
        }
        executeVoidSlowPath(frame);
    }

    private void executeVoidSlowPath(VirtualFrame frame) {
        try {
            int index = 0;
            while (index < statements.length) {
                statements[index].executeVoid(frame);
                index++;
            }
        } catch (LuaGotoException e) {
            Integer target = e.targetsBlock(blockPath) ? labelTargets.get(e.getLabel()) : null;
            if (target == null) {
                close(frame, LuaNil.SINGLETON);
                throw e;
            }

            int index = target;
            while (index < statements.length) {
                try {
                    statements[index].executeVoid(frame);
                    index++;
                } catch (LuaGotoException nested) {
                    Integer nestedTarget = nested.targetsBlock(blockPath) ? labelTargets.get(nested.getLabel()) : null;
                    if (nestedTarget == null) {
                        close(frame, LuaNil.SINGLETON);
                        throw nested;
                    }
                    index = nestedTarget;
                }
            }
        } catch (Throwable t) {
            close(frame, closeErrorObject(t));
            throw t;
        }

        close(frame, LuaNil.SINGLETON);
    }

    private void close(VirtualFrame frame, Object errorObject) {
        for (int i = closeNodes.length - 1; i >= 0; i--) {
            closeNodes[i].executeClose(frame, errorObject);
        }
    }

    private static Object closeErrorObject(Throwable throwable) {
        if (throwable instanceof ControlFlowException) {
            return LuaNil.SINGLETON;
        }
        String message = throwable.getMessage();
        return message == null ? LuaNil.SINGLETON : message;
    }

    /**
     * 提供一个方法来访问（例如用于调试或工具）块内的语句。
     * @return An unmodifiable list of the statements in this block.
     */
    public List<LuaStatementNode> getBlock() {
        if (statements.length == 0) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(statements));
    }

    public List<TruffleString> getLabels() {
        if (labelTargets.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(labelTargets.keySet()));
    }

    private static Map<TruffleString, Integer> buildLabelTargets(LuaStatementNode[] statements) {
        Map<TruffleString, Integer> result = new HashMap<>();
        for (int i = 0; i < statements.length; i++) {
            if (statements[i] instanceof LuaLabelNode labelNode) {
                result.put(labelNode.getLabel(), i + 1);
            }
        }
        return result;
    }
}
