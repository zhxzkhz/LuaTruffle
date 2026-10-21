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
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.builtins.*;
import com.zhhz.truffle.lua.builtins.pack.LoadLibBuiltinNodeGen;
import com.zhhz.truffle.lua.builtins.pack.LuaPathSearcherNodeGen;
import com.zhhz.truffle.lua.builtins.pack.PreloadSearcherNodeGen;
import com.zhhz.truffle.lua.builtins.pack.SearchPathBuiltinNodeGen;
import org.graalvm.polyglot.Context;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * SL在执行过程中的运行时状态。上下文由{@link LuaLanguage}创建。
 * <p>
 * 在执行一个脚本期间有两个不同的上下文实例是错误的。
 * 但是，如果两个单独的脚本同时在一个Java VM中运行，则它们具有不同的
 * 背景。因此，上下文不是单例。
 */
@Bind.DefaultExpression("get($node)")
public final class LuaContext {

    private final LuaLanguage language;
    @CompilationFinal private Env env;

    private final PrintWriter output;
    private final PrintWriter error;

    // 【核心】全局变量表
    private final LuaTable globals;
    private final Shape tableShape;
    private final Shape functionShape;
    private final Shape booleanShape;
    private final Shape fileShape;
    private final Shape threadShape;
    private LuaFileHandle defaultInput;
    private LuaFileHandle defaultOutput;

    // 打开的文件句柄弱引用注册表：用于在 collectgarbage() 时关闭已不可达的文件
    // （模拟 C Lua 的 FILE* __gc 终结器，确保 Windows 上能删除被 GC 的打开文件）。
    private final java.util.List<FileHandleEntry> openFileHandles = new java.util.ArrayList<>();

    private static final class FileHandleEntry {
        final java.lang.ref.WeakReference<Object> handle;
        final Runnable closeAction;

        FileHandleEntry(Object handle, Runnable closeAction) {
            this.handle = new java.lang.ref.WeakReference<>(handle);
            this.closeAction = closeAction;
        }
    }

    /** 注册一个打开的文件句柄，供 collectgarbage() 在其不可达时关闭。 */
    public void registerFileHandle(Object handle, Runnable closeAction) {
        synchronized (openFileHandles) {
            openFileHandles.add(new FileHandleEntry(handle, closeAction));
        }
    }

    /** 关闭所有已不可达的文件句柄（在 GC 之后调用）。 */
    public void collectFileHandles() {
        System.gc();
        synchronized (openFileHandles) {
            java.util.Iterator<FileHandleEntry> iterator = openFileHandles.iterator();
            while (iterator.hasNext()) {
                FileHandleEntry entry = iterator.next();
                if (entry.handle.get() == null) {
                    try {
                        entry.closeAction.run();
                    } catch (Exception ignored) {
                        // 终结过程忽略错误
                    }
                    iterator.remove();
                }
            }
        }
    }

    private LuaFunction ipairsIterator;
    private LuaFunction utf8Iterator;

    // collectgarbage 的状态（复刻标准 Lua 的 GC 控制接口；JVM 托管 GC 下仅模拟状态）。
    private boolean gcRunning = true;
    private long gcPause = 200;
    private long gcStepMul = 200;

    public boolean isGcRunning() {
        return gcRunning;
    }

    public void setGcRunning(boolean running) {
        this.gcRunning = running;
    }

    public long getGcPause() {
        return gcPause;
    }

    public void setGcPause(long pause) {
        this.gcPause = pause;
    }

    public long getGcStepMul() {
        return gcStepMul;
    }

    public void setGcStepMul(long stepMul) {
        this.gcStepMul = stepMul;
    }

    // debug.sethook 钩子（见 LuaLineHook 与 DebugLibrary.sethook）。
    // 钩子函数/掩码/计数/去重行/重入标志等状态是【每线程】的，存放于 LuaThread
    // （主线程使用 mainThread 实例），以支持协程级 sethook/gethook；
    // 此处仅保留上下文级的 instrumentation 绑定状态。
    private com.oracle.truffle.api.instrumentation.EventBinding<?> hookBinding;
    private final java.util.List<com.oracle.truffle.api.instrumentation.EventBinding<?>> extraHookBindings = new java.util.ArrayList<>();
    /** 已附加的监听器种类标记，避免重复附加/拆卸。 */
    private boolean hookBindingStatement;
    private boolean hookBindingCall;
    /** 已设置钩子的线程集合（用于绑定生命周期管理）。 */
    private final java.util.Set<LuaThread> hookedThreads =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
    /** 是否存在任意活跃钩子：VM 解释循环据此快速跳过每指令的线程状态查询。 */
    private volatile boolean anyHookActive;

    // coroutine 当前线程跟踪：每个 Java 线程对应一个 LuaThread（协程）或 null（主线程）。
    // 主线程的 LuaThread 也存入此 ThreadLocal，以便 coroutine.running() 能正确返回。
    private final ThreadLocal<LuaThread> currentCoroutine = new ThreadLocal<>();
    /** 主线程对象 */
    private LuaThread mainThread;

    /** 当前执行线程对应的 LuaThread（协程或主线程）。 */
    public LuaThread currentThread() {
        LuaThread thread = currentCoroutine.get();
        return thread != null ? thread : mainThread;
    }

    // ---- 钩子状态：委托到当前线程 ----
    public Object getHookFunction() { LuaThread t = currentThread(); return t == null ? null : t.getHookFunction(); }
    public void setHookFunction(Object hook) { LuaThread t = currentThread(); if (t != null) t.setHookFunction(hook); }
    public String getHookMask() { LuaThread t = currentThread(); return t == null ? "" : t.getHookMask(); }
    public void setHookMask(String mask) { LuaThread t = currentThread(); if (t != null) t.setHookMask(mask); }
    public int getHookCount() { LuaThread t = currentThread(); return t == null ? 0 : t.getHookCount(); }
    public void setHookCount(int count) { LuaThread t = currentThread(); if (t != null) t.setHookCount(count); }
    /** 判断掩码中是否包含指定字符（'l'/'c'/'r'）。 */
    public boolean isHookMask(char c) { LuaThread t = currentThread(); return t != null && t.isHookMask(c); }
    public int getLastHookLine() { LuaThread t = currentThread(); return t == null ? -1 : t.getLastHookLine(); }
    public void setLastHookLine(int line) { LuaThread t = currentThread(); if (t != null) t.setLastHookLine(line); }
    public boolean isInHook() { LuaThread t = currentThread(); return t != null && t.isInHook(); }
    public void setInHook(boolean value) { LuaThread t = currentThread(); if (t != null) t.setInHook(value); }
    public int getInstructionCount() { LuaThread t = currentThread(); return t == null ? 0 : t.getInstructionCount(); }
    public void setInstructionCount(int count) { LuaThread t = currentThread(); if (t != null) t.setInstructionCount(count); }
    /** 标记/取消“下一次函数进入来自尾调用”（AST 尾调用重试循环使用）。 */
    public void setPendingTailCall(boolean value) { LuaThread t = currentThread(); if (t != null) t.setPendingTailCall(value); }
    public boolean takePendingTailCall() {
        LuaThread t = currentThread();
        return t != null && t.takePendingTailCall();
    }

