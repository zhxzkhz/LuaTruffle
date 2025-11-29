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

    public MaterializedFrame getParentFrame() {
        return parentFrame;
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
    public boolean hasLanguage() {
        return true;
    }

    @ExportMessage
    public Class<? extends TruffleLanguage<?>> getLanguage() {
        return LuaLanguage.class;
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
     */
    @ExportMessage
    public static class Execute {

        /**
         * 快速路径：当被调用的函数 (CallTarget) 在连续调用中保持不变时，
         * 使用一个超快的 DirectCallNode。
         * `fibonacci` 的递归调用会 100% 命中这个缓存。
         */
        @Specialization(guards = "function.getCallTarget() == cachedTarget", limit = "1")
        protected static Object doDirect(LuaFunction function, Object[] arguments,
                                         @Cached("function.getCallTarget()") CallTarget cachedTarget,
                                         @Cached("create(cachedTarget)") DirectCallNode callNode) {

            // 准备最终的参数数组（父 Frame + 用户参数）
            final Object[] frameArguments = new Object[arguments.length + 1];
            frameArguments[0] = function.getParentFrame();
            System.arraycopy(arguments, 0, frameArguments, 1, arguments.length);
            // 【正确】通过高性能的 CallNode 执行
            return callNode.call(frameArguments);
        }

        /**
         * 慢速路径：当调用点变得多态（调用不同的函数）时，
         * 回退到使用稍慢但更通用的 IndirectCallNode。
         */
        @Specialization(replaces = "doDirect")
        protected static Object doIndirect(LuaFunction function, Object[] arguments,
                                           @Cached IndirectCallNode callNode) {

            final Object[] frameArguments = new Object[arguments.length + 1];
            frameArguments[0] = function.getParentFrame();
            System.arraycopy(arguments, 0, frameArguments, 1, arguments.length);

            // 【正确】通过通用的 CallNode 执行
            return callNode.call(function.getCallTarget(), frameArguments);
        }
    }

}