package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleContext;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import java.util.concurrent.Exchanger;

/**
 * Lua 协程（coroutine）的运行时表示。
 *
 * <p>实现方式：每个协程使用一条 Java 线程（通过 {@link Env#createThread(Runnable)} 创建，
 * 该线程注册了正确的 Truffle 上下文），与调用者线程通过 {@link Exchanger} 交换值。
 *
 * <p>状态机（与标准 Lua 一致）：
 * <ul>
 *   <li>{@code "suspended"}：已创建但未开始，或已 yield 等待 resume。</li>
 *   <li>{@code "running"}：正在本线程上执行（仅对当前线程可见）。</li>
 *   <li>{@code "normal"}：正在其他线程上执行（被 resume 后、active 期间）。</li>
 *   <li>{@code "dead"}：函数已返回或出错。</li>
 * </ul>
 */
@ExportLibrary(InteropLibrary.class)
public final class LuaThread extends LuaValue {

    /** 协程状态 */
    public enum State { SUSPENDED, RUNNING, NORMAL, DEAD }

    private volatile State state = State.SUSPENDED;

    /** 协程函数的 CallTarget */
    private final CallTarget callTarget;
    /** 传递给协程函数的 parentFrame（第一个参数） */
    private final Object parentFrame;
    /** 协程的名称（用于调试） */
    private final TruffleString name;

    /** 线程间值交换器 */
    private final Exchanger<Object[]> exchanger = new Exchanger<>();

    /** 底层线程（惰性创建） */
    private volatile Thread thread;
    /** 创建协程的 Env，用于 createThread + TruffleContext */
    private final Env env;
    /** 内部 context（线程进入用） */
    private final TruffleContext truffleContext;
    /** 协程函数（用于 wrap 后调用） */
    private final LuaFunction function;
    /** 标记是否是主线程 */
    private final boolean main;

    /** 上下文关闭时置位：唤醒挂起的协程线程并让其直接退出，不再交换结果。 */
    private volatile boolean disposed;

    // =================================================================================
    // 每线程调试钩子状态（对应标准 Lua 的 L->hook / L->hookmask / L->hookcount /
    // L->oldpc / L->allowhook）。协程各有独立钩子；主线程使用 mainThread 实例。
    // =================================================================================
    private Object hookFunction;
    private String hookMask = "";
    private int hookCount;
    private int lastHookLine = -1;
    private boolean inHook;
    private int instructionCount;

    /**
     * 下一个进入的 Lua 函数是否由尾调用产生。AST 的尾调用通过异常在固定 Java 栈深度下
     * 重新派发；重试循环在调用目标前置位，目标根节点在入口消费它，从而触发
     * "tail call" 而不是 "call"（参考实现依据前一条指令是否为 OP_TAILCALL 判断）。
     */
    private boolean pendingTailCall;

    /**
     * 每线程的调试簿记：VM 逻辑帧、活跃 VM 容器与活跃内建 RootNode。
     *
     * <p>这些栈描述的是<b>当前执行线程</b>的调用栈（Truffle 帧与 VM 栈都按线程区分）。
     * 协程在独立 Java 线程上交错执行（resume 阻塞等待 yield），若存放在
     * {@link LuaContext} 上会互相串栈：一个协程挂起时其条目仍留在全局列表里，
     * 另一个线程的增删与层级映射都会错位。
     */
    public static final class DebugStacks {
        final java.util.List<com.zhhz.truffle.lua.vm.VmFrame> vmFrames = new java.util.ArrayList<>();
        final java.util.List<com.zhhz.truffle.lua.vm.LuaVm> vmContainers = new java.util.ArrayList<>();
        final java.util.List<com.oracle.truffle.api.nodes.RootNode> builtinRoots = new java.util.ArrayList<>();
    }

    private final DebugStacks debugStacks = new DebugStacks();

    public DebugStacks debugStacks() {
        return debugStacks;
    }

    public Object getHookFunction() { return hookFunction; }
    public void setHookFunction(Object function) { this.hookFunction = function; }
    public String getHookMask() { return hookMask; }
    public void setHookMask(String mask) { this.hookMask = mask; }
    public int getHookCount() { return hookCount; }
    public void setHookCount(int count) { this.hookCount = count; }
    public boolean isHookMask(char c) { return hookMask.indexOf(c) >= 0; }
    public int getLastHookLine() { return lastHookLine; }
    public void setLastHookLine(int line) { this.lastHookLine = line; }
    public boolean isInHook() { return inHook; }
    public void setInHook(boolean value) { this.inHook = value; }
    public int getInstructionCount() { return instructionCount; }
    public void setInstructionCount(int count) { this.instructionCount = count; }