    // ---- 上下文级绑定状态 ----
    public com.oracle.truffle.api.instrumentation.EventBinding<?> getHookBinding() { return hookBinding; }
    public void setHookBinding(com.oracle.truffle.api.instrumentation.EventBinding<?> binding) { this.hookBinding = binding; }
    public java.util.List<com.oracle.truffle.api.instrumentation.EventBinding<?>> getExtraHookBindings() { return extraHookBindings; }
    public void addExtraHookBinding(com.oracle.truffle.api.instrumentation.EventBinding<?> binding) { extraHookBindings.add(binding); }
    public void clearExtraHookBindings() { extraHookBindings.clear(); }
    public boolean isHookBindingStatement() { return hookBindingStatement; }
    public void setHookBindingStatement(boolean value) { this.hookBindingStatement = value; }
    public boolean isHookBindingCall() { return hookBindingCall; }
    public void setHookBindingCall(boolean value) { this.hookBindingCall = value; }
    public java.util.Set<LuaThread> getHookedThreads() { return hookedThreads; }
    public boolean isAnyHookActive() { return anyHookActive; }
    public void setAnyHookActive(boolean value) { this.anyHookActive = value; }

    // 字节码 VM 的逻辑调用栈（见 com.zhhz.truffle.lua.vm.VmFrame）。
    // VM 整条调用栈运行在单个 Truffle 帧内，因此显式维护此列表供 debug 库做层级查询。
    // 列表按执行线程隔离（存放于 LuaThread），避免协程交错运行时的串栈。
    /** 构造期（mainThread 尚未建立）的后备调试栈；正常执行时不会用到。 */
    private final LuaThread.DebugStacks fallbackDebugStacks = new LuaThread.DebugStacks();

    private LuaThread.DebugStacks debugStacks() {
        LuaThread thread = currentThread();
        return thread != null ? thread.debugStacks() : fallbackDebugStacks;
    }

    public java.util.List<com.zhhz.truffle.lua.vm.VmFrame> getVmFrames() { return debugStacks().vmFrames; }

    public java.util.List<com.zhhz.truffle.lua.vm.LuaVm> getVmContainers() { return debugStacks().vmContainers; }

    public void enterVmContainer(com.zhhz.truffle.lua.vm.LuaVm vm) { debugStacks().vmContainers.add(vm); }

    public void exitVmContainer(com.zhhz.truffle.lua.vm.LuaVm vm) {
        java.util.List<com.zhhz.truffle.lua.vm.LuaVm> containers = debugStacks().vmContainers;
        int last = containers.size() - 1;
        if (last >= 0 && containers.get(last) == vm) {
            containers.remove(last);
        }
    }

    // coroutine 线程跟踪
    public LuaThread getCurrentCoroutine() { return currentCoroutine.get(); }
    public void setCurrentCoroutine(LuaThread thread) { currentCoroutine.set(thread); }

    /** 本上下文创建的所有协程（用于关闭时唤醒/回收其线程）。 */
    private final java.util.Set<LuaThread> coroutineThreads =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public void registerCoroutine(LuaThread thread) { coroutineThreads.add(thread); }

    /**
     * 关闭时回收所有协程线程：唤醒阻塞在 yield/resume 交换上的线程并等待其结束。
     * Truffle 在 dispose 完成后要求语言线程已全部退出，否则上下文关闭失败。
     */
    public void disposeCoroutines() {
        for (LuaThread thread : coroutineThreads) {
            thread.dispose();
        }
        for (LuaThread thread : coroutineThreads) {
            thread.awaitTermination(2000L);
        }
        coroutineThreads.clear();
    }

    // tostring 内建函数对象：error() 格式化非字符串错误对象时经由它调用
    // __tostring（保持标准 lua message handler 的帧深度），不能被用户替换的全局覆盖
    private LuaFunction tostringFunction;

    // import() 已导入符号缓存（Java 类 / polyglot 符号按名缓存）。
    // 宿主符号查找是 @TruffleBoundary 慢路径操作，缓存使重复导入零查找开销。
    // 仅缓存成功结果：导入失败会抛出 Lua 错误，不会写入此表。
    private final Map<String, Object> importedSymbols = new HashMap<>();

    // 【1. 新增】一个用于存储【类型默认元表】的 Map
    private final Map<Class<?>, LuaTable> typeMetatables = new HashMap<>();

    // 默认的搜索路径，模拟标准 Lua
    // ? 代表模块名。分号 ; 分隔不同的路径。
    private static final String DEFAULT_PATH = "./?.lua;./?/init.lua";
    private static final String DEFAULT_CPATH = "./?.so;./?.dll;./loadall.so;./lua/?.so;./lua/?.dll";

    private boolean packagePathInitialized = false;
    private boolean luaInitExecuted = false;
    private TruffleFile scriptDirectory;
    private Object[] scriptVarargs = new Object[0];

