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
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.builtins.*;
import org.graalvm.polyglot.Context;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    // 【核心】全局变量表
    private final LuaTable globals;
    private final Shape tableShape;
    private final Shape functionShape;
    private final Shape booleanShape;

    private LuaFunction ipairsIterator;

    // 【1. 新增】一个用于存储【类型默认元表】的 Map
    private final Map<Class<?>, LuaTable> typeMetatables = new HashMap<>();

    public LuaContext(LuaLanguage language, Env env) {
        this.env = env;

        this.output = new PrintWriter(env.out(), true);
        this.language = language;

        // 1. 【关键】在上下文创建时，实例化全局表
        //    我们需要一个方法来创建 table 的初始 Shape
        this.tableShape = Shape.newBuilder().layout(LuaTable.class, MethodHandles.lookup()).build();
        this.functionShape = Shape.newBuilder().layout(LuaFunction.class, MethodHandles.lookup()).build();
        this.booleanShape = Shape.newBuilder().layout(LuaBoolean.class, MethodHandles.lookup()).build();

        this.globals = new LuaTable(tableShape);
        // 2. 将全局表自身放入全局表中，键为 "_G"
        //    这是标准的 Lua 行为
        this.globals.rawset(toTruffleString("_G"), this.globals, DynamicObjectLibrary.getUncached());
        //初始化LuaBoolean
        LuaBoolean.get(this,true);
        LuaBoolean.get(this,false);
        //安装核心库
        installBuiltins();

    }

    // --- 提供 Getter 以便外界可以访问这些 Shape ---

    public Shape getTableShape() { return tableShape; }
    public Shape getFunctionShape() { return functionShape; }
    public Shape getBooleanShape() { return booleanShape; }


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

    @TruffleBoundary
    public static TruffleString toTruffleString(String s){
        return TruffleString.fromJavaStringUncached(s,LuaLanguage.STRING_ENCODING);
    }


    private void installBuiltins() {


        // 创建一个 LoadBuiltinNode 的工厂
        Supplier<LuaBuiltinNode> loadFactory = LoadBuiltinNodeGen::create;
        // 将它同时注册为 load 和 loadstring
        installBuiltin("load", loadFactory);
        installBuiltin("loadstring", loadFactory);

        installBuiltin("print",LuaPrintBuiltinNodeGen::create);
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
        var stringTable = installBuiltinLibrary(toTruffleString("string"),StringLibrary.class);
        LuaTable stringMetatable = new LuaTable(tableShape);
        stringMetatable.rawset(LuaMetatables.__INDEX, stringTable);
        this.typeMetatables.put(TruffleString.class, stringMetatable);
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