    /** 该线程是否设置了任意钩子（函数非空）。 */
    public boolean hasHook() { return hookFunction != null; }

    /** 标记/取消“下一次函数进入来自尾调用”。 */
    public void setPendingTailCall(boolean value) { this.pendingTailCall = value; }

    /** 消费“下一次函数进入来自尾调用”标记（读取后清零）。 */
    public boolean takePendingTailCall() {
        boolean value = pendingTailCall;
        pendingTailCall = false;
        return value;
    }

    /** 清空该线程的钩子状态。 */
    public void clearHook() {
        hookFunction = null;
        hookMask = "";
        hookCount = 0;
        lastHookLine = -1;
        inHook = false;
        instructionCount = 0;
    }

    public LuaThread(Shape shape, CallTarget callTarget, Object parentFrame, TruffleString name,
                     LuaFunction function, Env env, boolean main) {
        super(shape);
        this.callTarget = callTarget;
        this.parentFrame = parentFrame;
        this.name = name;
        this.function = function;
        this.env = env;
        this.truffleContext = env != null ? env.getContext() : null;
        this.main = main;
        // 主线程始终处于 RUNNING 状态
        if (main) {
            this.state = State.RUNNING;
        }
    }

    @Override
    public String getTypeName() {
        return "thread";
    }

    @CompilerDirectives.TruffleBoundary
    @Override
    public String toString() {
        return "thread: 0x" + Integer.toHexString(System.identityHashCode(this));
    }

    @ExportMessage
    public boolean hasLanguageId() {
        return true;
    }

    @ExportMessage
    public String getLanguageId() {
        return com.zhhz.truffle.lua.LuaLanguage.ID;
    }

    public State getState() { return state; }
    public boolean isMain() { return main; }
    public LuaFunction getFunction() { return function; }

