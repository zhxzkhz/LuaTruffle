package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.access.LuaReadTableNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNodeGen;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaTailCallException;
import com.zhhz.truffle.lua.runtime.LuaValue;


@NodeInfo(shortName = "invoke")
public final class LuaInvokeNode extends LuaExpressionNode {

    @Child private LuaExpressionNode functionNode;
    @Children private final LuaExpressionNode[] argumentNodes;
    @Child private InteropLibrary library;
    @Child private LuaMetatableNode metatableNode;

    /** 是否处于尾调用位置（由 LuaReturnNode 在解析期标记）。 */
    private boolean tailCall;

    public LuaInvokeNode(LuaExpressionNode functionNode, LuaExpressionNode[] argumentNodes) {
        this.functionNode = functionNode;
        if (functionNode == null){
            throw new RuntimeException("functionNode 为空");
        }
        this.argumentNodes = argumentNodes;
        this.library = InteropLibrary.getFactory().createDispatched(3);
        this.metatableNode = LuaMetatableNodeGen.create();
    }

    /** 标记此调用处于尾调用位置（return f(...)）。 */
    public void markTailCall() {
        this.tailCall = true;
    }

    @Override
    @ExplodeLoop
    public Object executeGeneric(VirtualFrame frame) {
        return LuaMultiValue.firstValue(executeMultiValue(frame));
    }