    public LuaContext(LuaLanguage language, Env env) {
        this.env = env;

        // Lua 字符串内部按 Latin-1 字节模型存储（每个字符 = 一个字节），输出必须按
        // ISO-8859-1 编码直通底层字节流，与标准 lua 的 8-bit clean 行为一致：
        // 写入什么字节，终端就收到什么字节（UTF-8/GBK 源文件的中文可原样输出）。
        // 若按默认 UTF-8 编码会再次编码导致乱码。
        this.output = new PrintWriter(new java.io.OutputStreamWriter(env.out(), java.nio.charset.StandardCharsets.ISO_8859_1), true);
        this.error = new PrintWriter(new java.io.OutputStreamWriter(env.err(), java.nio.charset.StandardCharsets.ISO_8859_1), true);
        this.language = language;

        // 1. 【关键】在上下文创建时，实例化全局表
        //    我们需要一个方法来创建 table 的初始 Shape
        this.tableShape = Shape.newBuilder().layout(LuaTable.class, MethodHandles.lookup()).build();
        this.functionShape = Shape.newBuilder().layout(LuaFunction.class, MethodHandles.lookup()).build();
        this.booleanShape = Shape.newBuilder().layout(LuaBoolean.class, MethodHandles.lookup()).build();
        this.fileShape = Shape.newBuilder().layout(LuaFileHandle.class, MethodHandles.lookup()).build();
        this.threadShape = Shape.newBuilder().layout(LuaThread.class, MethodHandles.lookup()).build();

        this.globals = new LuaTable(tableShape);
        // 2. 将全局表自身放入全局表中，键为 "_G"
        //    这是标准的 Lua 行为
        this.globals.rawset(toTruffleString("_G"), this.globals, DynamicObjectLibrary.getUncached());
        // Lua 5.3: _ENV 是主块的 upvalue，默认指向全局表。简化实现为全局变量，
        // 使 _ENV.x = v 与 x = v 等价（对主块外的函数，_ENV 仍解析为全局 _ENV）。
        this.globals.rawset(toTruffleString("_ENV"), this.globals, DynamicObjectLibrary.getUncached());
        // Lua's standard version marker is observable by startup scripts and libraries.
        this.globals.rawset(toTruffleString("_VERSION"), toTruffleString("Lua 5.3"),
                DynamicObjectLibrary.getUncached());
        LuaTable arguments = new LuaTable(tableShape);
        String[] applicationArguments = env.getApplicationArguments();
        // applicationArguments layout: [launcher, options..., scriptPath, scriptArgs...]
        // Options: -E, -l <mod>, -e <stmt>, -i (each takes 1 or 2 slots)
        // Standard Lua arg table: arg[-1]=launcher, arg[-2..-n]=options, arg[0]=scriptPath, arg[1..n]=scriptArgs
        
        // Find where the script path starts (first non-option after launcher)
        int scriptStart = 1;
        while (scriptStart < applicationArguments.length) {
            String opt = applicationArguments[scriptStart];
            if (opt.equals("--")) {
                scriptStart++; // end of options marker, skip it
                break;
            }
            if (!opt.startsWith("-") || opt.equals("-")) {
                break; // found script path
            }
            if (opt.equals("-E") || opt.equals("-i") || opt.equals("-v")) {
                scriptStart++;
            } else if (opt.equals("-l") || opt.equals("-e")) {
                scriptStart += 2; // option + argument
            } else if (opt.startsWith("-l") || opt.startsWith("-e")) {
                scriptStart++; // combined like -lmod
            } else {
                break; // unknown option, treat as script path
            }
        }
        
        // arg[0] = script path, arg[1..n] = script args
        if (scriptStart < applicationArguments.length) {
            arguments.rawset(0L, toTruffleString(applicationArguments[scriptStart]));
        }
        // Store script arguments as varargs for the main chunk
        int numScriptArgs = applicationArguments.length - scriptStart - 1;
        if (numScriptArgs > 0) {
            Object[] varargs = new Object[numScriptArgs];
            for (int i = 0; i < numScriptArgs; i++) {
                varargs[i] = toTruffleString(applicationArguments[scriptStart + 1 + i]);
            }
            this.scriptVarargs = varargs;
        }
        for (int i = scriptStart + 1; i < applicationArguments.length; i++) {
            arguments.rawset((long) i - scriptStart, toTruffleString(applicationArguments[i]));
        }
        // arg[-1] = launcher, arg[-2..-n] = options (in reverse order)
        int negIdx = -1;
        for (int i = Math.min(scriptStart - 1, applicationArguments.length - 1); i >= 0; i--) {
            arguments.rawset((long) negIdx, toTruffleString(applicationArguments[i]));
            negIdx--;
        }
        this.globals.rawset(toTruffleString("arg"), arguments, DynamicObjectLibrary.getUncached());
        //初始化LuaBoolean
        LuaBoolean.get(this,true);
        LuaBoolean.get(this,false);
        //安装核心库
        installBuiltins();
        //初始化 package 表
        setupPackageSystem();
        // 创建主线程 LuaThread 对象并设为当前协程（coroutine.running 返回它）
        mainThread = new LuaThread(threadShape, null, null, toTruffleString("main"), null, env, true);
        currentCoroutine.set(mainThread);
    }

    // --- 提供 Getter 以便外界可以访问这些 Shape ---

    /**
     * 共享的字节码 VM 入口 CallTarget。由字节码 {@code load} 产生的 VM 闭包统一使用它作为
     * {@link LuaFunction#getCallTarget()}，从而无论从 VM 还是 AST 引擎调用都走同一条路径。
     */
    private CallTarget vmCallTarget;

    public Shape getTableShape() { return tableShape; }
    public Shape getFunctionShape() { return functionShape; }
    public Shape getBooleanShape() { return booleanShape; }
    public Shape getFileShape() { return fileShape; }
    public Shape getThreadShape() { return threadShape; }
    public LuaLanguage getLanguage() { return language; }

    /** 惰性创建共享的字节码 VM 入口 CallTarget。 */
    @TruffleBoundary
    public CallTarget getVmCallTarget() {
        if (vmCallTarget == null) {
            vmCallTarget = new com.zhhz.truffle.lua.vm.LuaVmRootNode(language).getCallTarget();
        }
        return vmCallTarget;
    }

    /** 查找内嵌 lua-hook instrument 暴露的 Instrumenter（debug.sethook 用）。 */
    @TruffleBoundary
    public com.oracle.truffle.api.instrumentation.Instrumenter lookupHookInstrumenter() {
        java.util.Map<String, com.oracle.truffle.api.InstrumentInfo> instruments = env.getInstruments();
        com.oracle.truffle.api.InstrumentInfo info = instruments.get(com.zhhz.truffle.lua.LuaHookInstrument.ID);
        if (info == null) {
            return null;
        }
        // lookup(InstrumentInfo, Class) 会触发该 instrument 的加载并取回已注册服务
        com.zhhz.truffle.lua.LuaHookInstrument hookInstrument =
                env.lookup(info, com.zhhz.truffle.lua.LuaHookInstrument.class);
        return hookInstrument != null ? hookInstrument.getInstrumenterHandle() : null;
    }

