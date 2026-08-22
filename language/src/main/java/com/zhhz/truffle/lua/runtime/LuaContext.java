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
    private LuaFileHandle defaultInput;
    private LuaFileHandle defaultOutput;

    private LuaFunction ipairsIterator;

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

        this.output = new PrintWriter(env.out(), true);
        this.error = new PrintWriter(env.err(), true);
        this.language = language;

        // 1. 【关键】在上下文创建时，实例化全局表
        //    我们需要一个方法来创建 table 的初始 Shape
        this.tableShape = Shape.newBuilder().layout(LuaTable.class, MethodHandles.lookup()).build();
        this.functionShape = Shape.newBuilder().layout(LuaFunction.class, MethodHandles.lookup()).build();
        this.booleanShape = Shape.newBuilder().layout(LuaBoolean.class, MethodHandles.lookup()).build();
        this.fileShape = Shape.newBuilder().layout(LuaFileHandle.class, MethodHandles.lookup()).build();

        this.globals = new LuaTable(tableShape);
        // 2. 将全局表自身放入全局表中，键为 "_G"
        //    这是标准的 Lua 行为
        this.globals.rawset(toTruffleString("_G"), this.globals, DynamicObjectLibrary.getUncached());
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

    }

    // --- 提供 Getter 以便外界可以访问这些 Shape ---

    public Shape getTableShape() { return tableShape; }
    public Shape getFunctionShape() { return functionShape; }
    public Shape getBooleanShape() { return booleanShape; }
    public Shape getFileShape() { return fileShape; }
    public LuaLanguage getLanguage() { return language; }

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
        installBuiltin("error",LuaErrorBuiltinNodeGen::create);
        installBuiltin("next",LuaNextBuiltinNodeGen::create);
        installBuiltin("pairs",LuaPairsBuiltinNodeGen::create);
        createIpairsIterator();
        installBuiltin("ipairs",LuaIpairsBuiltinNodeGen::create);

        installBuiltin("setmetatable",LuaSetMetatableBuiltinNodeGen::create);
        installBuiltin("getmetatable", LuaGetMetatableBuiltinNodeGen::create);

        installBuiltin(LuaPcallBuiltinNodeGen.create());
        installBuiltin(LuaXpcallBuiltinNodeGen.create());
        installBuiltin(LuaAssertBuiltinNodeGen.create());
        installBuiltin(LuaCollectGarbageBuiltinNodeGen.create());
        installBuiltin(LuaRawGetBuiltinNodeGen.create());
        installBuiltin(LuaRawSetBuiltinNodeGen.create());
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
        var stringTable = installBuiltinLibrary(toTruffleString("string"),StringLibrary.class);
        installFunctionIntoTable(stringTable, "gmatch", LuaStringGmatchBuiltinNodeGen::create);
        LuaTable stringMetatable = new LuaTable(tableShape);
        stringMetatable.rawset(LuaMetatables.__INDEX, stringTable);
        this.typeMetatables.put(TruffleString.class, stringMetatable);

        LuaTable tableLibrary = installBuiltinLibrary(toTruffleString("table"), TableLibrary.class);
        installFunctionIntoTable(tableLibrary, "pack", LuaTablePackBuiltinNodeGen::create);
        installFunctionIntoTable(tableLibrary, "sort", LuaTableSortBuiltinNodeGen::create);

        installBuiltinLibrary(toTruffleString("debug"), DebugLibrary.class);

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
        LuaTable fileMetatable = new LuaTable(tableShape);
        fileMetatable.rawset(LuaMetatables.__INDEX, fileMethods);
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
        String[] stdLibs = {"string", "io", "math", "os", "table", "coroutine", "package", "debug"};
        for (String lib : stdLibs) {
            Object libTable = globals.rawget(toTruffleString(lib));
            if (libTable instanceof LuaTable) {
                loaded.rawset(toTruffleString(lib), libTable);
            }
        }

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
}
