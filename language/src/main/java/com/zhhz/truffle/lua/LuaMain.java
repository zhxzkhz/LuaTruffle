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
package com.zhhz.truffle.lua;

import com.zhhz.truffle.lua.runtime.LuaNil;
import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.polyglot.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
        // --interrupt-file <path>：启动器私有选项（不进入 arg 表）。
        // 指定文件出现时调用 context.interrupt()，用于在无法直接投递信号的平台
        // （如 Windows）上模拟 SIGINT：包装脚本 trap INT 后 touch 该文件。
        String interruptFile = null;
        List<String> stripped = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--interrupt-file") && i + 1 < args.length) {
                interruptFile = args[i + 1];
                i++;
            } else {
                stripped.add(args[i]);
            }
        }
        if (interruptFile != null) {
            Thread watcher = getWatcher(interruptFile);
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
            interactive, resourcesPath, noScript, showVersion));
    }

    private static Thread getWatcher(String interruptFile) {
        Path flagPath = Path.of(interruptFile);
        Thread watcher = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(30);
                    if (Files.exists(flagPath)) {
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
        return watcher;
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
                                     boolean interactive, String resourcesPath, boolean noScript,
                                     boolean showVersion) {
        // stdin 直接共享给 Context：io.read、debug.debug 等通过 env.in() 读取。
        // 注意不能给空流——否则 -e 中的 debug.debug() 会立即读到 EOF。
        // REPL 的 BufferedReader 在 -e/-l 语句之后才创建，顺序读取不会互相抢占。
        java.io.InputStream sharedIn = System.in;
        Context context;
        PrintStream err = System.err;
        try {

            HostAccess customAccess = HostAccess.newBuilder(HostAccess.ALL)
                // 允许 Lua 的 table 实现 Java 接口 (如 ApplicationListener)
                .allowAllImplementations(true)
                .allowIterableAccess(true)
                .allowIteratorAccess(true)
                // --- 【关键修复】只映射到【包装类】(Float.class, Integer.class) ---

                // 处理 Lua 数字 (Double) 到 Java 方法需要的 float 参数
                // 当 Java 方法需要 float 或 Float 时，Truffle 会查找到 Float.class 的映射
                .targetTypeMapping(
                    Double.class,  // 源类型 (Lua 内部通常是 Double)
                    Float.class,   // 【正确】目标类型必须是包装类 Float
                    null,          // 转换条件 (总是转换)
                    Double::floatValue, // 转换逻辑：将 Double 转换为 Float 对象
                    HostAccess.TargetMappingPrecedence.HIGHEST
                )

                // 处理 Lua 整数 (Long) 到 Java 方法需要的 int 参数
                // libGDX 大量使用 int (如屏幕宽度、数组索引等)
                .targetTypeMapping(
                    Long.class,
                    Integer.class, // 【正确】目标类型必须是包装类 Integer
                    null,
                    Long::intValue,     // 转换逻辑：将 Long 转换为 Integer 对象
                    HostAccess.TargetMappingPrecedence.HIGHEST
                )

                // (可选) 有时 Lua 的整数也会被传递给需要 float 的 Java 方法
                .targetTypeMapping(
                    Long.class,
                    Float.class,
                    null,
                    Long::floatValue,
                    HostAccess.TargetMappingPrecedence.HIGHEST
                )

                .targetTypeMapping(
                    LuaNil.class, // 假设这是你的 nil 类型
                    Boolean.class,
                    null,
                    (n) -> false, // nil 转换为 false
                    HostAccess.TargetMappingPrecedence.HIGHEST
                )

                // lua 字符串内部是字节透明的 ISO_8859_1 TruffleString（源码按原始 UTF-8 字节保存）。
                // 传给宿主方法（如 Gdx.app.log）时若按 Latin-1 转成 java.lang.String，中文会乱码，
                // 这里改为取出原始字节后按 UTF-8 解码
                .targetTypeMapping(
                    TruffleString.class,
                    String.class,
                    null,
                    ts -> new String(ts.copyToByteArrayUncached(TruffleString.Encoding.ISO_8859_1),
                        StandardCharsets.UTF_8),
                    HostAccess.TargetMappingPrecedence.HIGHEST
                )

                // libGDX 的 Label/BitmapFont.draw 等渲染接口参数是 CharSequence 而非 String，
                // 不加这条会绕过上面的映射，屏幕上的中文就会乱码
                .targetTypeMapping(
                    TruffleString.class,
                    CharSequence.class,
                    null,
                    ts -> (CharSequence) new String(ts.copyToByteArrayUncached(TruffleString.Encoding.ISO_8859_1),
                        StandardCharsets.UTF_8),
                    HostAccess.TargetMappingPrecedence.HIGHEST
                )

                .build();

            context = Context.newBuilder(Lua).in(sharedIn).out(System.out).arguments(Lua, arguments)
                .allowHostClassLookup(className -> true)
                .allowHostClassLoading(true)
                .allowAllAccess(true)
                .allowHostAccess(customAccess)
                //.allowCreateThread(true)
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
                if (!result.isNull()) {
                    System.out.println("返回值：" + Value.asValue(result));
                }
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
                        if (!result.isNull()) {
                            System.out.println("返回值：" + Value.asValue(result));
                        }
                    } catch (IOException ioException) {
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
        System.err.print(sb);
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
        } catch (PolyglotException | IOException pe) {
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
        if (msg.contains("<EOF>") || msg.contains("expected") || msg.contains("unfinished")
            || msg.contains("incomplete") || msg.contains("'end'")
            || msg.contains("'until'") || msg.contains("'then'")
            || msg.contains("'do'")
            || (msg.contains("expecting")
            && (msg.contains("'['") || msg.contains("'{'") || msg.contains("'('")))) {
            return true;
        }
        if (msg.contains("token recognition error")
            && (msg.contains("\"") || msg.contains("'") || msg.contains("\\"))) {
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

    private static final java.util.regex.Pattern LINE_COL_PATTERN =
        java.util.regex.Pattern.compile("-- line (\\d+) col (\\d+):");
}
