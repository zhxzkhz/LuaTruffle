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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LuaMain {

    private static final String Lua = "lua";

    /**
     * The main entry point.
     */
    public static void main(String[] args) throws IOException {
        boolean portableTests = args.length > 0 && args[0].equals("--portable-tests");

        int scriptIndex = portableTests ? 1 : 0;
        
        // Parse interpreter options: -E, -l, -e, -i, -v
        boolean ignoreEnv = false;
        boolean interactive = false;
        List<String> evalStatements = new ArrayList<>();
        List<String> loadModules = new ArrayList<>();
        // Preserve raw option tokens for the arg table (everything before script path)
        List<String> rawOptions = new ArrayList<>();
        
        while (args.length > scriptIndex && args[scriptIndex].startsWith("-") && !args[scriptIndex].equals("-")) {
            String opt = args[scriptIndex];
            if (opt.equals("--")) {
                rawOptions.add(opt);
                scriptIndex++; // end of options marker
                break;
            } else if (opt.equals("-E")) {
                ignoreEnv = true;
                rawOptions.add(opt);
                scriptIndex++;
            } else if (opt.equals("-i")) {
                interactive = true;
                rawOptions.add(opt);
                scriptIndex++;
            } else if (opt.equals("-v") || opt.equals("--version")) {
                System.out.println("Lua 5.3 (LuaTruffle)");
                return;
            } else if (opt.equals("-l")) {
                rawOptions.add(opt);
                scriptIndex++;
                if (args.length > scriptIndex) {
                    loadModules.add(args[scriptIndex]);
                    rawOptions.add(args[scriptIndex]);
                    scriptIndex++;
                }
            } else if (opt.equals("-e")) {
                rawOptions.add(opt);
                scriptIndex++;
                if (args.length > scriptIndex) {
                    evalStatements.add(args[scriptIndex]);
                    rawOptions.add(args[scriptIndex]);
                    scriptIndex++;
                }
            } else if (opt.startsWith("-e")) {
                evalStatements.add(opt.substring(2));
                rawOptions.add(opt);
                scriptIndex++;
            } else if (opt.startsWith("-l")) {
                loadModules.add(opt.substring(2));
                rawOptions.add(opt);
                scriptIndex++;
            } else {
                break; // unknown option, treat as script path
            }
        }

        String resourcesPath;
        String[] scriptArguments;
        boolean noScript = args.length <= scriptIndex;
        if (!noScript) {
            resourcesPath = args[scriptIndex];
            scriptArguments = Arrays.copyOfRange(args, scriptIndex + 1, args.length);
        } else {
            resourcesPath = "-";
            scriptArguments = new String[0];
        }

        // Source 创建规则（与标准 lua 的 main 流程一致）：
        // - 脚本文件：直接从文件创建；
        // - stdin（显式 "-" 或无脚本且非终端）：整个 stdin 作为一个脚本，
        //   但构建延迟到 -e/-l 执行之后（否则会提前吞掉 stdin，
        //   导致 -e 中的 debug.debug()/io.read 读到 EOF）；
        // - 无脚本且 stdin 为终端、或带 -i：进入交互 REPL。
        //   是否 REPL 最终在 executeSource 中决定。
        Source source;
        if (resourcesPath.equals("-") || noScript) {
            source = null;
        } else {
            source = Source.newBuilder(Lua, new File(resourcesPath)).build();
        }
        // Build applicationArguments for the arg table
        // Standard Lua arg layout: arg[-n..-2]=options, arg[-1]=launcher, arg[0]=script, arg[1..n]=scriptArgs
        List<String> argList = new ArrayList<>();
        argList.add(createLauncherCommand());
        argList.addAll(rawOptions);
        argList.add(resourcesPath);
        argList.addAll(Arrays.asList(scriptArguments));
        String[] applicationArguments = argList.toArray(new String[0]);

        System.exit(executeSource(source, applicationArguments, portableTests, evalStatements, loadModules,
                interactive, resourcesPath, noScript));
    }

    private static String createLauncherCommand() throws IOException {
        String modulePath = System.getProperty("jdk.module.path");
        if (modulePath == null || modulePath.isBlank()) {
            return "lua";
        }
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        // Use a fixed script name based on the module path hash so that parent and child
        // processes share the same launcher script (and thus the same progname).
        String scriptName = "lua-truffle.sh";
        Path script = Path.of(System.getProperty("java.io.tmpdir"), scriptName);
        String java = Path.of(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java").toString();
        if (windows) {
            java = java.replace('\\', '/');
            modulePath = modulePath.replace('\\', '/');
        }
        String command = "#!/bin/sh\nexec \"" + java + "\" -Xms16m -Xmx256m --enable-native-access=org.graalvm.truffle "
                + "--sun-misc-unsafe-memory-access=allow --module-path \"" + modulePath + "\" -m "
                + "org.graalvm.lua.launcher/com.zhhz.truffle.lua.launcher.LuaMain \"$@\"\n";
        Files.writeString(script, command);
        script.toFile().setExecutable(true);
        return windows ? script.toString().replace('\\', '/') : script.toString();
    }

    private static int executeSource(Source source, String[] arguments, boolean portableTests,
                                     List<String> evalStatements, List<String> loadModules,
                                     boolean interactive, String resourcesPath, boolean noScript) {
        // stdin 直接共享给 Context：io.read、debug.debug 等通过 env.in() 读取。
        // 注意不能给空流——否则 -e 中的 debug.debug() 会立即读到 EOF。
        // REPL 的 BufferedReader 在 -e/-l 语句之后才创建，顺序读取不会互相抢占。
        java.io.InputStream sharedIn = System.in;
        Context context;
        PrintStream err = System.err;
        try {
            context = Context.newBuilder(Lua).in(sharedIn).out(System.out).arguments(Lua, arguments)
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
            if (portableTests) {
                context.eval(Lua, "_port = true");
            }
            // Execute -l module loads (before -e statements, matching standard Lua behavior)
            // Standard Lua behavior: require(mod), and if result is non-nil, set _G[mod] = result
            for (String mod : loadModules) {
                // Escape the module name for use in a Lua string literal
                String escapedMod = mod.replace("\\", "\\\\").replace("'", "\\'");
                String code = "local r = require('" + escapedMod + "'); if r ~= nil then local m = '" + escapedMod + "'; _G[m] = r end";
                context.eval(Lua, code);
            }
            // Execute -e statements
            for (String stmt : evalStatements) {
                context.eval(Lua, stmt);
            }
            // Validate that 'arg' is still a table (standard Lua expects this
            // when passing script arguments as varargs to the main chunk)
            context.eval(Lua, "if arg ~= nil and type(arg) ~= 'table' then error(\"'arg' is not a table\") end");
            // Run the main script (if not stdin, or if there's a script file)
            if (source != null) {
                Value result = context.eval(source);
                if (!result.isNull()) {
                    System.out.println("返回值：" + Value.asValue(result));
                }
            } else if (resourcesPath.equals("-")) {
                // stdin 作为脚本或 REPL：
                // - 显式 "-" 或 无脚本且 stdin 非终端 → 整个 stdin 作为一个脚本执行；
                // - 无脚本且 stdin 为终端 → 交互 REPL
                if (!interactive && (!noScript || System.console() == null)) {
                    // 延迟到此处构建，避免在 -e/-l 之前吞掉 stdin
                    try {
                        Source stdinScript = Source.newBuilder(Lua, new InputStreamReader(System.in), "=stdin").build();
                        Value result = context.eval(stdinScript);
                        if (!result.isNull()) {
                            System.out.println("返回值：" + Value.asValue(result));
                        }
                    } catch (java.io.IOException ioException) {
                        err.println(ioException.getMessage());
                        return 1;
                    }
                } else if (noScript) {
                    interactive = true;
                }
            }
            // 无脚本且无 -e/-l 时也要触发一次解析，
            // 保证 LUA_INIT 与 package.path 初始化在进入 REPL 之前完成
            if (source == null && evalStatements.isEmpty() && loadModules.isEmpty() && !portableTests) {
                context.eval(Lua, "");
            }
            // Enter interactive mode if -i was specified
            if (interactive) {
                runInteractive(context, new java.io.BufferedReader(new InputStreamReader(System.in)));
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
        SourceSection location = e.getSourceLocation();
        if (location == null) {
            for (PolyglotException.StackFrame frame : e.getPolyglotStackTrace()) {
                if (frame.isGuestFrame() && frame.getSourceLocation() != null) {
                    location = frame.getSourceLocation();
                    break;
                }
            }
        }

        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = e.isSyntaxError() ? "syntax error" : "unknown Lua error";
        }
        StringBuilder sb = new StringBuilder();
        if (location != null) {
            appendLocation(sb, location).append(": ");
        }
        sb.append(message);
        System.err.println(sb);

        boolean printedHeader = false;
        for (PolyglotException.StackFrame frame : e.getPolyglotStackTrace()) {
            if (!frame.isGuestFrame() || frame.getSourceLocation() == null) {
                continue;
            }
            if (!printedHeader) {
                System.err.println("stack traceback:");
                printedHeader = true;
            }
            SourceSection frameLocation = frame.getSourceLocation();
            StringBuilder line = new StringBuilder("\t");
            appendLocation(line, frameLocation);
            String rootName = frame.getRootName();
            if (rootName != null && !rootName.isBlank()) {
                line.append(": in ").append(formatFunction(rootName));
            }
            System.err.println(line);
        }
    }

    private static StringBuilder appendLocation(StringBuilder target, SourceSection location) {
        return target.append(location.getSource().getName())
                .append(":")
                .append(location.getStartLine());
    }

    private static String formatFunction(String rootName) {
        return rootName.startsWith("<") ? rootName : "function '" + rootName + "'";
    }

    /**
     * Simple interactive REPL loop, similar to standard Lua's -i mode.
     * Reads lines from stdin, accumulates them until a complete chunk is formed,
     * then evaluates and prints results using Lua's print function.
     */
    private static void runInteractive(Context context, java.io.BufferedReader reader) {
        StringBuilder buffer = new StringBuilder();
        String prompt = getPrompt(context, "_PROMPT", "> ");
        String prompt2 = getPrompt(context, "_PROMPT2", ">> ");
        // 标准 lua 的 REPL 总是打印提示符（可通过 _PROMPT 自定义），
        // 测试脚本通常设置 _PROMPT='' 来保持输出干净
        System.out.print(prompt);
        System.out.flush();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                boolean firstLine = buffer.length() == 0;
                if (!firstLine) {
                    buffer.append("\n");
                }
                // 标准 lua REPL 的 "=expr" 语法糖：作为 "return expr" 求值并打印
                if (firstLine && line.startsWith("=")) {
                    buffer.append("return ").append(line, 1, line.length());
                } else {
                    buffer.append(line);
                }
                String chunk = buffer.toString();
                // 与标准 lua REPL 的 addreturn 语义一致：先按 "return <chunk>" 试一次，
                // 语法错误再按普通语句处理（可能续行）
                boolean evaluated = false;
                try {
                    Value result = context.eval(Source.newBuilder(Lua, "return " + chunk, "stdin").build());
                    echoResult(context, result);
                    evaluated = true;
                } catch (PolyglotException e) {
                    if (e.isSyntaxError()) {
                        // Try as a statement
                        try {
                            Value result = context.eval(Source.newBuilder(Lua, chunk, "stdin").build());
                            // 语句路径同样回显返回值（如 "return 7"、经 = 语法糖转换的行）
                            echoResult(context, result);
                            evaluated = true;
                        } catch (PolyglotException e2) {
                            if (e2.isSyntaxError() && isIncompleteChunk(chunk, e2)) {
                                // Incomplete — read more
                                System.out.print(prompt2);
                                System.out.flush();
                                continue;
                            } else {
                                printLuaError(e2);
                                evaluated = true;
                            }
                        }
                    } else {
                        printLuaError(e);
                        evaluated = true;
                    }
                }
                if (evaluated) {
                    buffer.setLength(0);
                    prompt = getPrompt(context, "_PROMPT", "> ");
                    prompt2 = getPrompt(context, "_PROMPT2", ">> ");
                    System.out.print(prompt);
                    System.out.flush();
                }
            }
        } catch (IOException e) {
            // stdin closed
        }
    }

    /** REPL 结果回显：把求值结果交给 Lua 的 print 打印（保持 Lua 的值格式化语义）。 */
    private static void echoResult(Context context, Value result) {
        if (result == null || result.isNull()) {
            return;
        }
        // Store the result in a global so Lua's print can access it
        context.getBindings(Lua).putMember("__repl_val", result);
        try {
            context.eval(Source.newBuilder(Lua,
                    "local r = __repl_val; __repl_val = nil; print(r)",
                    "repl_print").build());
        } catch (PolyglotException | java.io.IOException pe) {
            // Standard Lua reports print errors as "error calling 'print'"
            System.err.println("error calling 'print'");
        }
    }

    private static String getPrompt(Context context, String name, String defaultPrompt) {
        try {
            Value prompt = context.eval(Lua, "return " + name);
            if (!prompt.isNull() && prompt.isString()) {
                return prompt.asString();
            }
        } catch (Exception ignored) {
        }
        return defaultPrompt;
    }

    private static boolean isIncompleteChunk(String chunk, PolyglotException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        return msg.contains("<EOF>") || msg.contains("expected") || msg.contains("unfinished")
                || msg.contains("incomplete") || msg.contains("'end'")
                || msg.contains("'until'") || msg.contains("'then'")
                || msg.contains("'do'")
                // ANTLR 风格消息："mismatched input 'X' expecting {...}"。
                // 冒犯符号是未闭合的开放符（如 "a = [[..."、未闭合的表/括号）时，
                // 更多输入可能补全片段，按未完处理等待续行
                || (msg.contains("expecting")
                        && (msg.contains("'['") || msg.contains("'{'") || msg.contains("'('")));
    }
}