    public LuaFileHandle getDefaultInput() { return defaultInput; }
    public void setDefaultInput(LuaFileHandle input) { this.defaultInput = input; }
    public LuaFileHandle getDefaultOutput() { return defaultOutput; }
    public void setDefaultOutput(LuaFileHandle output) { this.defaultOutput = output; }


    /**
     * 修补（Patch）{@link LuaContext} 以使用新的 {@link Env}。
     * <p>
     * 该方法会在 Native Image（原生镜像）执行期间被调用，
     * 通常是由 {@link Context#create(java.lang.String...)} 触发的结果。
     *
     * @param newEnv 要使用的新 {@link Env} 环境对象。
     * @see LuaLanguage#patchContext(LuaContext, Env)
     */
    public void patchContext(Env newEnv) {
        this.env = newEnv;
    }

    /**
     * 返回当前的Truffle环境。
     */
    public Env getEnv() {
        return env;
    }

    public PrintWriter getOutput() {
        return output;
    }

    public PrintWriter getError() {
        return error;
    }

    @TruffleBoundary
    public Object getImportedSymbol(String name) {
        return importedSymbols.get(name);
    }

    public void cacheImportedSymbol(String name, Object symbol) {
        importedSymbols.put(name, symbol);
    }

    public static TruffleString toTruffleString(String s){
        return TruffleString.fromJavaStringUncached(s,LuaLanguage.STRING_ENCODING);
    }

    public boolean isPackagePathInitialized() {
        return packagePathInitialized;
    }

    public void setPackagePathInitialized(boolean initialized) {
        this.packagePathInitialized = initialized;
    }

    public boolean isLuaInitExecuted() {
        return luaInitExecuted;
    }

    public void setLuaInitExecuted(boolean executed) {
        this.luaInitExecuted = executed;
    }

    /**
     * 统一的用户文件名解析：绝对路径原样返回；相对路径在已知入口脚本目录时
     * 优先相对脚本目录解析，否则相对进程工作目录。
     * <p>
     * loadfile/dofile、io.open、os.remove/rename 等都必须经过同一个基准，
     * 否则「先写入再按名读取」的程序（如官方测试套件）会因两套基准而失败。
     */
    @TruffleBoundary
    public TruffleFile resolveScriptFile(String path) {
        TruffleFile file = getEnv().getPublicTruffleFile(path);
        if (!file.isAbsolute() && scriptDirectory != null) {
            return scriptDirectory.resolve(path);
        }
        return file;
    }

    /** {@link #resolveScriptFile(String)} 的 java.nio.Path 版本。 */
    @TruffleBoundary
    public java.nio.file.Path resolveScriptPath(String path) {
        try {
            String resolved = resolveScriptFile(path).getPath();
            if (resolved != null) {
                return java.nio.file.Path.of(resolved);
            }
        } catch (Exception ignored) {
            // 文件系统受限时退回原始名称，由调用方按工作目录处理
        }
        return java.nio.file.Path.of(path);
    }

    public TruffleFile getScriptDirectory() {
        return scriptDirectory;
    }

    public Object[] getScriptVarargs() {
        return scriptVarargs;
    }

    public void setScriptVarargs(Object[] varargs) {
        this.scriptVarargs = varargs;
    }

    private void installBuiltins() {


        // 创建一个 LoadBuiltinNode 的工厂
        Supplier<LuaBuiltinNode> loadFactory = LoadBuiltinNodeGen::create;
        // 将它同时注册为 load 和 loadstring
        installBuiltin("load", loadFactory);
        installBuiltin("loadstring", loadFactory);
        installBuiltin(LuaLoadFileBuiltinNodeGen.create());
        installBuiltin(LuaDoFileBuiltinNodeGen.create());

        installBuiltin("print",LuaPrintBuiltinNodeGen::create);
        installBuiltin("require", LuaRequireBuiltinNodeGen::create);
        installBuiltin("import", LuaImportBuiltinNodeGen::create);
        installBuiltin("error",LuaErrorBuiltinNodeGen::create);
        installBuiltin("next",LuaNextBuiltinNodeGen::create);
        installBuiltin("pairs",LuaPairsBuiltinNodeGen::create);
        createIpairsIterator();
        installBuiltin("ipairs",LuaIpairsBuiltinNodeGen::create);
        createUtf8Iterator();

        installBuiltin("setmetatable",LuaSetMetatableBuiltinNodeGen::create);
        installBuiltin("getmetatable", LuaGetMetatableBuiltinNodeGen::create);

        installBuiltin(LuaPcallBuiltinNodeGen.create());
        installBuiltin(LuaXpcallBuiltinNodeGen.create());
        installBuiltin(LuaAssertBuiltinNodeGen.create());
        installBuiltin(LuaCollectGarbageBuiltinNodeGen.create());
        installBuiltin(LuaRawGetBuiltinNodeGen.create());
        installBuiltin(LuaRawSetBuiltinNodeGen.create());
        installBuiltin("rawlen", LuaRawLenBuiltinNodeGen::create);
        installBuiltin("rawequal", LuaRawEqualBuiltinNodeGen::create);
        installBuiltin(LuaTypeBuiltinNodeGen.create());
        installBuiltin(LuaSelectBuiltinNodeGen.create());



        installBuiltin(LuaToNumberBuiltinNodeGen.create());
        installBuiltin(LuaToStringBuiltinNodeGen.create());


        // 【关键】调用通用的批量安装方法
        //System.out.println("OsLibrary.clock() = " + OsLibrary.clock());

        installBuiltinLibrary(toTruffleString("os"),OsLibrary.class);
        LuaTable mathTable = installBuiltinLibrary(toTruffleString("math"), MathLibrary.class);
        mathTable.rawset("pi", Math.PI);
        mathTable.rawset("huge", Double.POSITIVE_INFINITY);
        mathTable.rawset("maxinteger", Long.MAX_VALUE);
        mathTable.rawset("mininteger", Long.MIN_VALUE);
        // Lua 5.3 的兼容库 bit32（LUA_COMPAT_BITLIB）
        installBuiltinLibrary(toTruffleString("bit32"), Bit32Library.class);
        var stringTable = installBuiltinLibrary(toTruffleString("string"),StringLibrary.class);
        installFunctionIntoTable(stringTable, "gmatch", LuaStringGmatchBuiltinNodeGen::create);
        LuaTable stringMetatable = new LuaTable(tableShape);
        stringMetatable.rawset(LuaMetatables.__INDEX, stringTable);
        this.typeMetatables.put(TruffleString.class, stringMetatable);

        LuaTable tableLibrary = installBuiltinLibrary(toTruffleString("table"), TableLibrary.class);
        installFunctionIntoTable(tableLibrary, "pack", LuaTablePackBuiltinNodeGen::create);
        installFunctionIntoTable(tableLibrary, "sort", LuaTableSortBuiltinNodeGen::create);

        installBuiltinLibrary(toTruffleString("debug"), DebugLibrary.class);

        LuaTable utf8Table = installBuiltinLibrary(toTruffleString("utf8"), Utf8Library.class);
        utf8Table.rawset(toTruffleString("charpattern"), toTruffleString(Utf8Library.CHARPATTERN));
        installFunctionIntoTable(utf8Table, "codes", LuaUtf8CodesBuiltinNodeGen::create);

        installBuiltinLibrary(toTruffleString("coroutine"), CoroutineLibrary.class);

        // json.encode：Lua 值 → JSON 文本（数组部分 → JSON 数组，哈希部分 → JSON 对象）
        installBuiltinLibrary(toTruffleString("json"), JsonLibrary.class);

        LuaTable ioLibrary = installBuiltinLibrary(toTruffleString("io"), IoLibrary.class);
        LuaFileHandle stdin = LuaFileHandle.stream(fileShape, env.in(), null, true);
        LuaFileHandle stdout = LuaFileHandle.stream(fileShape, null, env.out(), true);
        LuaFileHandle stderr = LuaFileHandle.stream(fileShape, null, env.err(), true);
        this.defaultInput = stdin;
        this.defaultOutput = stdout;

        LuaTable fileMethods = new LuaTable(tableShape);
        String[] methods = {"close", "flush", "read", "write", "seek", "setvbuf", "lines"};
        for (String method : methods) {
            Object function = ioLibrary.rawget("file_" + method);
            fileMethods.rawset(method, function);
            ioLibrary.rawset("file_" + method, LuaNil.SINGLETON);
        }
        Object fileGc = ioLibrary.rawget("file_gc");
        ioLibrary.rawset("file_gc", LuaNil.SINGLETON);
        LuaTable fileMetatable = new LuaTable(tableShape);
        fileMetatable.rawset(LuaMetatables.__INDEX, fileMethods);
        fileMetatable.rawset("__gc", fileGc);
        fileMetatable.rawset(LuaMetatables.__NAME, toTruffleString("FILE*"));
        stdin.setMetatable(fileMetatable);
        stdout.setMetatable(fileMetatable);
        stderr.setMetatable(fileMetatable);
        this.typeMetatables.put(LuaFileHandle.class, fileMetatable);

        ioLibrary.rawset("stdin", stdin);
        ioLibrary.rawset("stdout", stdout);
        ioLibrary.rawset("stderr", stderr);
    }

