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
        // 解释器对 Lua-to-Lua 调用使用 Java 栈递归；标准 Lua 的调用深度上限远大于
        // 默认 JVM 栈容量。这里在执行前把调用深度上限调大，并在下方以大栈线程运行，
        // 使深层递归（例如测试套件中的 range(0,255)、深递归用例）不会触发 JVM 栈溢出。
        if (System.getProperty("lua.maxdepth") == null) {
            System.setProperty("lua.maxdepth", "1000");
        }
        // --interrupt-file <path>：启动器私有选项（不进入 arg 表）。
        // 指定文件出现时调用 context.interrupt()，用于在无法直接投递信号的平台
        // （如 Windows）上模拟 SIGINT：包装脚本 trap INT 后 touch 该文件。
        String interruptFile = null;
        java.util.List<String> stripped = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--interrupt-file") && i + 1 < args.length) {
                interruptFile = args[i + 1];
                i++;
            } else {
                stripped.add(args[i]);
            }
        }
        if (interruptFile != null) {
            final String flag = interruptFile;
            java.nio.file.Path flagPath = java.nio.file.Path.of(flag);
            Thread watcher = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(30);
                        if (java.nio.file.Files.exists(flagPath)) {
                            // 语言侧协作中断：循环迭代边界抛出可被 pcall 捕获的错误。
                            // launcher 不直接依赖语言模块，经反射调用以保持解耦
                            try {
                                Class<?> interrupt = Class.forName("com.zhhz.truffle.lua.runtime.LuaInterrupt");
                                interrupt.getMethod("request").invoke(null);
                            } catch (ReflectiveOperationException e) {
                                System.err.println("cannot request interrupt: " + e.getMessage());
                            }
                            return;
                        }
                    }
                } catch (InterruptedException ignored) {
                    return;
                }
            });
            watcher.setDaemon(true);
            watcher.start();
        }        args = stripped.toArray(new String[0]);

        boolean portableTests = args.length > 0 && args[0].equals("--portable-tests");

        int scriptIndex = portableTests ? 1 : 0;
        
        // Parse interpreter options: -E, -l, -e, -i, -v
        boolean ignoreEnv = false;
        boolean interactive = false;
        boolean showVersion = false;
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
                // 标准 lua：打印版本行到 stdout 后继续处理其余参数（不退出）
                System.out.print("Lua 5.3 (LuaTruffle)  Compatible with Lua 5.3, "
                        + "Copyright (C) 1994-2015 Lua.org, PUC-Rio\n");
                showVersion = true;
                rawOptions.add(opt);
                scriptIndex++;
            } else if (opt.equals("-l")) {
                rawOptions.add(opt);
                scriptIndex++;
                if (args.length > scriptIndex) {
                    loadModules.add(args[scriptIndex]);
                    rawOptions.add(args[scriptIndex]);
                    scriptIndex++;
                } else {
                    System.err.println("lua: '-l' needs argument");
                    System.err.println("usage: lua [options] [script [args]]...");
                    System.exit(1);
                }
            } else if (opt.equals("-e")) {
                rawOptions.add(opt);
                scriptIndex++;
                if (args.length > scriptIndex) {
                    evalStatements.add(args[scriptIndex]);
                    rawOptions.add(args[scriptIndex]);
                    scriptIndex++;
                } else {
                    System.err.println("lua: '-e' needs argument");
                    System.err.println("usage: lua [options] [script [args]]...");
                    System.exit(1);
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
                // 标准 lua：不识别的选项直接报错退出（如 -h、-Ex、-vv）
                System.err.println("lua: unrecognized option '" + opt + "'");
                System.err.println("usage: lua [options] [script [args]]...");
                System.exit(1);
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
        } else if (System.getenv("LUA_STRING_SOURCE") != null) {
            // 调试开关：以 String Source 求值文件内容（等价于 REPL 的求值路径）
            try {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(resourcesPath)),
                        java.nio.charset.StandardCharsets.ISO_8859_1);
                source = Source.newBuilder(Lua, content, "stdin#1").build();
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            File scriptFile = new File(resourcesPath);
            // 与标准 lua 一致：文件块的 chunkname 为 "@<path>"，使错误前缀显示文件名
            // （例如 "script.lua:12: ..." 而非 "[string \"script.lua\"]:12: ..."）。
            source = Source.newBuilder(Lua, scriptFile).name("@" + scriptFile.getPath()).build();
        }
        // Build applicationArguments for the arg table
        // Standard Lua arg layout: arg[-n..-2]=options, arg[-1]=launcher, arg[0]=script, arg[1..n]=scriptArgs
        List<String> argList = new ArrayList<>();
        argList.add(createLauncherCommand());
        argList.addAll(rawOptions);
        argList.add(resourcesPath);
        argList.addAll(Arrays.asList(scriptArguments));
        String[] applicationArguments = argList.toArray(new String[0]);

        // 在大栈线程中执行，避免深层 Lua 递归耗尽默认的 JVM 线程栈。
        final Source runSource = source;
        final String[] runArguments = applicationArguments;
        final boolean runPortableTests = portableTests;
        final List<String> runEvalStatements = evalStatements;
        final List<String> runLoadModules = loadModules;
        final boolean runInteractive = interactive;
        final String runResourcesPath = resourcesPath;
        final boolean runNoScript = noScript;
        final boolean runShowVersion = showVersion;
        final int[] exitCode = {0};
        Thread runner = new Thread(null, () -> exitCode[0] = executeSource(runSource, runArguments,
                runPortableTests, runEvalStatements, runLoadModules, runInteractive, runResourcesPath,
                runNoScript, runShowVersion), "lua-main", 64L * 1024 * 1024);
        runner.start();
        try {
            runner.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        System.exit(exitCode[0]);
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
        String command = "#!/bin/sh\n"
                // 启动器自注册中断映射（/tmp/lua_intr_kill_<pid> 内容：<中断标志> <java进程pid>）。
                // main.lua 的 Ctrl-C 测试用 `kill -INT <pid>` 中断后台 lua：os.execute 的
                // adaptInterruptKill 读到本映射后 touch 标志文件（语言侧协作中断，pcall 可捕获），
                // 重复 INT 则 kill -9 直接终止 java 子进程（模拟不可中断代码的强制终止）。
                // 标志路径用 printf 生成（不创建文件），避免 watcher 一启动就误判中断已到达；
                // 后台化的 java 需显式继承 stdin（POSIX 规定异步列表的 stdin 默认接 /dev/null，
                // 否则 `lua -` 经本包装读取 stdin 会读到 EOF）。
                + "flag=$(printf /tmp/lua_intr_flag_%s_%s \"$$\" \"$(date +%s)\")\n"
                + "exec 3<&0\n"
                + "\"" + java + "\" -Xms16m -Xmx256m --enable-native-access=org.graalvm.truffle "
                + "--sun-misc-unsafe-memory-access=allow --module-path \"" + modulePath + "\" -m "
                + "org.graalvm.lua.launcher/com.zhhz.truffle.lua.launcher.LuaMain "
                + "--interrupt-file \"$flag\" \"$@\" 0<&3 &\n"
                + "exec 3<&-\n"
                + "jpid=$!\n"
                + "echo \"$flag $jpid\" > /tmp/lua_intr_kill_$$\n"
                + "trap 'rm -f \"$flag\" /tmp/lua_intr_kill_$$ \"$flag.fired\" 2>/dev/null; "
                + "kill -9 \"$jpid\" 2>/dev/null' EXIT\n"
                + "wait \"$jpid\"\n"
                + "exit $?\n";
        Files.writeString(script, command);
        script.toFile().setExecutable(true);
        return windows ? script.toString().replace('\\', '/') : script.toString();
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private static int executeSource(Source source, String[] arguments, boolean portableTests,
                                     List<String> evalStatements, List<String> loadModules,
                                     boolean interactive, String resourcesPath, boolean noScript,
                                     boolean showVersion) {
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
                    .allowAllAccess(true)
                    .allowCreateThread(true)
                    .build();
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
                echoScriptReturn(context, result);
            } else if (resourcesPath.equals("-")) {
                // 标准 lua 的无脚本行为（lua.c pmain）：
                // - 带 -i：进入 REPL；
                // - 无脚本且带 -e/-v：执行完即退出（不读 stdin）；
                // - 显式 "-"：整个 stdin 作为一个脚本执行；
                // - 无脚本、stdin 为终端：REPL；非终端：stdin 作为一个脚本。
                boolean explicitStdin = !noScript;
                boolean noStdinInput = noScript && (!evalStatements.isEmpty() || showVersion);
                if (interactive) {
                    // REPL（在方法尾部统一处理）
                } else if (noStdinInput) {
                    // -e/-v 已执行完，直接退出
                } else if (explicitStdin || System.console() == null) {
                    // 延迟到此处构建，避免在 -e/-l 之前吞掉 stdin
                    try {
                        Source stdinScript = Source.newBuilder(Lua, new InputStreamReader(System.in), "=stdin").build();
                        Value result = context.eval(stdinScript);
                        echoScriptReturn(context, result);
                    } catch (java.io.IOException ioException) {
                        err.println(ioException.getMessage());
                        return 1;
                    }
                } else {
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
                printLuaError(ex, arguments.length > 0 ? arguments[0] : "lua");
            }
            return 1;
        } finally {
            context.close();
        }
    }

    /** REPL 内的错误打印（标准 lua 交互模式无 progname 前缀）。 */
    private static void printLuaError(PolyglotException e) {
        printLuaError(e, null);
    }

    private static void printLuaError(PolyglotException e, String progname) {
        // 标准 lua CLI 只打印 "progname: message"：位置信息（src:line:）已包含在
        // 消息内（error() 为字符串消息添加；非字符串对象无位置）。
        // 客栈回溯来自 polyglot 异常的 guest 栈。
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = e.isSyntaxError() ? "syntax error" : "unknown Lua error";
        }
        // 宿主异常（Java 侧抛出）打印完整 Java 堆栈，否则只有 guest 帧无法定位
        if (e.isHostException()) {
            e.asHostException().printStackTrace(System.err);
        }
        StringBuilder sb = new StringBuilder();
        if (progname != null && !progname.isBlank()) {
            sb.append(progname).append(": ");
        }
        sb.append(message);
        // 显式 \n：println 在 Windows 上产生 \r\n，会破坏与标准 lua 输出的逐字节比较
        writeBytes(System.err, sb.toString());
        System.err.print('\n');

        SourceSection location = e.getSourceLocation();
        if (location == null) {
            for (PolyglotException.StackFrame frame : e.getPolyglotStackTrace()) {
                if (frame.isGuestFrame() && frame.getSourceLocation() != null) {
                    location = frame.getSourceLocation();
                    break;
                }
            }
        }
        boolean printedHeader = false;
        for (PolyglotException.StackFrame frame : e.getPolyglotStackTrace()) {
            if (!frame.isGuestFrame() || frame.getSourceLocation() == null) {
                continue;
            }
            if (!printedHeader) {
                System.err.print("stack traceback:\n");
                printedHeader = true;
            }
            SourceSection frameLocation = frame.getSourceLocation();
            StringBuilder line = new StringBuilder("\t");
            appendLocation(line, frameLocation);
            String rootName = frame.getRootName();
            if (rootName != null && !rootName.isBlank()) {
                line.append(": in ").append(formatFunction(rootName));
            }
            System.err.print(line);
            System.err.print('\n');
        }
    }

    /**
     * 语言内部的字符串按字节模型（Latin-1 字符 = 一个字节）传递到异常消息中，
     * 直接按 ISO-8859-1 写回原始字节，避免 System.err 的 UTF-8 编码把高位字符
     * 二次编码成乱码；含真实 Unicode 字符（&gt;0xFF）的文本按 UTF-8 输出。
     */
    private static void writeBytes(PrintStream stream, String text) {
        byte[] bytes;
        for (int i = 0; ; i++) {
            if (i < text.length() && text.charAt(i) <= 0xFF) {
                continue;
            }
            bytes = text.getBytes(i < text.length()
                    ? java.nio.charset.StandardCharsets.UTF_8
                    : java.nio.charset.StandardCharsets.ISO_8859_1);
            break;
        }
        try {
            stream.write(bytes);
            stream.flush();
        } catch (java.io.IOException ex) {
            throw new java.io.UncheckedIOException(ex);
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
        int evalSeq = 0;
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
                evalSeq++;
                boolean evaluated = false;
                try {
                    Value result = context.eval(Source.newBuilder(Lua, "return " + chunk, "stdin#" + evalSeq).build());
                    if (System.getenv("DBG_REPL") != null) {
                        System.err.println("[DBGR] SUCCESS-RETURN len=" + chunk.length()
                                + " tail=" + chunk.substring(Math.max(0, chunk.length() - 30)).replace("\n", "\\n"));
                    }
                    echoResult(context, result);
                    evaluated = true;
                } catch (PolyglotException e) {
                    if (System.getenv("DBG_REPL") != null) {
                        System.err.println("[DBGR] FIRST-FAIL isSyntaxError=" + e.isSyntaxError()
                                + " msg=" + String.valueOf(e.getMessage()).replace("\n", " | "));
                    }
                    if (e.isSyntaxError()) {
                        // Try as a statement
                        try {
                            Value result = context.eval(Source.newBuilder(Lua, chunk, "stdin#" + evalSeq).build());
                            // 语句路径同样回显返回值（如 "return 7"、经 = 语法糖转换的行）
                            echoResult(context, result);
                            evaluated = true;
                        } catch (PolyglotException e2) {
                            if (System.getenv("DBG_REPL") != null) {
                                System.err.println("[DBGR] chunk=" + chunk.replace("\n", "\\n")
                                        + "\n[DBGR] err=" + e2.getMessage().replace("\n", " | ")
                                        + "\n[DBGR] incomplete=" + isIncompleteChunk(chunk, e2));
                            }
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
                if (System.getenv("DBG_REPL") != null) {
                    System.err.println("[DBGR] ITER len=" + chunk.length() + " evaluated=" + evaluated
                            + " chunk=" + chunk.replace("\n", "|"));
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

    /**
     * 脚本/标准输入执行结果回显（launcher 调试用途；标准 lua 不打印返回值）。
     * <p>
     * 多值返回（LuaMultiValue：`return 111, 222`）与带数组部分的表（`return {111, 222}`）
     * 都通过 polyglot 的【数组协议】读取（hasArrayElements / getArrayElement）；
     * 不要用 getMemberKeys()：它枚举的是具名成员键，Lua 表的整数键不是字符串，
     * 在字符串化的成员 API 下会变成 null（观察到 [null, null]）。
     */
    private static void echoScriptReturn(Context context, Value result) {
        if (result == null || result.isNull()) {
            return;
        }
        // 空的多值返回（函数/脚本自然结束，零个返回值）：不打印。
        if (result.hasArrayElements() && result.getArraySize() == 0 && !result.hasMembers()) {
            return;
        }
        // 多值返回（LuaMultiValue：return 111, 222）：host 端直接按数组元素读取。
        if (result.hasArrayElements() && result.getArraySize() > 0 && !result.hasMembers()) {
            StringBuilder sb = new StringBuilder("返回值：");
            long n = result.getArraySize();
            for (long i = 0; i < n; i++) {
                if (i > 0) {
                    sb.append("\t");
                }
                sb.append(result.getArrayElement(i));
            }
            System.out.println(sb);
            return;
        }
        // Lua 表：委托 Lua 自身格式化（pairs 可遍历任意键类型；host 端 getMemberKeys
        // 只有字符串键，整数键（如 [1111]=...）会丢失，混合键哈希部分也读不到）。
        if (result.hasMembers()) {
            context.getBindings(Lua).putMember("__repl_val", result);
            evalPrint(context, "local r = __repl_val; __repl_val = nil"
                    + " local parts = {}"
                    + " for i = 1, #r do parts[#parts+1] = tostring(r[i]) end"
                    + " for k, v in pairs(r) do"
                    + "  local ik = tonumber(k)"
                    + "  if not (ik and ik >= 1 and ik <= #r and ik % 1 == 0) then"
                    + "   parts[#parts+1] = tostring(k) .. ' = ' .. tostring(v)"
                    + "  end end"
                    + " print('返回值：' .. table.concat(parts, '\\t'))");
            return;
        }
        System.out.println("返回值：" + Value.asValue(result));
    }

    /** REPL 结果回显：把求值结果交给 Lua 的 print 打印（保持 Lua 的值格式化语义）。 */
    private static void echoResult(Context context, Value result) {
        if (result == null || result.isNull()) {
            return;
        }
        // 多返回值：逐个存入临时全局变量后以多参数调用 print（"13\t22" 格式）
        if (result.hasArrayElements() && result.getArraySize() > 1) {
            StringBuilder call = new StringBuilder("print(");
            long n = result.getArraySize();
            for (long i = 0; i < n; i++) {
                context.getBindings(Lua).putMember("__repl_v" + i, result.getArrayElement(i));
                call.append(i > 0 ? ", " : "").append("__repl_v").append(i);
            }
            call.append(")");
            evalPrint(context, call.toString());
            return;
        }
        // Store the result in a global so Lua's print can access it
        context.getBindings(Lua).putMember("__repl_val", result);
        evalPrint(context, "local r = __repl_val; __repl_val = nil; print(r)");
    }

    private static void evalPrint(Context context, String code) {
        try {
            context.eval(Source.newBuilder(Lua, code, "repl_print").build());
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

    /**
     * 判断 REPL 缓冲是否是"未完成的片段"（应继续读入更多行）。
     * <p>
     * 标准 lua 只认 "near <eof>"，我们的 ANTLR 错误格式不同，按三类特征判定：
     * 1. 显式 &lt;EOF&gt; / 期待 'end' 等续行关键字 / 未闭合的开放符；
     * 2. 词法层的未终结字符串（token 文本中含引号或反斜杠）；
     * 3. 【位置判定】错误的 line/col 位于缓冲末尾或之后——更多输入可能补全。
     *    （例如 "function f ( x ) local" 在 'local' 之后报错，即 col 已越过行尾）
     */
    private static boolean isIncompleteChunk(String chunk, PolyglotException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        // 核心判定：解析器的冒犯 token 是 <EOF>（真正读到了输入末尾仍不完整）。
        // 覆盖 ANTLR 消息形态："missing 'end' at '<EOF>'"、"mismatched input '<EOF>'
        // expecting ..."、"no viable alternative at input '<EOF>'"。
        // 注意不能用 msg.contains("<EOF>")：mismatched input 的【期望集合】里
        // 总是含 <EOF> 与 '(' 等，会把真正损坏的 chunk（如以 '+' 开头）误判为未完成。
        if (msg.contains("at '<EOF>'") || msg.contains("input '<EOF>'")) {
            return true;
        }
        if (msg.contains("unfinished") || msg.contains("incomplete") || msg.contains("<eof>")) {
            return true;
        }
        // 未结束的字符串（"malformed string near '\"'")：标准 lua 的词法器此时报
        // "unfinished string ... <eof>"，REPL 依赖 <eof> 后缀继续读取。我们的词法
        // 消息不同，这里等价判定：词法片段恰好是整个 chunk 的末尾 → 字符串尚未闭合。
        if (msg.contains("malformed string near '")) {
            String token = extractNearToken(msg);
            if (token != null && !token.isEmpty() && chunk.endsWith(token)) {
                return true;
            }
        }
        // 未闭合的长字符串/长注释（如 "a = [[b"）：词法器不报错，而是把 '[' 当普通
        // 符号交给解析器（报 mismatched input '['）。这里按长括号配平判断：
        // [=*[ 开头多于 ]=*] 结尾 → 长串尚未闭合，继续读取。
        if (countLongBrackets(chunk, LONG_OPEN) > countLongBrackets(chunk, LONG_CLOSE)) {
            return true;
        }
        // "missing 'X' at '('" 一类：冒犯符号是开放符（即行尾字符），其后仍需更多输入
        if (msg.contains("missing")
                && (msg.contains("at '('") || msg.contains("at '['") || msg.contains("at '{'"))) {
            return true;
        }
        java.util.regex.Matcher matcher = LINE_COL_PATTERN.matcher(msg);
        String[] lines = chunk.split("\n", -1);
        int lastLine = lines.length;
        int lastLineLength = lines[lastLine - 1].length();
        int bestLine = -1;
        int bestCol = -1;
        while (matcher.find()) {
            int line = Integer.parseInt(matcher.group(1));
            int col = Integer.parseInt(matcher.group(2));
            if (line > bestLine || (line == bestLine && col > bestCol)) {
                bestLine = line;
                bestCol = col;
            }
        }
        return bestLine > lastLine || (bestLine == lastLine && bestCol > lastLineLength);
    }

    /** 从 "malformed string near '...'" 消息中提取引号内的词法片段。 */
    private static String extractNearToken(String msg) {
        int firstQuote = msg.indexOf('\'');
        int lastQuote = msg.lastIndexOf('\'');
        if (firstQuote >= 0 && lastQuote > firstQuote) {
            return msg.substring(firstQuote + 1, lastQuote);
        }
        return null;
    }

    private static final java.util.regex.Pattern LONG_OPEN =
            java.util.regex.Pattern.compile("\\[(=*)\\[");
    private static final java.util.regex.Pattern LONG_CLOSE =
            java.util.regex.Pattern.compile("\\](=*)\\]");

    private static int countLongBrackets(String s, java.util.regex.Pattern pattern) {
        java.util.regex.Matcher m = pattern.matcher(s);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    private static final java.util.regex.Pattern LINE_COL_PATTERN =
            java.util.regex.Pattern.compile("-- line (\\d+) col (\\d+):");
}