    @Override
    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        Object result = executeGeneric(frame);
        if (result instanceof Long l) {
            return l;
        }
        throw new UnexpectedResultException(result);
    }

    @Override
    @ExplodeLoop
    public Object executeMultiValue(VirtualFrame frame) {

        Object functionValue = LuaMultiValue.firstValue(functionNode.executeGeneric(frame));
        Object callableReceiver = null;
        boolean instantiate = false;
        boolean notCallable = false;

        if (!library.isExecutable(functionValue)) {
            Object metamethod = metatableNode.lookup(functionValue, LuaMetatables.__CALL);
            if (metamethod == null || !library.isExecutable(metamethod)) {
                // import() 导入的宿主 Java 类：调用即实例化（Date() -> new Date(...)）
                if (library.isInstantiable(functionValue)) {
                    instantiate = true;
                } else {
                    // 标准 Lua 先求值全部实参，再到 CALL 指令处才报告函数不可调用；
                    // 因此这里只做标记，延迟到实参求值之后再抛错（保证报错点位与顺序一致）。
                    notCallable = true;
                }
            } else {
                callableReceiver = functionValue;
                functionValue = metamethod;
            }
        }



        // 2. 【参数处理优化】
        int argCount = this.argumentNodes.length;

        // 优化：无参调用直接处理
        if (argCount == 0) {
            if (notCallable) {
                throw LuaException.runtimeError(getMessage(functionValue), this);
            }
            Object[] finalArguments = callableReceiver == null ? null : new Object[]{callableReceiver};
            return dispatchCall(frame, functionValue, finalArguments, instantiate);
        }

        // 步骤 A: 计算前 N-1 个参数 (这些参数长度固定为 1)
        // 我们先用一个临时数组存起来。JIT 编译器通常能将这个小数组虚拟化(scalarize)，
        // 甚至直接在寄存器中传递，不会产生真实的堆分配。
        int implicitArgCount = callableReceiver == null ? 0 : 1;
        final Object[] prefixArgs = new Object[argCount - 1 + implicitArgCount];
        if (callableReceiver != null) {
            prefixArgs[0] = callableReceiver;
        }
        for (int i = 0; i < argCount - 1; i++) {
            Object rawArg = this.argumentNodes[i].executeGeneric(frame);
            if (rawArg == null) {
                throw new RuntimeException("unexpected array or null return value");
            }
            prefixArgs[i + implicitArgCount] = LuaMultiValue.firstValue(rawArg);
        }
        // 步骤 B: 计算最后一个参数 (可能返回多值)
        Object lastResult = this.argumentNodes[argCount - 1].executeMultiValue(frame);

        if (lastResult == null) {
            throw new RuntimeException("unexpected array or null return value");
        }
        Object[] finalArguments = LuaMultiValue.collect(prefixArgs, lastResult);

        if (notCallable) {
            throw LuaException.runtimeError(getMessage(functionValue), this);
        }
        return dispatchCall(frame, functionValue, finalArguments, instantiate);
    }

    /**
     * 派发调用：若处于尾调用位置且目标为 Lua 函数，抛出 {@link LuaTailCallException}
     * 由根节点循环重试，避免 Java 栈增长；否则走普通 InteropLibrary 调用路径。
     * <p>
     * 仅对真正的 Lua 函数（{@link com.zhhz.truffle.lua.nodes.LuaRootNode}）做 TCO；
     * 内建函数（{@link com.zhhz.truffle.lua.builtins.BuiltinRootNode}，对应 C 函数）
     * 不做尾调用优化——它们对应 Lua 语义中的 C 调用，不应替换栈帧（Lua 5.3 手册 3.4.10）。
     */
    private Object dispatchCall(VirtualFrame frame, Object functionValue, Object[] finalArguments, boolean instantiate) {
        LuaFunction luaFunction = functionValue instanceof LuaFunction lf ? lf : null;
        // 以 CallTarget 的根节点区分 Lua 函数与内建（C）函数。
        boolean luaCall = luaFunction != null
                && luaFunction.getCallTarget() instanceof com.oracle.truffle.api.RootCallTarget rct
                && rct.getRootNode() instanceof com.zhhz.truffle.lua.nodes.LuaRootNode;
        if (luaCall && tailCall) {
            throw new LuaTailCallException(luaFunction, finalArguments == null ? new Object[0] : finalArguments);
        }
        // 记录调用点，供内建函数的参数错误消息（luaL_argerror / pushglobalfuncname）解析函数名。
        // Lua 函数调用不需要该信息，直接跳过上下文查询与字段读写（递归调用的最热路径）。
        com.zhhz.truffle.lua.runtime.LuaContext callContext =
                luaCall ? null : com.zhhz.truffle.lua.runtime.LuaContext.get(this);
        com.oracle.truffle.api.nodes.Node previousCallSite =
                callContext != null ? callContext.getPendingCallSite() : null;
        if (callContext != null) {
            callContext.setPendingCallSite(this);
        }
        try {
            return instantiate
                    ? execInstantiate(functionValue, finalArguments)
                    : execReturn(functionValue, finalArguments);
        } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
            throw LuaException.undefinedFunction(this, e);
        } finally {
            if (callContext != null) {
                callContext.setPendingCallSite(previousCallSite);
            }
        }
    }

    /** 宿主类实例化：interop 的 instantiate 消息（import 导入的 Java 类构造调用）。 */
    private Object execInstantiate(Object functionValue, Object[] finalArguments) throws UnsupportedMessageException, UnsupportedTypeException, ArityException {
        if (finalArguments == null) {
            return library.instantiate(functionValue);
        }
        return library.instantiate(functionValue, finalArguments);
    }

    private Object execReturn(Object functionValue,Object[] finalArguments) throws UnsupportedMessageException, UnsupportedTypeException, ArityException {
        Object result;
        if (finalArguments == null){
            result = library.execute(functionValue);
        } else {
            result = library.execute(functionValue, finalArguments);
        }

        return result;
    }

    @TruffleBoundary
    private String getMessage(Object functionValue) {
        return "attempt to call a "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName(functionValue)
                + " value" + this.functionNode.getErrorSuffix();
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.CallTag.class) {
            return true;
        }
        return super.hasTag(tag);
    }



    /** 调试信息：被调用函数的表达式节点（debug.getinfo 调用点分析）。 */
    public LuaExpressionNode getDebugFunctionNode() {
        return functionNode;
    }
}