    private void setupPackageSystem() {
        LuaTable packageTable = new LuaTable(tableShape);
        globals.rawset("package", packageTable);


        // 1. 基础字段
        LuaTable loaded = new LuaTable(tableShape);
        packageTable.rawset("loaded", loaded);
        packageTable.rawset("preload", new LuaTable(tableShape));
        packageTable.rawset("path", toTruffleString(DEFAULT_PATH)); // "./?.lua;..."
        packageTable.rawset("cpath", toTruffleString(DEFAULT_CPATH)); // "./?.so;..."

        // Register standard libraries in package.loaded so require('string'), require('io'), etc. work
        String[] stdLibs = {"string", "io", "math", "os", "table", "coroutine", "package", "debug", "utf8", "bit32"};
        for (String lib : stdLibs) {
            Object libTable = globals.rawget(toTruffleString(lib));
            if (libTable instanceof LuaTable) {
                loaded.rawset(toTruffleString(lib), libTable);
            }
        }
        // 与标准 Lua 一致：基础库以 "_G" 为名注册到 package.loaded（供错误消息解析限定名）。
        loaded.rawset(toTruffleString("_G"), globals, DynamicObjectLibrary.getUncached());

        // Windows 用 \，其他用 /
        String dirSep = FileSystems.getDefault().getSeparator();
        packageTable.rawset("config", toTruffleString(dirSep + "\n;\n?\n!\n-"));

        // 2. 注册 package 函数
        installFunctionIntoTable(packageTable, "searchpath", SearchPathBuiltinNodeGen::create);
        installFunctionIntoTable(packageTable, "loadlib", LoadLibBuiltinNodeGen::create);

        // 3. 初始化 package.searchers (Lua 5.2+) / package.loaders (Lua 5.1)
        // 这是一个包含函数的表（数组）
        LuaTable searchers = new LuaTable(tableShape);

        // Searcher 1: Preload (检查 package.preload)
        installFunctionIntoArray(searchers, 1, PreloadSearcherNodeGen::create);

        // Searcher 2: Lua Path (使用 package.path 查找文件)
        installFunctionIntoArray(searchers, 2, LuaPathSearcherNodeGen::create);

        packageTable.rawset("searchers", searchers);
    }

    private void installFunctionIntoArray(LuaTable table, int index, Supplier<LuaBuiltinNode> factory) {
        RootNode root = new BuiltinRootNode(language, factory);
        table.arrayRawSet(index, new LuaFunction(this.functionShape,root.getCallTarget()));
    }

    /**
     * 根据入口脚本的 Source 初始化 package.path。
     * 只有在第一次解析【文件型】脚本时调用。
     */
    public void initPackagePath(Source source) {
        // 1. 获取 package 表
        Object pkgObj = globals.rawget("package");
        if (!(pkgObj instanceof LuaTable packageTable)) {
            return; // 防御性编程
        }

        // 2. 检查是否有 -E 选项（禁用环境变量）
        boolean ignoreEnv = hasIgnoreEnvFlag();

        // 3. 环境变量优先：LUA_PATH_5_3 > LUA_PATH > 默认路径
        String luaPath = null;
        if (!ignoreEnv) {
            luaPath = System.getenv("LUA_PATH_5_3");
            if (luaPath == null) {
                luaPath = System.getenv("LUA_PATH");
            }
        }

        if (luaPath != null) {
            // 处理 ;; 替换为默认路径（与 -E 时的默认值一致，即脚本目录锚定版本，
            // 这样官方测试套件 convert 测试中两处默认路径才能对上）
            String computedDefault = computeDefaultPath(source);
            luaPath = luaPath.replace(";;", ";" + computedDefault + ";");
            packageTable.rawset("path", toTruffleString(luaPath));
        } else {
            // 4. 使用默认路径（锚定到入口脚本目录）
            packageTable.rawset("path", toTruffleString(computeDefaultPath(source)));
        }

        applyCPathEnvironmentOverride(ignoreEnv);
    }