    /**
     * resume 协程：将参数发给协程线程，等待协程 yield 或结束。
     * 调用者线程阻塞直到交换完成。
     *
     * @param args 传给协程的参数
     * @return 协程返回的值数组：[0]=是否成功（Boolean）, [1..n]=返���值或错误对象
     */
    @CompilerDirectives.TruffleBoundary
    public Object[] resume(Object[] args) {
        if (state == State.DEAD) {
            return new Object[]{Boolean.FALSE, LuaContext.toTruffleString("cannot resume dead coroutine")};
        }
        if (state == State.RUNNING || state == State.NORMAL) {
            return new Object[]{Boolean.FALSE, LuaContext.toTruffleString("cannot resume non-suspended coroutine")};
        }

        if (thread == null) {
            startThread();
        }

        // 标记状态
        state = State.NORMAL;

        // 把参数发给协程线程，等待协程 yield 或结束
        Object[] result;
        try {
            exchanger.exchange(args);
            result = exchanger.exchange(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            state = State.DEAD;
            if (disposed) {
                throw new AbortExecution();
            }
            return new Object[]{Boolean.FALSE, LuaContext.toTruffleString("coroutine interrupted")};
        }

        // 解析结果
        // result[0] = "yield" | "return" | "error"
        // result[1..] = values
        String mode = (String) result[0];
        if ("yield".equals(mode)) {
            state = State.SUSPENDED;
            Object[] values = new Object[result.length - 1];
            System.arraycopy(result, 1, values, 0, values.length);
            Object[] ret = new Object[values.length + 1];
            ret[0] = Boolean.TRUE;
            System.arraycopy(values, 0, ret, 1, values.length);
            return ret;
        } else if ("return".equals(mode)) {
            state = State.DEAD;
            Object[] values = new Object[result.length - 1];
            System.arraycopy(result, 1, values, 0, values.length);
            Object[] ret = new Object[values.length + 1];
            ret[0] = Boolean.TRUE;
            System.arraycopy(values, 0, ret, 1, values.length);
            return ret;
        } else {
            // error
            state = State.DEAD;
            Object[] ret = new Object[2];
            ret[0] = Boolean.FALSE;
            ret[1] = result.length > 1 ? result[1] : LuaContext.toTruffleString("error in coroutine");
            return ret;
        }
    }

    /**
     * yield 协程：将值发给调用者线程，阻塞等待下次 resume。
     * 由协程线程在协程函数内部调用。
     *
     * @param values 要 yield 的值
     * @return 下次 resume 传来的参数
     */
    @CompilerDirectives.TruffleBoundary
    public Object[] yield(Object[] values) {
        // 把 yield 值发给调用者
        Object[] yieldResult = new Object[values.length + 1];
        yieldResult[0] = "yield";
        System.arraycopy(values, 0, yieldResult, 1, values.length);
        try {
            exchanger.exchange(yieldResult);
            // 等待 resume 传来的参数
            Object[] resumeArgs = exchanger.exchange(null);
            return resumeArgs;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (disposed) {
                // 上下文正在关闭：直接展开调用栈，避免协程继续执行后再次阻塞。
                throw new AbortExecution();
            }
            return new Object[0];
        }
    }

    /**
     * 启动协程线程。线程进入 Truffle 上下文后调用协程函数。
     */
    @CompilerDirectives.TruffleBoundary
    private void startThread() {
        if (env == null) {
            throw new RuntimeException("cannot create coroutine thread: env is null");
        }
        Runnable body = () -> {
            Object prev = null;
            if (truffleContext != null) {
                prev = truffleContext.enter(null);
            }
            // currentCoroutine 是 ThreadLocal：协程体在【本线程】上执行，
            // coroutine.yield/status/isyieldable 需要在本线程上取到当前协程
            LuaThread previousCoroutine = null;
            try {
                LuaContext context = LuaContext.get(null);
                previousCoroutine = context.getCurrentCoroutine();
                context.setCurrentCoroutine(this);
            } catch (Exception ignored) {
                // 上下文不可用时退化为无 ThreadLocal 状态（yield 会明确报错）
            }
            try {
                // 等待第一次 resume 的参数
                Object[] firstArgs = exchanger.exchange(null);
                if (disposed) {
                    return; // 创建后未运行即关闭
                }
                // 构建调用参数: 被调用的 LuaFunction + user args
                Object[] frameArgs = new Object[firstArgs.length + 1];
                frameArgs[0] = function;
                System.arraycopy(firstArgs, 0, frameArgs, 1, firstArgs.length);
                    Object result;
                    try {
                        result = callTarget.call(frameArgs);
                        // 发送返回值
                        Object[] retValues;
                        if (result instanceof LuaMultiValue mv) {
                            Object[] arr = mv.values();
                            retValues = new Object[arr.length + 1];
                            retValues[0] = "return";
                            System.arraycopy(arr, 0, retValues, 1, arr.length);
                        } else {
                            retValues = new Object[]{"return",
                                    result == null ? LuaNil.SINGLETON : result};
                        }
                    if (disposed) {
                        return; // 上下文已关闭：不再与（已停止等待的）调用者交换结果
                    }
                    exchanger.exchange(retValues);
                } catch (Throwable e) {
                    if (disposed) {
                        return; // 关闭时的取消/中断：静默退出
                    }
                    Object errObj;
                    if (e instanceof com.zhhz.truffle.lua.LuaException le) {
                        errObj = le.getErrorObject();
                        if (errObj == null) errObj = LuaContext.toTruffleString(le.getMessage());
                    } else {
                        errObj = LuaContext.toTruffleString(e.getMessage() != null ? e.getMessage() : e.toString());
                    }
                    exchanger.exchange(new Object[]{"error", errObj});
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // 清理本线程的协程标记
                try {
                    LuaContext.get(null).setCurrentCoroutine(previousCoroutine);
                } catch (Exception ignored) {
                    // 上下文已关闭
                }
                if (truffleContext != null && prev != null) {
                    truffleContext.leave(null, prev);
                }
            }
        };
        thread = env.createThread(body);
        if (thread == null) {
            throw new RuntimeException("cannot create coroutine thread: createThread returned null");
        }
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 关闭协程：唤醒阻塞在 {@link Exchanger} 上的线程并令其退出。
     * 由上下文销毁时调用（Truffle 要求语言线程在 dispose 前全部结束）。
     */
    public void dispose() {
        disposed = true;
        Thread t = thread;
        if (t != null) {
            t.interrupt();
        }
    }

    /** 等待协程线程结束（主线程对象没有底层线程，直接返回）。 */
    public void awaitTermination(long millis) {
        Thread t = thread;
        if (t == null || t == Thread.currentThread()) {
            return;
        }
        try {
            t.join(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 取消执行：上下文关闭时从 {@link #yield} 抛出，展开协程的 Lua 调用栈。
     * 不填充栈轨迹（纯控制流）。
     */
    private static final class AbortExecution extends RuntimeException {
        AbortExecution() {
            super(null, null, false, false);
        }
    }

    /**
     * 获取协程当前状态字符串（标准 Lua 格式）。
     */
    public String statusString() {
        return state.name().toLowerCase();
    }
}
