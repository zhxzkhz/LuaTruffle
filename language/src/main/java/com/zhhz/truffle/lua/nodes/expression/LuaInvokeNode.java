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
    @Child private LuaCallNode luaCallNode;

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
        this.luaCallNode = LuaCallNodeGen.create();
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



        // 2. 实参求值：得到不含 self 的显式实参（最后一个参数可能展开多返回值）。
        Object[] explicitArguments = evaluateArguments(frame);

        if (notCallable) {
            throw LuaException.runtimeError(getMessage(functionValue), this);
        }

        // Lua 函数：直接走专用调用节点，参数数组一次构造成 [function, receiver?, ...args]，
        // 省掉 InteropLibrary 分发与一次额外的数组复制（递归调用最热路径）。
        LuaFunction luaFunction = functionValue instanceof LuaFunction lf && isLuaRootCallTarget(lf)
                ? lf : null;
        if (luaFunction != null) {
            if (tailCall) {
                throw new LuaTailCallException(luaFunction, withReceiver(callableReceiver, explicitArguments));
            }
            Object[] frameArguments = new Object[explicitArguments.length + 1
                    + (callableReceiver == null ? 0 : 1)];
            frameArguments[0] = luaFunction;
            int offset = 1;
            if (callableReceiver != null) {
                frameArguments[offset++] = callableReceiver;
            }
            System.arraycopy(explicitArguments, 0, frameArguments, offset, explicitArguments.length);
            return luaCallNode.execute(luaFunction, frameArguments);
        }

        Object[] finalArguments = explicitArguments.length == 0 && callableReceiver == null
                ? null
                : withReceiver(callableReceiver, explicitArguments);
        return dispatchInteropCall(functionValue, finalArguments, instantiate);
    }

    @ExplodeLoop
    private Object[] evaluateArguments(VirtualFrame frame) {
        int argCount = this.argumentNodes.length;
        if (argCount == 0) {
            return new Object[0];
        }
        // 前 N-1 个参数长度固定为 1；JIT 通常能把这个小数组标量替换掉。
        final Object[] prefixArgs = new Object[argCount - 1];
        for (int i = 0; i < argCount - 1; i++) {
            Object rawArg = this.argumentNodes[i].executeGeneric(frame);
            if (rawArg == null) {
                throw new RuntimeException("unexpected array or null return value");
            }
            prefixArgs[i] = LuaMultiValue.firstValue(rawArg);
        }
        // 最后一个参数可能返回多值。
        Object lastResult = this.argumentNodes[argCount - 1].executeMultiValue(frame);
        if (lastResult == null) {
            throw new RuntimeException("unexpected array or null return value");
        }
        return LuaMultiValue.collect(prefixArgs, lastResult);
    }

    private static Object[] withReceiver(Object receiver, Object[] arguments) {
        if (receiver == null) {
            return arguments;
        }
        Object[] result = new Object[arguments.length + 1];
        result[0] = receiver;
        System.arraycopy(arguments, 0, result, 1, arguments.length);
        return result;
    }

    /** 以 CallTarget 的根节点区分 Lua 函数与内建（C）函数。 */
    private static boolean isLuaRootCallTarget(LuaFunction function) {
        return function.getCallTarget() instanceof com.oracle.truffle.api.RootCallTarget rct
                && rct.getRootNode() instanceof com.zhhz.truffle.lua.nodes.LuaRootNode;
    }

    /**
     * 非 Lua 调用的派发（内建函数、宿主可执行对象、实例化）：走 InteropLibrary，
     * 并记录调用点，供内建函数的参数错误消息解析函数名。
     */
    private Object dispatchInteropCall(Object functionValue, Object[] finalArguments, boolean instantiate) {
        com.zhhz.truffle.lua.runtime.LuaContext callContext =
                com.zhhz.truffle.lua.runtime.LuaContext.get(this);
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

        return functionValue instanceof com.zhhz.truffle.lua.runtime.LuaValue
                ? result
                : com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.normalizeHostValue(result);
    }

    @TruffleBoundary
    private String getMessage(Object functionValue) {
        return "attempt to call a "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getRuntimeTypeName(functionValue)
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
