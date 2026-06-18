// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.InstrumentableNode.WrapperNode;

@GeneratedBy(LuaStatementNode.class)
final class LuaStatementNodeWrapper extends LuaStatementNode implements WrapperNode {

    @Child private LuaStatementNode delegateNode;
    @Child private ProbeNode probeNode;

    LuaStatementNodeWrapper(LuaStatementNode delegateNode, ProbeNode probeNode) {
        this.delegateNode = delegateNode;
        this.probeNode = probeNode;
    }

    @Override
    public LuaStatementNode getDelegateNode() {
        return delegateNode;
    }

    @Override
    public ProbeNode getProbeNode() {
        return probeNode;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        for (;;) {
            boolean wasOnReturnExecuted = false;
            try {
                probeNode.onEnter(frame);
                delegateNode.executeVoid(frame);
                wasOnReturnExecuted = true;
                probeNode.onReturnValue(frame, null);
                break;
            } catch (Throwable t) {
                Object result = probeNode.onReturnExceptionalOrUnwind(frame, t, wasOnReturnExecuted);
                if (result == ProbeNode.UNWIND_ACTION_REENTER) {
                    continue;
                } else if (result != null) {
                    break;
                }
                throw t;
            }
        }
    }

}
