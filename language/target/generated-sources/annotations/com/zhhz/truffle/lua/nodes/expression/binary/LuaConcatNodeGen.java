// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.dsl.InlineSupport.InlineTarget;
import com.oracle.truffle.api.dsl.InlineSupport.ReferenceField;
import com.oracle.truffle.api.dsl.InlineSupport.StateField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.ConcatNode;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.nodes.util.LuaToTruffleStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaToTruffleStringNodeGen;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaConcatNode#doConcat}
 *     Activation probability: 0.65000
 *     With/without class size: 37/35 bytes
 *   Specialization {@link LuaConcatNode#doGeneric}
 *     Activation probability: 0.35000
 *     With/without class size: 22/35 bytes
 * </pre> */
@GeneratedBy(LuaConcatNode.class)
@SuppressWarnings("javadoc")
public final class LuaConcatNodeGen extends LuaConcatNode {

    private static final StateField CONCAT_LUA_CONCAT_NODE_CONCAT_STATE_0_UPDATER = StateField.create(ConcatData.lookup_(), "concat_state_0_");
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaConcatNode#doConcat}
     *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
     *   Inline method: {@link LuaToTruffleStringNodeGen#inline}</pre> */
    private static final LuaToTruffleStringNode INLINED_CONCAT_TO_TRUFFLE_STRING_NODE_LEFT_ = LuaToTruffleStringNodeGen.inline(InlineTarget.create(LuaToTruffleStringNode.class, CONCAT_LUA_CONCAT_NODE_CONCAT_STATE_0_UPDATER.subUpdater(0, 9), ReferenceField.create(ConcatData.lookup_(), "concat_toTruffleStringNodeLeft__field1_", Node.class), ReferenceField.create(ConcatData.lookup_(), "concat_toTruffleStringNodeLeft__field2_", Node.class), ReferenceField.create(ConcatData.lookup_(), "concat_toTruffleStringNodeLeft__field3_", Node.class)));
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaConcatNode#doConcat}
     *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
     *   Inline method: {@link LuaToTruffleStringNodeGen#inline}</pre> */
    private static final LuaToTruffleStringNode INLINED_CONCAT_TO_TRUFFLE_STRING_NODE_RIGHT_ = LuaToTruffleStringNodeGen.inline(InlineTarget.create(LuaToTruffleStringNode.class, CONCAT_LUA_CONCAT_NODE_CONCAT_STATE_0_UPDATER.subUpdater(9, 9), ReferenceField.create(ConcatData.lookup_(), "concat_toTruffleStringNodeRight__field1_", Node.class), ReferenceField.create(ConcatData.lookup_(), "concat_toTruffleStringNodeRight__field2_", Node.class), ReferenceField.create(ConcatData.lookup_(), "concat_toTruffleStringNodeRight__field3_", Node.class)));
    private static final StateField FALLBACK_LUA_CONCAT_NODE_FALLBACK_STATE_0_UPDATER = StateField.create(FallbackData.lookup_(), "fallback_state_0_");
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaConcatNode#doGeneric}
     *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
     *   Inline method: {@link LuaToTruffleStringNodeGen#inline}</pre> */
    private static final LuaToTruffleStringNode INLINED_FALLBACK_TO_TRUFFLE_STRING_NODE_LEFT_ = LuaToTruffleStringNodeGen.inline(InlineTarget.create(LuaToTruffleStringNode.class, FALLBACK_LUA_CONCAT_NODE_FALLBACK_STATE_0_UPDATER.subUpdater(0, 9), ReferenceField.create(FallbackData.lookup_(), "fallback_toTruffleStringNodeLeft__field1_", Node.class), ReferenceField.create(FallbackData.lookup_(), "fallback_toTruffleStringNodeLeft__field2_", Node.class), ReferenceField.create(FallbackData.lookup_(), "fallback_toTruffleStringNodeLeft__field3_", Node.class)));
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaConcatNode#doGeneric}
     *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
     *   Inline method: {@link LuaToTruffleStringNodeGen#inline}</pre> */
    private static final LuaToTruffleStringNode INLINED_FALLBACK_TO_TRUFFLE_STRING_NODE_RIGHT_ = LuaToTruffleStringNodeGen.inline(InlineTarget.create(LuaToTruffleStringNode.class, FALLBACK_LUA_CONCAT_NODE_FALLBACK_STATE_0_UPDATER.subUpdater(9, 9), ReferenceField.create(FallbackData.lookup_(), "fallback_toTruffleStringNodeRight__field1_", Node.class), ReferenceField.create(FallbackData.lookup_(), "fallback_toTruffleStringNodeRight__field2_", Node.class), ReferenceField.create(FallbackData.lookup_(), "fallback_toTruffleStringNodeRight__field3_", Node.class)));

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaConcatNode#doConcat}
     *   1: SpecializationActive {@link LuaConcatNode#doGeneric}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private ConcatData concat_cache;
    @Child private FallbackData fallback_cache;

    private LuaConcatNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object leftNodeValue, Object rightNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaConcatNode.doConcat(TruffleString, TruffleString, Node, LuaToTruffleStringNode, LuaToTruffleStringNode, LuaMetatableNode, ConcatNode)] */) && leftNodeValue instanceof TruffleString && rightNodeValue instanceof TruffleString) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaConcatNode.doConcat(TruffleString, TruffleString, Node, LuaToTruffleStringNode, LuaToTruffleStringNode, LuaMetatableNode, ConcatNode)] || SpecializationActive[LuaConcatNode.doGeneric(Object, Object, Node, LuaToTruffleStringNode, LuaToTruffleStringNode, LuaMetatableNode, ConcatNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaConcatNode.doConcat(TruffleString, TruffleString, Node, LuaToTruffleStringNode, LuaToTruffleStringNode, LuaMetatableNode, ConcatNode)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    ConcatData s0_ = this.concat_cache;
                    if (s0_ != null) {
                        {
                            Node node__ = (s0_);
                            return LuaConcatNode.doConcat(leftNodeValue__, rightNodeValue__, node__, INLINED_CONCAT_TO_TRUFFLE_STRING_NODE_LEFT_, INLINED_CONCAT_TO_TRUFFLE_STRING_NODE_RIGHT_, s0_.metatableNode_, s0_.concatNode_);
                        }
                    }
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaConcatNode.doGeneric(Object, Object, Node, LuaToTruffleStringNode, LuaToTruffleStringNode, LuaMetatableNode, ConcatNode)] */) {
                FallbackData s1_ = this.fallback_cache;
                if (s1_ != null) {
                    {
                        Node node__1 = (s1_);
                        if (fallbackGuard_(state_0, leftNodeValue_, rightNodeValue_)) {
                            return LuaConcatNode.doGeneric(leftNodeValue_, rightNodeValue_, node__1, INLINED_FALLBACK_TO_TRUFFLE_STRING_NODE_LEFT_, INLINED_FALLBACK_TO_TRUFFLE_STRING_NODE_RIGHT_, s1_.metatableNode_, s1_.concatNode_);
                        }
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeGeneric(frameValue);
        return;
    }

    private Object executeAndSpecialize(Object leftNodeValue, Object rightNodeValue) {
        int state_0 = this.state_0_;
        {
            Node node__ = null;
            if (leftNodeValue instanceof TruffleString) {
                TruffleString leftNodeValue_ = (TruffleString) leftNodeValue;
                if (rightNodeValue instanceof TruffleString) {
                    TruffleString rightNodeValue_ = (TruffleString) rightNodeValue;
                    ConcatData s0_ = this.insert(new ConcatData());
                    node__ = (s0_);
                    s0_.metatableNode_ = s0_.insert((LuaMetatableNode.create()));
                    ConcatNode concatNode__ = s0_.insert((ConcatNode.create()));
                    Objects.requireNonNull(concatNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                    s0_.concatNode_ = concatNode__;
                    VarHandle.storeStoreFence();
                    this.concat_cache = s0_;
                    state_0 = state_0 | 0b1 /* add SpecializationActive[LuaConcatNode.doConcat(TruffleString, TruffleString, Node, LuaToTruffleStringNode, LuaToTruffleStringNode, LuaMetatableNode, ConcatNode)] */;
                    this.state_0_ = state_0;
                    return LuaConcatNode.doConcat(leftNodeValue_, rightNodeValue_, node__, INLINED_CONCAT_TO_TRUFFLE_STRING_NODE_LEFT_, INLINED_CONCAT_TO_TRUFFLE_STRING_NODE_RIGHT_, s0_.metatableNode_, concatNode__);
                }
            }
        }
        {
            Node node__1 = null;
            FallbackData s1_ = this.insert(new FallbackData());
            node__1 = (s1_);
            s1_.metatableNode_ = s1_.insert((LuaMetatableNode.create()));
            ConcatNode concatNode__1 = s1_.insert((ConcatNode.create()));
            Objects.requireNonNull(concatNode__1, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
            s1_.concatNode_ = concatNode__1;
            VarHandle.storeStoreFence();
            this.fallback_cache = s1_;
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaConcatNode.doGeneric(Object, Object, Node, LuaToTruffleStringNode, LuaToTruffleStringNode, LuaMetatableNode, ConcatNode)] */;
            this.state_0_ = state_0;
            return LuaConcatNode.doGeneric(leftNodeValue, rightNodeValue, node__1, INLINED_FALLBACK_TO_TRUFFLE_STRING_NODE_LEFT_, INLINED_FALLBACK_TO_TRUFFLE_STRING_NODE_RIGHT_, s1_.metatableNode_, concatNode__1);
        }
    }

    @NeverDefault
    public static LuaConcatNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaConcatNodeGen(leftNode, rightNode);
    }

    @GeneratedBy(LuaConcatNode.class)
    @DenyReplace
    private static final class ConcatData extends Node implements SpecializationDataNode {

        /**
         * State Info: <pre>
         *   0-8: InlinedCache
         *        Specialization: {@link LuaConcatNode#doConcat}
         *        Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
         *        Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   9-17: InlinedCache
         *        Specialization: {@link LuaConcatNode#doConcat}
         *        Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
         *        Inline method: {@link LuaToTruffleStringNodeGen#inline}
         * </pre> */
        @CompilationFinal @UnsafeAccessedField private int concat_state_0_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doConcat}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field1</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node concat_toTruffleStringNodeLeft__field1_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doConcat}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field2</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node concat_toTruffleStringNodeLeft__field2_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doConcat}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field3</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node concat_toTruffleStringNodeLeft__field3_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doConcat}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field1</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node concat_toTruffleStringNodeRight__field1_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doConcat}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field2</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node concat_toTruffleStringNodeRight__field2_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doConcat}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field3</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node concat_toTruffleStringNodeRight__field3_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doConcat}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doConcat}
         *   Parameter: {@link ConcatNode} concatNode</pre> */
        @Child ConcatNode concatNode_;

        ConcatData() {
        }

        private static Lookup lookup_() {
            return MethodHandles.lookup();
        }

    }
    @GeneratedBy(LuaConcatNode.class)
    @DenyReplace
    private static final class FallbackData extends Node implements SpecializationDataNode {

        /**
         * State Info: <pre>
         *   0-8: InlinedCache
         *        Specialization: {@link LuaConcatNode#doGeneric}
         *        Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
         *        Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   9-17: InlinedCache
         *        Specialization: {@link LuaConcatNode#doGeneric}
         *        Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
         *        Inline method: {@link LuaToTruffleStringNodeGen#inline}
         * </pre> */
        @CompilationFinal @UnsafeAccessedField private int fallback_state_0_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doGeneric}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field1</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node fallback_toTruffleStringNodeLeft__field1_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doGeneric}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field2</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node fallback_toTruffleStringNodeLeft__field2_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doGeneric}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeLeft
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field3</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node fallback_toTruffleStringNodeLeft__field3_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doGeneric}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field1</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node fallback_toTruffleStringNodeRight__field1_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doGeneric}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field2</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node fallback_toTruffleStringNodeRight__field2_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doGeneric}
         *   Parameter: {@link LuaToTruffleStringNode} toTruffleStringNodeRight
         *   Inline method: {@link LuaToTruffleStringNodeGen#inline}
         *   Inline field: {@link Node} field3</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node fallback_toTruffleStringNodeRight__field3_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doGeneric}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaConcatNode#doGeneric}
         *   Parameter: {@link ConcatNode} concatNode</pre> */
        @Child ConcatNode concatNode_;

        FallbackData() {
        }

        private static Lookup lookup_() {
            return MethodHandles.lookup();
        }

    }
}
