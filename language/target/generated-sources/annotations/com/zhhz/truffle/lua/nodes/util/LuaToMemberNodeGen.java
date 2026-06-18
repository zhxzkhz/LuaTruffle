// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.dsl.InlineSupport.InlineTarget;
import com.oracle.truffle.api.dsl.InlineSupport.ReferenceField;
import com.oracle.truffle.api.dsl.InlineSupport.RequiredField;
import com.oracle.truffle.api.dsl.InlineSupport.StateField;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.ToJavaStringNode;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaToMemberNode#fromString}
 *     Activation probability: 0.27381
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaToMemberNode#fromTruffleString}
 *     Activation probability: 0.23095
 *     With/without class size: 8/4 bytes
 *   Specialization {@link LuaToMemberNode#fromBoolean}
 *     Activation probability: 0.18810
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaToMemberNode#fromLong}
 *     Activation probability: 0.14524
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaToMemberNode#fromInterop}
 *     Activation probability: 0.10238
 *     With/without class size: 6/4 bytes
 *   Specialization {@link LuaToMemberNode#fromInterop}
 *     Activation probability: 0.05952
 *     With/without class size: 4/0 bytes
 * </pre> */
@GeneratedBy(LuaToMemberNode.class)
@SuppressWarnings({"javadoc", "unused"})
public final class LuaToMemberNodeGen {

    private static final Uncached UNCACHED = new Uncached();
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    @NeverDefault
    public static LuaToMemberNode getUncached() {
        return LuaToMemberNodeGen.UNCACHED;
    }

    /**
     * Required Fields: <ul>
     * <li>{@link Inlined#state_0_}
     * <li>{@link Inlined#fromTruffleString_toJavaStringNode_}
     * <li>{@link Inlined#fromInterop0_cache}
     * </ul> */
    @NeverDefault
    public static LuaToMemberNode inline(@RequiredField(bits = 6, value = StateField.class)@RequiredField(type = Node.class, value = ReferenceField.class)@RequiredField(type = Node.class, value = ReferenceField.class) InlineTarget target) {
        return new LuaToMemberNodeGen.Inlined(target);
    }

    @GeneratedBy(LuaToMemberNode.class)
    @DenyReplace
    private static final class Inlined extends LuaToMemberNode implements UnadoptableNode {

        /**
         * State Info: <pre>
         *   0: SpecializationActive {@link LuaToMemberNode#fromString}
         *   1: SpecializationActive {@link LuaToMemberNode#fromTruffleString}
         *   2: SpecializationActive {@link LuaToMemberNode#fromBoolean}
         *   3: SpecializationActive {@link LuaToMemberNode#fromLong}
         *   4: SpecializationActive {@link LuaToMemberNode#fromInterop}
         *   5: SpecializationActive {@link LuaToMemberNode#fromInterop}
         * </pre> */
        private final StateField state_0_;
        private final ReferenceField<ToJavaStringNode> fromTruffleString_toJavaStringNode_;
        private final ReferenceField<FromInterop0Data> fromInterop0_cache;

        @SuppressWarnings("unchecked")
        private Inlined(InlineTarget target) {
            assert target.getTargetClass().isAssignableFrom(LuaToMemberNode.class);
            this.state_0_ = target.getState(0, 6);
            this.fromTruffleString_toJavaStringNode_ = target.getReference(1, ToJavaStringNode.class);
            this.fromInterop0_cache = target.getReference(2, FromInterop0Data.class);
        }

        @ExplodeLoop
        @Override
        public String execute(Node arg0Value, Object arg1Value) throws UnknownIdentifierException {
            int state_0 = this.state_0_.get(arg0Value);
            if (state_0 != 0 /* is SpecializationActive[LuaToMemberNode.fromString(String)] || SpecializationActive[LuaToMemberNode.fromTruffleString(TruffleString, ToJavaStringNode)] || SpecializationActive[LuaToMemberNode.fromBoolean(boolean)] || SpecializationActive[LuaToMemberNode.fromLong(long)] || SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] || SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] */) {
                if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaToMemberNode.fromString(String)] */ && arg1Value instanceof String) {
                    String arg1Value_ = (String) arg1Value;
                    return LuaToMemberNode.fromString(arg1Value_);
                }
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaToMemberNode.fromTruffleString(TruffleString, ToJavaStringNode)] */ && arg1Value instanceof TruffleString) {
                    TruffleString arg1Value_ = (TruffleString) arg1Value;
                    {
                        ToJavaStringNode toJavaStringNode__ = this.fromTruffleString_toJavaStringNode_.get(arg0Value);
                        if (toJavaStringNode__ != null) {
                            return LuaToMemberNode.fromTruffleString(arg1Value_, toJavaStringNode__);
                        }
                    }
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaToMemberNode.fromBoolean(boolean)] */ && arg1Value instanceof Boolean) {
                    boolean arg1Value_ = (boolean) arg1Value;
                    return LuaToMemberNode.fromBoolean(arg1Value_);
                }
                if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaToMemberNode.fromLong(long)] */ && arg1Value instanceof Long) {
                    long arg1Value_ = (long) arg1Value;
                    return LuaToMemberNode.fromLong(arg1Value_);
                }
                if ((state_0 & 0b110000) != 0 /* is SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] || SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] */) {
                    if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] */) {
                        FromInterop0Data s4_ = this.fromInterop0_cache.get(arg0Value);
                        while (s4_ != null) {
                            if ((s4_.interop_.accepts(arg1Value))) {
                                return LuaToMemberNode.fromInterop(arg1Value, s4_.interop_);
                            }
                            s4_ = s4_.next_;
                        }
                    }
                    if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] */) {
                        return this.fromInterop1Boundary(state_0, arg0Value, arg1Value);
                    }
                }
            }
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(arg0Value, arg1Value);
        }

        @SuppressWarnings("static-method")
        @TruffleBoundary
        private String fromInterop1Boundary(int state_0, Node arg0Value, Object arg1Value) throws UnknownIdentifierException {
            EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
            Node prev_ = encapsulating_.set(arg0Value);
            try {
                {
                    InteropLibrary interop__ = (INTEROP_LIBRARY_.getUncached(arg1Value));
                    return LuaToMemberNode.fromInterop(arg1Value, interop__);
                }
            } finally {
                encapsulating_.set(prev_);
            }
        }

        private String executeAndSpecialize(Node arg0Value, Object arg1Value) throws UnknownIdentifierException {
            int state_0 = this.state_0_.get(arg0Value);
            if (arg1Value instanceof String) {
                String arg1Value_ = (String) arg1Value;
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaToMemberNode.fromString(String)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToMemberNode.fromString(arg1Value_);
            }
            if (arg1Value instanceof TruffleString) {
                TruffleString arg1Value_ = (TruffleString) arg1Value;
                ToJavaStringNode toJavaStringNode__ = arg0Value.insert((ToJavaStringNode.create()));
                Objects.requireNonNull(toJavaStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                VarHandle.storeStoreFence();
                this.fromTruffleString_toJavaStringNode_.set(arg0Value, toJavaStringNode__);
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaToMemberNode.fromTruffleString(TruffleString, ToJavaStringNode)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToMemberNode.fromTruffleString(arg1Value_, toJavaStringNode__);
            }
            if (arg1Value instanceof Boolean) {
                boolean arg1Value_ = (boolean) arg1Value;
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaToMemberNode.fromBoolean(boolean)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToMemberNode.fromBoolean(arg1Value_);
            }
            if (arg1Value instanceof Long) {
                long arg1Value_ = (long) arg1Value;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaToMemberNode.fromLong(long)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToMemberNode.fromLong(arg1Value_);
            }
            if (((state_0 & 0b100000)) == 0 /* is-not SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] */) {
                while (true) {
                    int count4_ = 0;
                    FromInterop0Data s4_ = this.fromInterop0_cache.getVolatile(arg0Value);
                    FromInterop0Data s4_original = s4_;
                    while (s4_ != null) {
                        if ((s4_.interop_.accepts(arg1Value))) {
                            break;
                        }
                        count4_++;
                        s4_ = s4_.next_;
                    }
                    if (s4_ == null) {
                        // assert (s4_.interop_.accepts(arg1Value));
                        if (count4_ < (LuaToMemberNode.LIMIT)) {
                            s4_ = arg0Value.insert(new FromInterop0Data(s4_original));
                            InteropLibrary interop__ = s4_.insert((INTEROP_LIBRARY_.create(arg1Value)));
                            Objects.requireNonNull(interop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                            s4_.interop_ = interop__;
                            if (!this.fromInterop0_cache.compareAndSet(arg0Value, s4_original, s4_)) {
                                continue;
                            }
                            state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] */;
                            this.state_0_.set(arg0Value, state_0);
                        }
                    }
                    if (s4_ != null) {
                        return LuaToMemberNode.fromInterop(arg1Value, s4_.interop_);
                    }
                    break;
                }
            }
            {
                InteropLibrary interop__ = null;
                {
                    EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
                    Node prev_ = encapsulating_.set(arg0Value);
                    try {
                        interop__ = (INTEROP_LIBRARY_.getUncached(arg1Value));
                        this.fromInterop0_cache.set(arg0Value, null);
                        state_0 = state_0 & 0xffffffef /* remove SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] */;
                        state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaToMemberNode.fromInterop(Object, InteropLibrary)] */;
                        this.state_0_.set(arg0Value, state_0);
                        return LuaToMemberNode.fromInterop(arg1Value, interop__);
                    } finally {
                        encapsulating_.set(prev_);
                    }
                }
            }
        }

    }
    @GeneratedBy(LuaToMemberNode.class)
    @DenyReplace
    private static final class FromInterop0Data extends Node implements SpecializationDataNode {

        @Child FromInterop0Data next_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaToMemberNode#fromInterop}
         *   Parameter: {@link InteropLibrary} interop</pre> */
        @Child InteropLibrary interop_;

        FromInterop0Data(FromInterop0Data next_) {
            this.next_ = next_;
        }

    }
    @GeneratedBy(LuaToMemberNode.class)
    @DenyReplace
    private static final class Uncached extends LuaToMemberNode implements UnadoptableNode {

        @TruffleBoundary
        @Override
        public String execute(Node arg0Value, Object arg1Value) throws UnknownIdentifierException {
            if (arg1Value instanceof String) {
                String arg1Value_ = (String) arg1Value;
                return LuaToMemberNode.fromString(arg1Value_);
            }
            if (arg1Value instanceof TruffleString) {
                TruffleString arg1Value_ = (TruffleString) arg1Value;
                return LuaToMemberNode.fromTruffleString(arg1Value_, (ToJavaStringNode.getUncached()));
            }
            if (arg1Value instanceof Boolean) {
                boolean arg1Value_ = (boolean) arg1Value;
                return LuaToMemberNode.fromBoolean(arg1Value_);
            }
            if (arg1Value instanceof Long) {
                long arg1Value_ = (long) arg1Value;
                return LuaToMemberNode.fromLong(arg1Value_);
            }
            return LuaToMemberNode.fromInterop(arg1Value, (INTEROP_LIBRARY_.getUncached(arg1Value)));
        }

    }
}
