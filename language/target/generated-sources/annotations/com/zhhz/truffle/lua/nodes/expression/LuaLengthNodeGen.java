// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.CodePointLengthNode;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaLengthNode#lengthOfString(LuaString, CodePointLengthNode)}
 *     Activation probability: 0.38500
 *     With/without class size: 11/4 bytes
 *   Specialization {@link LuaLengthNode#lengthOfString(TruffleString, CodePointLengthNode)}
 *     Activation probability: 0.29500
 *     With/without class size: 9/4 bytes
 *   Specialization {@link LuaLengthNode#lengthOfTable}
 *     Activation probability: 0.20500
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaLengthNode#typeError}
 *     Activation probability: 0.11500
 *     With/without class size: 5/0 bytes
 * </pre> */
@GeneratedBy(LuaLengthNode.class)
@SuppressWarnings("javadoc")
public final class LuaLengthNodeGen extends LuaLengthNode {

    @Child private LuaExpressionNode operandNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaLengthNode#lengthOfString(LuaString, CodePointLengthNode)}
     *   1: SpecializationActive {@link LuaLengthNode#lengthOfString(TruffleString, CodePointLengthNode)}
     *   2: SpecializationActive {@link LuaLengthNode#lengthOfTable}
     *   3: SpecializationActive {@link LuaLengthNode#typeError}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaLengthNode#lengthOfString(LuaString, CodePointLengthNode)}
     *   Parameter: {@link CodePointLengthNode} codePointLengthNode</pre> */
    @Child private CodePointLengthNode lengthOfString0_codePointLengthNode_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaLengthNode#lengthOfString(TruffleString, CodePointLengthNode)}
     *   Parameter: {@link CodePointLengthNode} codePointLengthNode</pre> */
    @Child private CodePointLengthNode lengthOfString1_codePointLengthNode_;

    private LuaLengthNodeGen(LuaExpressionNode operandNode) {
        this.operandNode_ = operandNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object operandNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLengthNode.lengthOfString(LuaString, CodePointLengthNode)] */) && operandNodeValue instanceof LuaString) {
            return false;
        }
        if (!((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLengthNode.lengthOfString(TruffleString, CodePointLengthNode)] */) && operandNodeValue instanceof TruffleString) {
            return false;
        }
        if (!((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLengthNode.lengthOfTable(LuaTable)] */) && operandNodeValue instanceof LuaTable) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        Object operandNodeValue_ = this.operandNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaLengthNode.lengthOfString(LuaString, CodePointLengthNode)] || SpecializationActive[LuaLengthNode.lengthOfString(TruffleString, CodePointLengthNode)] || SpecializationActive[LuaLengthNode.lengthOfTable(LuaTable)] || SpecializationActive[LuaLengthNode.typeError(Object, Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLengthNode.lengthOfString(LuaString, CodePointLengthNode)] */ && operandNodeValue_ instanceof LuaString) {
                LuaString operandNodeValue__ = (LuaString) operandNodeValue_;
                {
                    CodePointLengthNode codePointLengthNode__ = this.lengthOfString0_codePointLengthNode_;
                    if (codePointLengthNode__ != null) {
                        return lengthOfString(operandNodeValue__, codePointLengthNode__);
                    }
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLengthNode.lengthOfString(TruffleString, CodePointLengthNode)] */ && operandNodeValue_ instanceof TruffleString) {
                TruffleString operandNodeValue__ = (TruffleString) operandNodeValue_;
                {
                    CodePointLengthNode codePointLengthNode__1 = this.lengthOfString1_codePointLengthNode_;
                    if (codePointLengthNode__1 != null) {
                        return lengthOfString(operandNodeValue__, codePointLengthNode__1);
                    }
                }
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLengthNode.lengthOfTable(LuaTable)] */ && operandNodeValue_ instanceof LuaTable) {
                LuaTable operandNodeValue__ = (LuaTable) operandNodeValue_;
                return lengthOfTable(operandNodeValue__);
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLengthNode.typeError(Object, Node)] */) {
                {
                    Node node__ = (this);
                    if (fallbackGuard_(state_0, operandNodeValue_)) {
                        return typeError(operandNodeValue_, node__);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(operandNodeValue_);
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeGeneric(frameValue);
        return;
    }

    private Object executeAndSpecialize(Object operandNodeValue) {
        int state_0 = this.state_0_;
        if (operandNodeValue instanceof LuaString) {
            LuaString operandNodeValue_ = (LuaString) operandNodeValue;
            CodePointLengthNode codePointLengthNode__ = this.insert((CodePointLengthNode.create()));
            Objects.requireNonNull(codePointLengthNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
            VarHandle.storeStoreFence();
            this.lengthOfString0_codePointLengthNode_ = codePointLengthNode__;
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaLengthNode.lengthOfString(LuaString, CodePointLengthNode)] */;
            this.state_0_ = state_0;
            return lengthOfString(operandNodeValue_, codePointLengthNode__);
        }
        if (operandNodeValue instanceof TruffleString) {
            TruffleString operandNodeValue_ = (TruffleString) operandNodeValue;
            CodePointLengthNode codePointLengthNode__1 = this.insert((CodePointLengthNode.create()));
            Objects.requireNonNull(codePointLengthNode__1, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
            VarHandle.storeStoreFence();
            this.lengthOfString1_codePointLengthNode_ = codePointLengthNode__1;
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaLengthNode.lengthOfString(TruffleString, CodePointLengthNode)] */;
            this.state_0_ = state_0;
            return lengthOfString(operandNodeValue_, codePointLengthNode__1);
        }
        if (operandNodeValue instanceof LuaTable) {
            LuaTable operandNodeValue_ = (LuaTable) operandNodeValue;
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaLengthNode.lengthOfTable(LuaTable)] */;
            this.state_0_ = state_0;
            return lengthOfTable(operandNodeValue_);
        }
        {
            Node node__ = null;
            node__ = (this);
            state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaLengthNode.typeError(Object, Node)] */;
            this.state_0_ = state_0;
            return typeError(operandNodeValue, node__);
        }
    }

    @NeverDefault
    public static LuaLengthNode create(LuaExpressionNode operandNode) {
        return new LuaLengthNodeGen(operandNode);
    }

}