    /**
     * 仅应用 LUA_PATH/LUA_CPATH 环境变量覆盖（不锚定脚本目录）。
     * <p>
     * 启动器可能在解析主脚本之前执行内部代码片段（如 -l 触发的 require），
     * 这些片段也需要看到正确的 package.path；此操作是幂等的，
     * 之后的文件型 Source 仍可完成脚本目录锚定。
     */
    public void applyPathEnvironmentOverrides() {
        if (hasIgnoreEnvFlag()) {
            return;
        }
        String luaPath = System.getenv("LUA_PATH_5_3");
        if (luaPath == null) {
            luaPath = System.getenv("LUA_PATH");
        }
        if (luaPath == null) {
            return;
        }
        Object pkgObj = globals.rawget("package");
        if (!(pkgObj instanceof LuaTable packageTable)) {
            return;
        }
        luaPath = luaPath.replace(";;", ";" + DEFAULT_PATH + ";");
        packageTable.rawset("path", toTruffleString(luaPath));
        applyCPathEnvironmentOverride(false);
    }

    private void applyCPathEnvironmentOverride(boolean ignoreEnv) {
        String luaCpath = null;
        if (!ignoreEnv) {
            luaCpath = System.getenv("LUA_CPATH_5_3");
            if (luaCpath == null) {
                luaCpath = System.getenv("LUA_CPATH");
            }
        }
        if (luaCpath != null) {
            Object pkgObj = globals.rawget("package");
            if (pkgObj instanceof LuaTable packageTable) {
                packageTable.rawset("cpath", toTruffleString(luaCpath));
            }
        }
    }

    /**
     * 计算默认的 package.path（包含脚本目录）。
     */
    private String computeDefaultPath(Source source) {
        TruffleFile rootDir;
        if (source.getPath() != null) {
            TruffleFile scriptFile = env.getPublicTruffleFile(source.getPath());
            rootDir = scriptFile.getParent();
        } else {
            rootDir = env.getCurrentWorkingDirectory();
        }
        if (rootDir == null) {
            rootDir = env.getCurrentWorkingDirectory();
        }
        scriptDirectory = rootDir.getAbsoluteFile();

        String rootPath = scriptDirectory.getPath();
        String separator = env.getFileNameSeparator();
        if (!rootPath.endsWith(separator)) {
            rootPath += separator;
        }

        return rootPath + "?" + ".lua;" +
                rootPath + "?" + separator + "init.lua;" +
                DEFAULT_PATH.replace("/", separator);
    }

    /**
     * 检查命令行参数中是否包含 -E 标志（禁用环境变量）。
     * 扫描解释器选项区域（从索引1开始，直到遇到脚本路径）。
     */
    private boolean hasIgnoreEnvFlag() {
        String[] applicationArguments = env.getApplicationArguments();
        int i = 1;
        while (i < applicationArguments.length) {
            String opt = applicationArguments[i];
            if (opt.equals("--")) {
                break; // end of options
            }
            if (!opt.startsWith("-") || opt.equals("-")) {
                break; // reached script path
            }
            if (opt.equals("-E")) {
                return true;
            }
            if (opt.equals("-E") || opt.equals("-i") || opt.equals("-v")) {
                i++;
            } else if (opt.equals("-l") || opt.equals("-e")) {
                i += 2; // option + argument
            } else if (opt.startsWith("-l") || opt.startsWith("-e")) {
                i++; // combined like -lmod
            } else {
                i++; // unknown option
            }
        }
        return false;
    }

    /**
     * 处理 LUA_INIT 环境变量。
     * LUA_INIT_5_3 优先于 LUA_INIT。
     * 如果值以 @ 开头，则加载并执行该文件。
     * 否则，将其作为 Lua 代码块执行。
     */
    public void runLuaInit() {
        if (hasIgnoreEnvFlag()) {
            return;
        }
        String luaInit = System.getenv("LUA_INIT_5_3");
        if (luaInit == null) {
            luaInit = System.getenv("LUA_INIT");
        }
        if (luaInit == null || luaInit.isEmpty()) {
            return;
        }
        try {
            Source initSource;
            if (luaInit.startsWith("@")) {
                String filename = luaInit.substring(1);
                TruffleFile file = env.getPublicTruffleFile(filename);
                initSource = Source.newBuilder(LuaLanguage.ID, file)
                        .name("@" + file.getPath())
                        .build();
            } else {
                initSource = Source.newBuilder(LuaLanguage.ID, luaInit, "LUA_INIT")
                        .build();
            }
            CallTarget callTarget = env.parseInternal(initSource);
            callTarget.call();
        } catch (IOException e) {
            throw LuaException.create("LUA_INIT: " + e.getMessage(), null);
        }
    }

    /**
     * 【新增】创建并缓存 ipairs 迭代器函数的辅助方法。
     */
    private void createIpairsIterator() {
        // 这个过程与 installFunctionIntoTable 类似，但不把它注册到任何 table 中
        RootNode rootNode = new BuiltinRootNode(language, IPairsIteratorBuiltinNodeGen::create);
        this.ipairsIterator = new LuaFunction(functionShape,rootNode.getCallTarget(),null,toTruffleString("iterator"));
    }

    /**
     * 【新增】提供一个公共的 getter
     */
    public LuaFunction getIpairsIterator() {
        return this.ipairsIterator;
    }

    /** 创建并缓存 utf8.codes 的迭代器函数（不注册到任何表）。 */
    private void createUtf8Iterator() {
        RootNode rootNode = new BuiltinRootNode(language, com.zhhz.truffle.lua.builtins.LuaUtf8IteratorBuiltinNodeGen::create);
        this.utf8Iterator = new LuaFunction(functionShape, rootNode.getCallTarget(), null, toTruffleString("utf8_iterator"));
    }

    public LuaFunction getUtf8Iterator() {
        return this.utf8Iterator;
    }

    /** 供 error() 等内建调用的原始 tostring 函数（不受全局替换影响）。 */
    public LuaFunction getTostringFunction() {
        return this.tostringFunction;
    }

