package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.utilities.TriState;
import com.zhhz.truffle.lua.LuaLanguage;

/**
 * 代表Lua的函数类型（闭包），继承自LuaValue。
 *
 * <p>一个LuaFunction封装了两样东西：
 * <ol>
 *   <li><b>代码</b>: 一个 {@link CallTarget}，指向该函数的AST。</li>
 *   <li><b>环境</b>: 一个 {@link MaterializedFrame}，捕获了函数定义时的父作用域，用于实现upvalues（闭包）。</li>
 * </ol>
 *
 * <p>它实现了 InteropLibrary 的可执行协议，允许其他语言（如Java）直接调用这个Lua函数。
 */
@ExportLibrary(InteropLibrary.class)
public final class LuaFunction extends LuaValue {

    private final CallTarget callTarget;
    private MaterializedFrame parentFrame;
    private final TruffleString name; // (可选) 用于调试和错误信息

    /**
     * 此闭包捕获的 upvalue cell 数组，顺序与 {@code LuaRootNode.getUpvalueDescriptors()} 一致。
     * 每个元素是一个 {@link LuaCell}（被捕获变量的共享单元）。未捕获任何 upvalue 时为空数组。
     */
    private Object[] upvalues = EMPTY_UPVALUES;

    private static final Object[] EMPTY_UPVALUES = new Object[0];

    /**
     * 该函数作为 chunk 被调用时使用的环境（{@code _ENV} 初值）。
     * 由 {@code load}/require 的第 4 个参数设置；{@code null} 表示未指定（回退到全局表）。
     * 对于普通嵌套函数该字段不用，闭包通过 {@link #parentFrame} 捕获词法父帧。
     */
    private Object environment;

    /**
     * 该函数需要保持强引用的附属资源。主要用于 io.lines 迭代器：迭代器的 AST（CallTarget）
     * 会被调用点缓存而无法及时回收，因此把文件句柄改为挂在 LuaFunction 上强引用，
     * 由 LuaFunction 的生命周期决定句柄的生命周期，从而支持 GC 终结（collectgarbage 关闭文件）。
     */
    private Object retainedResource;

    /**
     * 该函数对应的字节码原型 (Proto)。
     * <ul>
     *   <li>{@link #vmClosure} 为真时：这是由字节码 VM 执行的闭包；</li>
     *   <li>否则：这是由 AST 引擎执行的源码函数，此 Proto 仅用于 {@code string.dump}。</li>
     * </ul>
     */
    private com.zhhz.truffle.lua.bytecode.Proto proto;

    /** 该函数是否为由 {@code lvm.c} 移植的字节码 VM 执行的闭包。 */
    private boolean vmClosure;

    public com.zhhz.truffle.lua.bytecode.Proto getProto() {
        return proto;
    }

    public void setProto(com.zhhz.truffle.lua.bytecode.Proto proto) {
        this.proto = proto;
    }

    public boolean isVmClosure() {
        return vmClosure;
    }

    public void setVmClosure(boolean vmClosure) {
        this.vmClosure = vmClosure;
    }

    public LuaFunction(Shape shape, CallTarget callTarget, MaterializedFrame parentFrame) {
        this(shape,callTarget, parentFrame, null);
    }

    public LuaFunction(Shape shape, CallTarget callTarget, MaterializedFrame parentFrame, TruffleString name) {
        super(shape);
        this.callTarget = callTarget;
        this.parentFrame = parentFrame;
        this.name = name;
    }

    public LuaFunction(Shape shape, CallTarget callTarget) {
        super(shape);
        this.callTarget = callTarget;
        this.name = null;
    }

    public CallTarget getCallTarget() {
        return callTarget;
    }

    /**
     * 从宿主/解释器内部直接调用本函数，不经过 interop 分发。
     * <p>
     * 与 {@link com.oracle.truffle.api.interop.InteropLibrary#execute} 的区别：interop 调用
     * 会在调用栈上插入一个 host→guest 包装帧，导致 {@code debug.getinfo} 的层级多出一层
     * 伪 C 帧（钩子内 {@code getinfo(2)} 因此看不到被调函数）。钩子调用需要精确的层级，
     * 故这里直接使用 CallTarget。
     */
    public Object callFromHook(Object... arguments) {
        return invoke(arguments);
    }

