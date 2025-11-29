/*
 * Copyright (c) 2012, 2024, Oracle and/or its affiliates. All rights reserved.
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
package com.zhhz.truffle.lua.launcher;

import org.graalvm.polyglot.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Objects;

public final class LuaMain {

    private static final String Lua = "lua";

    /**
     * The main entry point.
     */
    public static void main(String[] args) throws IOException {

        String fileName = "fibonacci.lua";

        ClassLoader classLoader = LuaMain.class.getClassLoader();
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(fileName)));

        Source source = Source.newBuilder(Lua, reader ,fileName).build();

        System.exit(executeSource(source));


    }

    private static int executeSource(Source source) {
        Context context;
        PrintStream err = System.err;
        try {
            context = Context.newBuilder(Lua).in(System.in).out(System.out)
                    //.option("engine.CompileImmediately","true")
                    //.option("engine.CompilationFailureAction","Print")
                    //.option("compiler.TracePerformanceWarnings","all")
                    //.option("compiler.DiagnoseFailure","true")
                    .allowAllAccess(true).build();
        } catch (IllegalArgumentException e) {
            err.println(e.getMessage());
            return 1;
        }

        try {
            Value result = context.eval(source);
            if (!result.isNull()) {
                System.out.println("返回值：" + Value.asValue(result));
            }
            return 0;
        } catch (PolyglotException ex) {
            if (ex.isInternalError()) {
                // for internal errors we print the full stack trace
                ex.printStackTrace();
            } else {
                printLuaError(ex);
            }
            return 1;
        } finally {
            context.close();
        }
    }

    private static void printLuaError(PolyglotException e) {
        StringBuilder sb = new StringBuilder();

        // 1. 尝试直接获取位置（适用于非内置函数的错误）
        SourceSection location = e.getSourceLocation();

        // 2. 如果直接位置为空（例如内置函数报错），则遍历堆栈寻找第一个有源码的调用者
        if (location == null) {
            for (PolyglotException.StackFrame frame : e.getPolyglotStackTrace()) {
                // 检查这一帧是否有源码位置

                if (frame.isGuestFrame() && frame.getSourceLocation() != null) {
                    location = frame.getSourceLocation();
                    break; // 找到了最近的调用者，停止
                }
            }

        }

        // 3. 格式化输出
        if (location != null) {
            sb.append(location.getSource().getName())
                    .append(":")
                    .append(location.getStartLine())
                    .append(": ");
        } else {
            sb.append("unknown location: ");
        }

        sb.append(e.getMessage());
        System.err.println(sb);

        // (可选) 打印完整的 Lua 堆栈信息
        // printStackTrace(e);
    }


}