    public void installBuiltin(String funcName,Supplier<LuaBuiltinNode> builtinNodeFactory) {

        // 步骤 A: 创建一个 BuiltinRootNode 来包装我们的内置节点
        RootNode rootNode = new BuiltinRootNode(language, builtinNodeFactory);

        TruffleString name = toTruffleString(funcName);

        // 步骤 B: 从 RootNode 创建一个可执行的 CallTarget
        var callTarget = rootNode.getCallTarget();

        // 步骤 C: 创建一个 Lua 用户可以看见的函数对象 (TruffleObject)
        LuaFunction function = new LuaFunction(this.functionShape, callTarget, null, name);

        globals.rawset(name, function, DynamicObjectLibrary.getUncached());
    }

    public void installBuiltin(LuaBuiltinNode funcObject) {

        // 步骤 A: 创建一个 BuiltinRootNode 来包装我们的内置节点
        RootNode rootNode = new BuiltinRootNode(language, funcObject);

        NodeInfo nodeInfo = funcObject.getClass().getAnnotation(NodeInfo.class);

        TruffleString name = toTruffleString(nodeInfo.shortName());

        // 步骤 B: 从 RootNode 创建一个可执行的 CallTarget
        var callTarget = rootNode.getCallTarget();

        // 步骤 C: 创建一个 Lua 用户可以看见的函数对象 (TruffleObject)
        LuaFunction function = new LuaFunction(this.functionShape, callTarget, null, name);

        globals.rawset(name, function, DynamicObjectLibrary.getUncached());
        if ("tostring".equals(nodeInfo.shortName())) {
            this.tostringFunction = function;
        }
    }


    /**
     * 【核心】通用的、基于反射的批量库安装器。
     *
     * @param name 库名
     * @param libraryClass 包含带 @LuaBuiltin 注解的静态方法的类
     */
    @TruffleBoundary // 反射是慢路径操作
    private LuaTable installBuiltinLibrary(TruffleString name, Class<?> libraryClass) {
        LuaTable targetTable = new LuaTable(tableShape);

        // 1. 遍历指定类中的所有方法
        for (Method method : libraryClass.getMethods()) {
            // 2. 检查方法是否是静态的，并且带有我们的自定义注解
            if (Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(LuaBuiltin.class)) {
                // 3. 获取注解和函数名
                LuaBuiltin builtinAnnotation = method.getAnnotation(LuaBuiltin.class);
                String luaFunctionName = builtinAnnotation.name().isEmpty() ? method.getName() : builtinAnnotation.name();

                // 4. 为这个方法创建一个 GenericBuiltinNode 实例
                //    我们通过 Supplier 传递 Method 对象
                Supplier<LuaBuiltinNode> factory = () -> GenericBuiltinNodeGen.create(method);

                // 5. 调用我们已有的安装逻辑
                installFunctionIntoTable(targetTable, luaFunctionName, factory);

            }
        }
        globals.rawset(name, targetTable,DynamicObjectLibrary.getUncached());
        return targetTable;
    }

    /**
     * 【核心辅助方法】将一个内置函数安装到【任意一个指定】的表中。
     * @param table 目标表 (可以是全局表，也可以是 os 表等)
     * @param name 函数名 (键)
     * @param builtinNodeFactory 创建内置节点的工厂
     */
    public void installFunctionIntoTable(LuaTable table, String name, Supplier<LuaBuiltinNode> builtinNodeFactory) {
        // 这个逻辑与你之前安装 print 的逻辑完全一样：
        // 1. 创建 RootNode
        RootNode rootNode = new BuiltinRootNode(language, builtinNodeFactory);
        // 2. 创建 CallTarget
        var callTarget = rootNode.getCallTarget();

        var funcName = TruffleString.fromJavaStringUncached(name,LuaLanguage.STRING_ENCODING);
        // 3. 创建 LuaFunction 运行时对象
        LuaFunction function = new LuaFunction(this.functionShape, callTarget, null, funcName);
        // 4. 写入目标表
        table.rawset(funcName, function , DynamicObjectLibrary.getUncached());
    }

    /**
     * 获取一个值的有效元表。
     * 1. 首先检查对象自身的元表。
     * 2. 如果没有，则检查该对象类型的默认元表。
     * @param value lua值
     * @return 返回值的元表
     */
    @TruffleBoundary
    public LuaTable getEffectiveMetatable(Object value) {
        if (value instanceof LuaTable) {
            LuaTable mt = ((LuaTable) value).getMetatable();
            if (mt != null) {
                return mt; // 优先返回对象自身的元表
            }
        }

        // 如果没有自身元表，查找类型的默认元表
        if (value != null) {
            return typeMetatables.get(value.getClass());
        }

        return null;
    }

    /**
     * 为基本类型（nil/boolean/number/string 等）设置共享的类型元表。
     * 对应 Lua 的 {@code debug.setmetatable} 对非 table 值的行为。
     */
    public void setTypeMetatable(Object value, LuaTable metatable) {
        if (value instanceof Long || value instanceof Double) {
            typeMetatables.put(Long.class, metatable);
            typeMetatables.put(Double.class, metatable);
        } else if (value != null) {
            typeMetatables.put(value.getClass(), metatable);
        }
    }

    public CallTarget parse(Source source) {
        return env.parsePublic(source);
    }

    /**
     * 返回一个对象，该对象包含跨所有使用的语言导出的绑定。到
     * 从该对象读取或写入时，可以使用｛@link TruffleObject互操作｝API。
     */
    public TruffleObject getPolyglotBindings() {
        return (TruffleObject) env.getPolyglotBindings();
    }

    private static final ContextReference<LuaContext> REFERENCE = ContextReference.create(LuaLanguage.class);

    public static LuaContext get(Node node) {
        return REFERENCE.get(node);
    }

    public LuaTable getGlobals() {
        return this.globals;
    }

    // =================================================================================
    // 调用点追踪（用于参数错误消息里复刻 luaL_argerror / getinfo "n" 的命名行为）
    // =================================================================================
    private com.oracle.truffle.api.nodes.Node pendingCallSite;
    private com.oracle.truffle.api.nodes.Node activeCallSite;

    public com.oracle.truffle.api.nodes.Node getPendingCallSite() {
        return pendingCallSite;
    }

    public void setPendingCallSite(com.oracle.truffle.api.nodes.Node node) {
        this.pendingCallSite = node;
    }

    /** 取出并清空“待消费”的调用点（内建函数进入时调用）。 */
    public com.oracle.truffle.api.nodes.Node takePendingCallSite() {
        com.oracle.truffle.api.nodes.Node node = pendingCallSite;
        pendingCallSite = null;
        return node;
    }

    public com.oracle.truffle.api.nodes.Node getActiveCallSite() {
        return activeCallSite;
    }

