package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaTailCallException;

/**
 * Lua 函数调用的专用派发节点：跳过 {@code InteropLibrary.execute} 的通用分发，
 * 直接以 {@link DirectCallNode} 调用 CallTarget，使调用点在编译后可以内联。
 *
 * <p>参数数组由调用方一次性构造为 {@code [function, ...args]}，因此这里不再复制。
 */
@NodeInfo(shortName = "lua-call")
public abstract class LuaCallNode extends Node {

    public abstract Object execute(LuaFunction function, Object[] frameArguments);

    @Specialization(guards = "cachedTarget == function.getCallTarget()", limit = "2")
    protected static Object doDirect(LuaFunction function, Object[] frameArguments,
                                     @Cached("function.getCallTarget()") CallTarget cachedTarget,
                                     @Cached("create(cachedTarget)") DirectCallNode callNode,
                                     @Cached.Shared("indirect") @Cached IndirectCallNode tailCallNode) {
        try {
            return callNode.call(frameArguments);
        } catch (LuaTailCallException tce) {
            return runTailCallLoop(tce, tailCallNode);
        }
    }

    @Specialization(replaces = "doDirect")
    protected static Object doIndirect(LuaFunction function, Object[] frameArguments,
                                       @Cached.Shared("indirect") @Cached IndirectCallNode callNode) {
        try {
            return callNode.call(function.getCallTarget(), frameArguments);
        } catch (LuaTailCallException tce) {
            return runTailCallLoop(tce, callNode);
        }
    }

    /**
     * 尾调用重试循环：与 {@link LuaFunction.Execute} 的语义一致，在固定 Java 栈深度下
     * 反复调用新的 CallTarget，直到某次调用正常返回。
     */
    static Object runTailCallLoop(LuaTailCallException initial, IndirectCallNode callNode) {
        LuaTailCallException tce = initial;
        while (true) {
            LuaFunction target = tce.getFunction();
            Object[] args = tce.getArguments();
            Object[] frameArguments = new Object[args.length + 1];
            frameArguments[0] = target;
            System.arraycopy(args, 0, frameArguments, 1, args.length);
            com.zhhz.truffle.lua.runtime.LuaContext context =
                    com.zhhz.truffle.lua.runtime.LuaContext.get(null);
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
