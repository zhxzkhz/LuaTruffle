/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
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
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * Lua "return" 语句的 AST 节点实现。
 * <p>
 * 该节点负责计算所有返回表达式的值，并将它们封装在 {@link LuaReturnException} 中抛出。
 * 这个异常随后会被 {@link LuaFunctionBodyNode} 捕获，从而实现从函数中退出的控制流跳转。
 */
@NodeInfo(shortName = "return", description = "实现return语句的节点")
public final class LuaReturnNode extends LuaStatementNode {

    /**
     * 返回值表达式数组。
     * <p>
     * 使用 @Children 而不是 @Child，因为 Lua 支持多重返回值 (e.g., {@code return a, b, c})。
     * Truffle 框架会对数组中的每个子节点分别进行管理和优化。
     */
    @Children private LuaExpressionNode[] returnExpressions;

    public LuaReturnNode(LuaExpressionNode[] returnExpressions) {
        this.returnExpressions = returnExpressions;
    }

    /**
     * 执行 return 语句。
     * <p>
     * <b>Truffle 关键优化：@ExplodeLoop</b>
     * <p>
     * 这个注解告诉 Graal 编译器：请把下面的 for 循环“展开”（Unroll）。
     * <p>
     * <b>原理：</b> 在 AST 构建完成后，this.returnExpressions.length 是一个固定常数（Compilation Constant）。
     * 编译器在部分求值（Partial Evaluation）阶段，会将这个循环展开成一系列线性的指令。
     * <p>
     * 例如，如果 return a, b; (length=2)，编译后的代码相当于：
     * <pre>
     *    results[0] = expr[0].executeGeneric(frame);
     *    results[1] = expr[1].executeGeneric(frame);
     * </pre>
     * 这样做消除了循环计数器检查和跳转的开销，极大提升了性能。
     */
    @Override
    @ExplodeLoop
    public void executeVoid(VirtualFrame frame) {


        // 如果表达式列表为空，返回一个空数组。
        int argCount = this.returnExpressions.length;

        // 优化：无参调用直接处理
        if (argCount == 0) {
            throw LuaReturnException.withResult(LuaNil.SINGLETON);
        }

        final Object[] prefixArgs = new Object[argCount - 1];

        for (int i = 0; i < argCount - 1; i++) {
            // 在这里，我们仍然需要处理参数本身是多返回值的情况
            Object rawArg = this.returnExpressions[i].executeGeneric(frame);
            // 返回值可能是luaMultiValue，也可能是一个基础对象
            if (rawArg instanceof LuaMultiValue luaMultiValue) {
                //如果是多值则获取第一个
                if (luaMultiValue.isMultiValue()){
                    prefixArgs[i] = ((Object[]) luaMultiValue.values())[0];
                } else {
                    prefixArgs[i] = luaMultiValue.values();
                }
            } else  if (rawArg instanceof Object[] || rawArg == null) {
                //如果返回数值则报错,然后修改返回数组的方法
                throw new RuntimeException("不能返回数组祸空");
            } else {
                prefixArgs[i] = rawArg;
            }
        }


        // 步骤 B: 计算最后一个参数 (可能返回多值)
        Object lastResult = this.returnExpressions[argCount - 1].executeGeneric(frame);

        // 步骤 C: 根据最后一个参数的结果，创建最终数组
        Object[] finalArguments;

        if (lastResult instanceof LuaMultiValue luaMultiValue) {
            if (luaMultiValue.isMultiValue()){
                Object[] values = (Object[]) luaMultiValue.values();
                // --- 情况 1: 最后一个参数返回了多值 ---
                // 计算总长度
                int totalLength = (argCount - 1) + values.length;
                finalArguments = new Object[totalLength];

                // 只有这里发生了真正的数组复制
                System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
                System.arraycopy(values, 0, finalArguments, prefixArgs.length, values.length);

            } else {
                // --- 情况 2: 最后一个参数返回单值 (最常见的情况) ---
                finalArguments = new Object[argCount];
                System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
                finalArguments[argCount - 1] = luaMultiValue.values();
            }
        }  else  if (lastResult instanceof Object[] || lastResult == null) {
            //如果返回数值则报错,然后修改返回数组的方法
            throw new RuntimeException("不能返回数组祸空");
        } else {
            finalArguments = new Object[argCount];
            System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
            finalArguments[argCount - 1] = lastResult;
        }

        if (finalArguments.length == 1) {
            throw LuaReturnException.withResult(finalArguments[0]);
        }

        // 4. 【多返回值情况】
        // 将结果数组封装在异常中抛出。
        throw LuaReturnException.withResult(new LuaMultiValue(finalArguments));

    }

    /**
     * 辅助方法：判断是否为空返回。
     * 主要用于解析器或工具查询。
     */
    public boolean isEmpty() {
        return returnExpressions.length == 0;
    }
}