    /**
     * 从非 Lua 调用点（宿主、内建函数、协程线程入口）调用本函数。
     * <p>
     * 与 {@link #callFromHook} 相同，但语义更通用：目标函数内部若发生尾调用，
     * {@link LuaTailCallException} 会冒泡到本方法，由固定深度的重试循环接续执行。
     * 直接从 CallTarget 调用会把这个控制流异常泄漏给宿主（例如协程体
     * {@code return f()} 会让 {@code coroutine.resume} 返回异常对象）。
     */
    public Object invoke(Object... arguments) {
        Object[] frameArguments = new Object[arguments.length + 1];
        frameArguments[0] = this;
        System.arraycopy(arguments, 0, frameArguments, 1, arguments.length);
        try {
            return getCallTarget().call(frameArguments);
        } catch (LuaTailCallException tce) {
            // 钩子内发生尾调用：在固定 Java 栈深度下重复派发，直到正常返回。
            LuaTailCallException e = tce;
            while (true) {
                LuaFunction target = e.getFunction();
                Object[] args = e.getArguments();
                Object[] frameArgs = new Object[args.length + 1];
                frameArgs[0] = target;
                System.arraycopy(args, 0, frameArgs, 1, args.length);
                LuaContext context = LuaContext.get(null);
                if (context != null) {
                    context.setPendingTailCall(true);
                }
                try {
                    return target.getCallTarget().call(frameArgs);
                } catch (LuaTailCallException next) {
                    e = next;
                } finally {
                    if (context != null) {
                        context.setPendingTailCall(false);
                    }
                }
            }
        }
    }

    public MaterializedFrame getParentFrame() {
        return parentFrame;
    }

    /** 闭包捕获的 upvalue cell 数组（元素为 {@link LuaCell}）。 */
    public Object[] getUpvalues() {
        return upvalues;
    }

    public void setUpvalues(Object[] upvalues) {
        this.upvalues = upvalues != null ? upvalues : EMPTY_UPVALUES;
    }

    public Object getEnvironment() {
        return environment;
    }

    public void setEnvironment(Object environment) {
        this.environment = environment;
    }

    public Object getRetainedResource() {
        return retainedResource;
    }

    public void setRetainedResource(Object retainedResource) {
        this.retainedResource = retainedResource;
    }

    /**
     * 调用该函数时应放入新帧 {@code arguments[0]} 的值：普通闭包为其词法父帧（静态链），
     * 而 chunk（无父帧）则为其环境表；若都没有则返回 {@code null}（由 chunk 序言回退到全局表）。
     */
    public Object getCallParent() {
        return parentFrame != null ? parentFrame : environment;
    }

    public TruffleString getName() {
        return name;
    }

    // --- 继承自 LuaValue 的方法 ---

    @Override
    public String getTypeName() {
        return "function";
    }

    @Override
    public String toString() {
        if (name != null) {
            return "function: " + name.toJavaStringUncached();
        }
        return "function: 0x" + Integer.toHexString(hashCode());
    }


    // =================================================================================
    // InteropLibrary “可执行”协议的实现
    // =================================================================================

    @ExportMessage
    public boolean hasLanguageId() {
        return true;
    }

    @ExportMessage
    public String getLanguageId() {
        return com.zhhz.truffle.lua.LuaLanguage.ID;
    }


    /**
     * 宣称自己是一个可执行的对象。
     * 这告诉其他语言，可以对我使用 `()` 操作符。
     */
    @ExportMessage
    public boolean isExecutable() {
        return true;
    }

    @ExportMessage
    boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    Object getMetaObject() {
        return LuaType.FUNCTION;
    }

    @ExportMessage
    @SuppressWarnings("unused")
    static final class IsIdenticalOrUndefined {
        @Specialization
        static TriState doLuaFunction(LuaFunction receiver, LuaFunction other) {
            /*
             * LuaFunctions are potentially identical to other LuaFunctions.
             */
            return receiver == other ? TriState.TRUE : TriState.FALSE;
        }

        @Fallback
        static TriState doOther(LuaFunction receiver, Object other) {
            return TriState.UNDEFINED;
        }
    }

    @ExportMessage
    @CompilerDirectives.TruffleBoundary
    static int identityHashCode(LuaFunction receiver) {
        return System.identityHashCode(receiver);
    }

    
    public void setParentFrame(MaterializedFrame materialize) {
        this.parentFrame = materialize;
    }