    public void setActiveCallSite(com.oracle.truffle.api.nodes.Node node) {
        this.activeCallSite = node;
    }

    // =================================================================================
    // 调用深度限制 / 栈溢出（复刻标准 Lua 的 "stack overflow" 与 "C stack overflow"）
    // =================================================================================

    /** 标准 Lua 的 LUAI_MAXCCALLS（C 调用嵌套上限）。 */
    private static final int LUAI_MAXCCALLS = 200;
    /**
     * Lua 函数嵌套上限。标准 Lua 通过 Lua 栈（约 1e6 槽）限制，纯 Lua 递归报
     * "stack overflow"；本引擎使用 Java 栈，故取一个远低于 Java 栈容量的值。
     */
    private static final int LUA_MAX_DEPTH = Integer.getInteger("lua.maxdepth", 250);
    /** 错误处理期间允许的额外嵌套余量（对应标准 Lua 的 LUAI_MAXCCALLS>>3）。 */
    private static final int LUA_ERROR_HEADROOM = 25;

    private int cCallDepth;
    private int luaCallDepth;
    /** 是否正处于一次栈溢出错误处理过程中（用于生成 "error in error handling"）。 */
    private boolean stackOverflow;
    /** 当前生效的 xpcall 消息处理函数（在抛出点调用，复刻 luaG_errormsg 行为）。 */
    private Object errorHandler;

    private static final com.oracle.truffle.api.interop.InteropLibrary UNCACHED_INTEROP =
            com.oracle.truffle.api.interop.InteropLibrary.getFactory().getUncached();

    public Object setErrorHandler(Object handler) {
        Object previous = errorHandler;
        errorHandler = handler;
        return previous;
    }

    public void restoreErrorHandler(Object handler) {
        errorHandler = handler;
    }

    /** 进入一个内建（C）函数调用；超过 LUAI_MAXCCALLS 抛 "C stack overflow"。 */
    public void enterBuiltin(com.oracle.truffle.api.nodes.Node location) {
        if (++cCallDepth >= LUAI_MAXCCALLS) {
            cCallDepth--;
            throw stackError("C stack overflow", location);
        }
        // 活跃内建 RootNode 栈（每线程，外层在前），debug 库据此跳过自身的 C 帧。
        debugStacks().builtinRoots.add(
                location instanceof com.oracle.truffle.api.nodes.RootNode root ? root : null);
    }

    /** 当前正在执行的内建（C 函数）RootNode；debug 库据此跳过自身的栈帧。 */
    public com.oracle.truffle.api.nodes.RootNode getCurrentBuiltinRoot() {
        java.util.List<com.oracle.truffle.api.nodes.RootNode> roots = debugStacks().builtinRoots;
        int last = roots.size() - 1;
        return last >= 0 ? roots.get(last) : null;
    }

    public void exitBuiltin() {
        java.util.List<com.oracle.truffle.api.nodes.RootNode> roots = debugStacks().builtinRoots;
        if (!roots.isEmpty()) {
            roots.remove(roots.size() - 1);
        }
        if (--cCallDepth <= 0) {
            cCallDepth = 0;
            // 栈已完全展开：清除溢出标记，避免后续调用误判为“错误处理中”。
            // 标准 Lua 通过递减 nCcalls 隐式恢复，这里显式复位。
            if (luaCallDepth <= 0) {
                stackOverflow = false;
            }
        }
    }

    /** 进入一个 Lua 函数调用；超过 LUA_MAX_DEPTH 抛 "stack overflow"。 */
    public void enterLuaCall(com.oracle.truffle.api.nodes.Node location) {
        int depth = ++luaCallDepth;
        // 硬上限：与标准 Lua 一致，在错误处理期间再次溢出时报 "error in error handling"。
        if (depth >= LUA_MAX_DEPTH + LUA_ERROR_HEADROOM) {
            luaCallDepth--;
            stackOverflow = true;
            throw LuaException.create("error in error handling", location);
        }
        // 软上限：首次达到上限时报 "stack overflow"（消息处理函数尚可在余量内运行）。
        if (depth >= LUA_MAX_DEPTH && !stackOverflow) {
            luaCallDepth--;
            throw stackError("stack overflow", location);
        }
    }

    public void exitLuaCall() {
        if (--luaCallDepth <= 0) {
            luaCallDepth = 0;
            if (cCallDepth <= 0) {
                stackOverflow = false;
            }
        }
    }

    // =================================================================================
    // 可让出性（复刻标准 Lua 的 L->nny：non-yieldable 层数）
    // =================================================================================
    // 协程体在 nny==0 的环境中运行，可直接 yield；当 Lua 代码是经内建（C）函数回调
    // 进入时（例如 table.sort 的比较器、元方法），nny>0，此时 yield 会报
    // "attempt to yield across a C-call boundary"。计数按线程隔离（每个协程一条线程）。
    private final ThreadLocal<int[]> nonYieldable = ThreadLocal.withInitial(() -> new int[1]);

    public void enterNonYieldable() {
        nonYieldable.get()[0]++;
    }

    public void exitNonYieldable() {
        nonYieldable.get()[0]--;
    }

    public boolean isNonYieldable() {
        return nonYieldable.get()[0] > 0;
    }

    private RuntimeException stackError(String message, com.oracle.truffle.api.nodes.Node location) {
        if (stackOverflow) {
            return LuaException.create("error in error handling", location);
        }
        stackOverflow = true;
        LuaException error = (LuaException) LuaException.runtimeError(message, location);
        Object handler = errorHandler;
        if (handler != null) {
            // 标准 Lua：消息处理函数在展开栈之前、栈仍然很深时被调用，
            // 因此 debug.traceback 能看到完整的递归调用链。
            try {
                Object messageObject = error.getErrorObject();
                if (messageObject instanceof String string) {
                    messageObject = toTruffleString(string);
                }
                enterNonYieldable();
                Object handled;
                try {
                    handled = UNCACHED_INTEROP.execute(handler, messageObject);
                } finally {
                    exitNonYieldable();
                }
                throw new LuaHandledError(handled);
            } catch (LuaHandledError handled) {
                throw handled;
            } catch (Exception handlerError) {
                stackOverflow = false;
                return LuaException.create("error in error handling", location);
            }
        }
        return error;
    }

    /** 由 pcall/xpcall 在捕获错误后调用，重置栈溢出状态。 */
    public void clearStackOverflow() {
        stackOverflow = false;
    }
}
