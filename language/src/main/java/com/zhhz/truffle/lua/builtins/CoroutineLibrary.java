package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaThread;
import com.oracle.truffle.api.object.Shape;

/**
 * Lua 5.3 {@code coroutine} 标准库的实现。
 *
 * <p>协程通过 Java 线程 + {@link com.oracle.truffle.api.TruffleContext} 实现。
 * 每个协程运行在独立线程中，通过 {@link java.util.concurrent.Exchanger} 与
 * 调用者交换 yield/resume 值。
 *
 * <p>支持的函数：create, resume, yield, status, wrap, running, isyieldable。
 */
public final class CoroutineLibrary {

    private CoroutineLibrary() {
    }

    /**
     * coroutine.create(f): 创建一个以 f 为函数的新协程，返回 thread 对象。
     */
    @LuaBuiltin(name = "create")
    @TruffleBoundary
    public static LuaThread create(Object[] arguments) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaFunction func)) {
            throw LuaException.create("bad argument #1 to 'create' (function expected)", null);
        }
        LuaContext context = LuaContext.get(null);
        Env env = context.getEnv();
        TruffleString name = func.getName();
        LuaThread thread = new LuaThread(context.getThreadShape(), func.getCallTarget(), func.getCallParent(),
                name, func, env, false);
        context.registerCoroutine(thread);
        return thread;
    }

    /**
     * coroutine.resume(co, ...): 恢复协程，传递参数。返回 true + yield/return 值，
     * 或 false + 错误消息。
     */
    @LuaBuiltin(name = "resume")
    @TruffleBoundary
    public static Object resume(Object[] arguments) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaThread thread)) {
            throw LuaException.create("bad argument #1 to 'resume' (thread expected)", null);
        }
        LuaContext context = LuaContext.get(null);
        // 标准 Lua：resume 在当前线程上执行，切换 currentCoroutine
        LuaThread prev = context.getCurrentCoroutine();
        Object[] args = new Object[arguments.length - 1];
        System.arraycopy(arguments, 1, args, 0, args.length);
        context.clearStackOverflow();
        context.setCurrentCoroutine(thread);
        try {
            Object[] result = thread.resume(args);
            // 恢复调用者的 currentCoroutine
            context.setCurrentCoroutine(prev);
            // 标准 Lua：resume 是受保护调用，返回后 C 栈状态恢复，清除栈溢出标记
            context.clearStackOverflow();
            return new LuaMultiValue(result);
        } catch (RuntimeException e) {
            context.setCurrentCoroutine(prev);
            context.clearStackOverflow();
            throw e;
        }
    }

    /**
     * coroutine.yield(...): 挂起当前协程，传递值给 resume 的调用者。
     * 返回下次 resume 传来的参数。
     */
    @LuaBuiltin(name = "yield")
    @TruffleBoundary
    public static Object yield(Object[] arguments) {
        LuaContext context = LuaContext.get(null);
        if (context.isNonYieldable()) {
            throw LuaException.create("attempt to yield across a C-call boundary", null);
        }
        LuaThread current = context.getCurrentCoroutine();
        if (current == null || current.isMain()) {
            throw LuaException.create("attempt to yield from outside a coroutine", null);
        }
        Object[] result = current.yield(arguments);
        return new LuaMultiValue(result);
    }

    /**
     * coroutine.status(co): 返回协程状态字符串。
     */
    @LuaBuiltin(name = "status")
    @TruffleBoundary
    public static TruffleString status(Object[] arguments) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaThread thread)) {
            throw LuaException.create("bad argument #1 to 'status' (thread expected)", null);
        }
        LuaContext context = LuaContext.get(null);
        LuaThread current = context.getCurrentCoroutine();
        // 标准 Lua：当前正在执行的协程状态为 "running"，
        // 被当前协程 resume 的协程状态为 "normal"
        if (current == thread) {
            return LuaContext.toTruffleString("running");
        }
        if (thread.getState() == LuaThread.State.NORMAL) {
            return LuaContext.toTruffleString("normal");
        }
        return LuaContext.toTruffleString(thread.statusString());
    }

    /**
     * coroutine.wrap(f): 创建一个以 f 为函数的新协程，返回一个恢复协程的函数。
     * 与 create 不同，wrap 返回的函数不返回成功标志，出错时直接抛出错误。
     */
    @LuaBuiltin(name = "wrap")
    @TruffleBoundary
    public static LuaFunction wrap(Object[] arguments) {
        final LuaThread thread = create(arguments);
        LuaContext context = LuaContext.get(null);
        // wrap 返回的函数：每次调用 = resume(thread, ...)
        // 我们通过创建一个新的内建函数来实现
        return createResumeWrapper(context, thread);
    }

    /**
     * coroutine.running(): 返回当前正在运行的协程和是否是主线程。
     */
    @LuaBuiltin(name = "running")
    @TruffleBoundary
    public static Object running(@SuppressWarnings("unused") Object[] arguments) {
        LuaContext context = LuaContext.get(null);
        LuaThread current = context.getCurrentCoroutine();
        if (current == null) {
            // 理论上不应发生：主线程的 LuaThread 在上下文初始化时已设置
            return new LuaMultiValue(new Object[]{LuaNil.SINGLETON, Boolean.TRUE});
        }
        return new LuaMultiValue(new Object[]{current, current.isMain()});
    }

    /**
     * coroutine.isyieldable(): 返回当前是否可以 yield。
     * 在主线程中返回 false，在协程中返回 true。
     */
    @LuaBuiltin(name = "isyieldable")
    @TruffleBoundary
    public static boolean isyieldable(@SuppressWarnings("unused") Object[] arguments) {
        LuaContext context = LuaContext.get(null);
        LuaThread current = context.getCurrentCoroutine();
        // 主线程不可 yield；经内建（C）函数回调进入（nny>0，例如 gsub 替换函数、
        // table.sort 比较器）时同样不可（参考实现 lua_isyieldable 检查 L->nny）。
        return current != null && !current.isMain() && !context.isNonYieldable();
    }

    /**
     * 为 wrap 创建一个 resume 包装函数。
     * wrap 的函数调用 resume 但不返回成功标志，出错时直接抛出。
     */
    @TruffleBoundary
    private static LuaFunction createResumeWrapper(LuaContext context, LuaThread thread) {
        // 包装逻辑必须放入正规 RootNode 并经 Truffle.getRuntime().createCallTarget 创建：
        // LuaFunction 的调用路径使用 DirectCallNode/IndirectCallNode，HotSpot Truffle
        // 运行时要求 CallTarget 为 OptimimizedCallTarget，裸的匿名 CallTarget 会触发
        // ClassCastException（官方测试套件 literals.lua 的 coroutine.wrap 泛型 for 即触发）。
        com.oracle.truffle.api.nodes.RootNode rootNode = new com.oracle.truffle.api.nodes.RootNode(context.getLanguage()) {
            @Override
            @TruffleBoundary
            public Object execute(com.oracle.truffle.api.frame.VirtualFrame frame) {
                // frame.getArguments()[0] 为 LuaFunction 调用约定的父 Frame 占位
                Object[] frameArguments = frame.getArguments();
                Object[] resumeArgs = new Object[Math.max(frameArguments.length - 1, 0)];
                System.arraycopy(frameArguments, 1, resumeArgs, 0, resumeArgs.length);
                LuaThread prev = context.getCurrentCoroutine();
                context.setCurrentCoroutine(thread);
                Object[] result;
                try {
                    result = thread.resume(resumeArgs);
                } finally {
                    context.setCurrentCoroutine(prev);
                    context.clearStackOverflow();
                }
                // result[0] = 成功标志
                if (result.length > 0 && result[0] == Boolean.FALSE) {
                    // 出错：抛出错误
                    Object errObj = result.length > 1 ? result[1] : LuaNil.SINGLETON;
                    throw LuaException.create(errObj, null);
                }
                // 去掉成功标志，返回其余值
                Object[] values = new Object[result.length - 1];
                System.arraycopy(result, 1, values, 0, values.length);
                return new LuaMultiValue(values);
            }

            @Override
            public String getName() {
                return "coroutine_wrapper";
            }
        };
        com.oracle.truffle.api.CallTarget wrapperTarget = rootNode.getCallTarget();
        return new LuaFunction(context.getFunctionShape(), wrapperTarget, null,
                LuaContext.toTruffleString("coroutine_wrapper"));
    }
}
