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
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.access.LuaReadTableNode;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaTailCallException;
import com.zhhz.truffle.lua.runtime.LuaValue;

@NodeInfo(shortName = "method_call")
public final class LuaMethodCallNode extends LuaExpressionNode {

    @Child private LuaExpressionNode receiverNode;
    @Child private LuaExpressionNode methodKeyNode;
    @Children private final LuaExpressionNode[] argumentNodes;
    @Child private InteropLibrary library;

    /** 是否处于尾调用位置（由 LuaReturnNode 在解析期标记）。 */
    private boolean tailCall;

    public LuaMethodCallNode(LuaExpressionNode receiverNode, LuaExpressionNode methodKeyNode, LuaExpressionNode[] argumentNodes) {
        this.receiverNode = receiverNode;
        this.methodKeyNode = methodKeyNode;
        this.argumentNodes = argumentNodes;
        this.library = InteropLibrary.getFactory().createDispatched(3);
    }

    /** 标记此调用处于尾调用位置（return obj:method(...)）。 */
    public void markTailCall() {
        this.tailCall = true;
    }

    private Object unpackResult(Object result) {
        return LuaMultiValue.firstValue(result);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return LuaMultiValue.firstValue(executeMultiValue(frame));
    }

    @Override
    @ExplodeLoop
    public Object executeMultiValue(VirtualFrame frame) {
        Object receiverValue = receiverNode.executeGeneric(frame);

        Object methodKey = methodKeyNode.executeGeneric(frame);
        // 方法调用读路径的 "attempt to index a ... value" 错误需要带上接收者表达式的
        // 变量来源后缀（如 aaa.bbb:ddd() -> (field 'bbb')）。
        Object functionValue = unpackResult(
                LuaReadTableNode.readUncached(this, receiverValue, methodKey, receiverNode.getErrorSuffix()));

        if (!library.isExecutable(functionValue)) {
            String message = getMessage(functionValue);
            throw LuaException.runtimeError(message, this);
        }

        int explicitArgCount = this.argumentNodes.length;

        if (explicitArgCount == 0) {
            return dispatchCall(functionValue, new Object[]{receiverValue});
        }

        final Object[] prefixArgs = new Object[explicitArgCount - 1 + 1];
        prefixArgs[0] = receiverValue;
        for (int i = 0; i < explicitArgCount - 1; i++) {
            Object rawArg = this.argumentNodes[i].executeGeneric(frame);
            if (rawArg == null) {
                throw LuaException.create("unexpected multi-return or nil in method call argument", this);
            }
            prefixArgs[i + 1] = LuaMultiValue.firstValue(rawArg);
        }

        Object lastResult = this.argumentNodes[explicitArgCount - 1].executeMultiValue(frame);
        if (lastResult == null) {
            throw LuaException.create("unexpected multi-return or nil in method call argument", this);
        }
        Object[] finalArguments = LuaMultiValue.collect(prefixArgs, lastResult);

        return dispatchCall(functionValue, finalArguments);
    }

    /**
     * 派发调用：若处于尾调用位置且目标为 Lua 函数，抛出 {@link LuaTailCallException}
     * 由根节点循环重试，避免 Java 栈增长；否则走普通 InteropLibrary 调用路径。
     * <p>
     * 仅对真正的 Lua 函数（{@link com.zhhz.truffle.lua.nodes.LuaRootNode}）做 TCO；
     * 内建函数（{@link com.zhhz.truffle.lua.builtins.BuiltinRootNode}，对应 C 函数）
     * 不做尾调用优化——它们对应 Lua 语义中的 C 调用，不应替换栈帧（Lua 5.3 手册 3.4.10）。
     */
    private Object dispatchCall(Object functionValue, Object[] finalArguments) {
        if (tailCall && functionValue instanceof LuaFunction luaFunction
                && luaFunction.getCallTarget() instanceof com.oracle.truffle.api.RootCallTarget rct
                && rct.getRootNode() instanceof com.zhhz.truffle.lua.nodes.LuaRootNode) {
            throw new LuaTailCallException(luaFunction, finalArguments);
        }
        // 记录调用点，供内建函数的参数错误消息解析方法名与 "bad self" 语义。
        com.zhhz.truffle.lua.runtime.LuaContext callContext =
                com.zhhz.truffle.lua.runtime.LuaContext.get(this);
        com.oracle.truffle.api.nodes.Node previousCallSite =
                callContext != null ? callContext.getPendingCallSite() : null;
        if (callContext != null) {
            callContext.setPendingCallSite(this);
        }
        try {
            return execReturn(functionValue, finalArguments);
        } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
            throw LuaException.undefinedFunction(this, e);
        } finally {
            if (callContext != null) {
                callContext.setPendingCallSite(previousCallSite);
            }
        }
    }

    private Object execReturn(Object functionValue, Object[] finalArguments) throws UnsupportedMessageException, UnsupportedTypeException, ArityException {
        return library.execute(functionValue, finalArguments);
    }

    @TruffleBoundary
    private String getMessage(Object functionValue) {
        String methodName = "?";
        if (methodKeyNode instanceof com.zhhz.truffle.lua.nodes.expression.LuaStringLiteralNode literal) {
            TruffleString name = literal.getUnresolvedVariableName();
            if (name != null) {
                methodName = name.toJavaStringUncached();
            }
        }
        return "attempt to call a "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName(functionValue)
                + " value (method '" + methodName + "')";
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.CallTag.class) {
            return true;
        }
        return super.hasTag(tag);
    }

    /** 调试信息：方法名 key 节点（debug.getinfo 调用点分析）。 */
    public LuaExpressionNode getDebugMethodKeyNode() {
        return methodKeyNode;
    }
}