    // =================================================================================
    // InteropLibrary “可执行”协议的【高性能】实现
    // =================================================================================

    /**
     * 核心执行逻辑。
     * 这里使用了静态内部类和 @Specialization 来实现【内联缓存 (Inline Caching)】。
     * <p>
     * 这确保了当同一个 LuaFunction 被反复调用时，走的是极快的 DirectCallNode，
     * 而不是慢速的 IndirectCallNode。
     * <p>
     * 此外，这里实现了【尾调用优化 (TCO)】：当被调用函数内部执行尾调用时
     * （{@code return f(...)}），会抛出 {@link LuaTailCallException}。本方法捕获该异常，
     * 在固定 Java 栈深度下循环调用尾调用目标，避免递归导致栈溢出（Lua 手册 3.4.10）。
     * <p>
     * 两条路径（doDirect / doIndirect）都必须捕获该异常，否则异常会越过任何非尾位置的
     * 调用者（如 {@code return B() + 1} 中的 +1）冒泡到更外层，导致外层计算被静默丢弃。
     */
    @ExportMessage
    public static class Execute {

        /**
         * 快速路径：当被调用的函数 (CallTarget) 在连续调用中保持不变时，
         * 使用一个超快的 DirectCallNode。
         * `fibonacci` 的递归调用会 100% 命中这个缓存。
         * <p>
         * 尾调用目标可能指向不同的 CallTarget，故循环内改用通用的 {@link IndirectCallNode}
         * 派发，避免 DirectCallNode 在多态尾调用场景下频繁重建。
         */
        @Specialization(guards = "function.getCallTarget() == cachedTarget", limit = "1")
        protected static Object doDirect(LuaFunction function, Object[] arguments,
                                         @Cached("function.getCallTarget()") CallTarget cachedTarget,
                                         @Cached("create(cachedTarget)") DirectCallNode callNode,
                                         @Cached.Shared("indirect") @Cached IndirectCallNode tailCallNode) {

            // 准备最终的参数数组（被调用的 LuaFunction + 用户参数）
            final Object[] frameArguments = new Object[arguments.length + 1];
            frameArguments[0] = function;
            System.arraycopy(arguments, 0, frameArguments, 1, arguments.length);
            // 【正确】通过高性能的 CallNode 执行；并捕获尾调用异常，避免冒泡越过非尾调用者
            try {
                return callNode.call(frameArguments);
            } catch (LuaTailCallException tce) {
                return runTailCallLoop(tce, tailCallNode);
            }
        }

        /**
         * 慢速路径：当调用点变得多态（调用不同的函数）时，
         * 回退到使用稍慢但更通用的 IndirectCallNode。
         */
        @Specialization(replaces = "doDirect")
        protected static Object doIndirect(LuaFunction function, Object[] arguments,
                                           @Cached.Shared("indirect") @Cached IndirectCallNode callNode) {

            Object[] frameArguments = new Object[arguments.length + 1];
            frameArguments[0] = function;
            System.arraycopy(arguments, 0, frameArguments, 1, arguments.length);

            // 【正确】通过通用的 CallNode 执行
            try {
                return callNode.call(function.getCallTarget(), frameArguments);
            } catch (LuaTailCallException tce) {
                return runTailCallLoop(tce, callNode);
            }
        }

        /**
         * 尾调用重试循环：目标函数内部若产生尾调用会抛出 {@link LuaTailCallException}
         * （未被目标 execute 捕获而冒泡到此处），在固定 Java 栈深度下反复调用新的
         * 目标 CallTarget，直到某次调用正常返回，避免递归栈溢出（Lua 手册 3.4.10）。
         */
        private static Object runTailCallLoop(LuaTailCallException initial, IndirectCallNode callNode) {
            LuaTailCallException tce = initial;
            while (true) {
                LuaFunction target = tce.getFunction();
                Object[] args = tce.getArguments();
                Object[] frameArguments = new Object[args.length + 1];
                frameArguments[0] = target;
                System.arraycopy(args, 0, frameArguments, 1, args.length);
                LuaContext context = LuaContext.get(null);
                if (context != null) {
                    context.setPendingTailCall(true);
                }
                try {
                    return callNode.call(target.getCallTarget(), frameArguments);
                } catch (LuaTailCallException next) {
                    tce = next;
                } finally {
                    if (context != null) {
                        context.setPendingTailCall(false);
                    }
                }
            }
        }
    }

}