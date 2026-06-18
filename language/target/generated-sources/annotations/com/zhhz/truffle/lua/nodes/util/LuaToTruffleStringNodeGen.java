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
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.FromJavaStringNode;
import com.oracle.truffle.api.strings.TruffleString.FromLongNode;
import com.zhhz.truffle.lua.nodes.LuaTypes;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaString;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaToTruffleStringNode#fromNull}
 *     Activation probability: 0.19111
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaToTruffleStringNode#fromString}
 *     Activation probability: 0.17111
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaToTruffleStringNode#fromTruffleString(TruffleString)}
 *     Activation probability: 0.15111
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaToTruffleStringNode#fromTruffleString(LuaString)}
 *     Activation probability: 0.13111
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaToTruffleStringNode#fromBoolean}
 *     Activation probability: 0.11111
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaToTruffleStringNode#fromLong}
 *     Activation probability: 0.09111
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaToTruffleStringNode#fromFunction}
 *     Activation probability: 0.07111
 *     With/without class size: 4/0 bytes
 *   Specialization {@link LuaToTruffleStringNode#fromInterop}
 *     Activation probability: 0.05111
 *     With/without class size: 5/4 bytes
 *   Specialization {@link LuaToTruffleStringNode#fromInterop}
 *     Activation probability: 0.03111
 *     With/without class size: 4/0 bytes
 * </pre> */
@GeneratedBy(LuaToTruffleStringNode.class)
@SuppressWarnings({"javadoc", "unused"})
public final class LuaToTruffleStringNodeGen {

    private static final Uncached UNCACHED = new Uncached();
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    @NeverDefault
    public static LuaToTruffleStringNode getUncached() {
        return LuaToTruffleStringNodeGen.UNCACHED;
    }

    /**
     * Required Fields: <ul>
     * <li>{@link Inlined#state_0_}
     * <li>{@link Inlined#fromJava}
     * <li>{@link Inlined#fromLong}
     * <li>{@link Inlined#fromInterop0_cache}
     * </ul> */
    @NeverDefault
    public static LuaToTruffleStringNode inline(@RequiredField(bits = 9, value = StateField.class)@RequiredField(type = Node.class, value = ReferenceField.class)@RequiredField(type = Node.class, value = ReferenceField.class)@RequiredField(type = Node.class, value = ReferenceField.class) InlineTarget target) {
        return new LuaToTruffleStringNodeGen.Inlined(target);
    }

    @GeneratedBy(LuaToTruffleStringNode.class)
    @DenyReplace
    private static final class Inlined extends LuaToTruffleStringNode implements UnadoptableNode {

        /**
         * State Info: <pre>
         *   0: SpecializationActive {@link LuaToTruffleStringNode#fromNull}
         *   1: SpecializationActive {@link LuaToTruffleStringNode#fromString}
         *   2: SpecializationActive {@link LuaToTruffleStringNode#fromTruffleString(TruffleString)}
         *   3: SpecializationActive {@link LuaToTruffleStringNode#fromTruffleString(LuaString)}
         *   4: SpecializationActive {@link LuaToTruffleStringNode#fromBoolean}
         *   5: SpecializationActive {@link LuaToTruffleStringNode#fromLong}
         *   6: SpecializationActive {@link LuaToTruffleStringNode#fromFunction}
         *   7: SpecializationActive {@link LuaToTruffleStringNode#fromInterop}
         *   8: SpecializationActive {@link LuaToTruffleStringNode#fromInterop}
         * </pre> */
        private final StateField state_0_;
        private final ReferenceField<FromJavaStringNode> fromJava;
        private final ReferenceField<FromLongNode> fromLong;
        private final ReferenceField<FromInterop0Data> fromInterop0_cache;

        @SuppressWarnings("unchecked")
        private Inlined(InlineTarget target) {
            assert target.getTargetClass().isAssignableFrom(LuaToTruffleStringNode.class);
            this.state_0_ = target.getState(0, 9);
            this.fromJava = target.getReference(1, FromJavaStringNode.class);
            this.fromLong = target.getReference(2, FromLongNode.class);
            this.fromInterop0_cache = target.getReference(3, FromInterop0Data.class);
        }

        @ExplodeLoop
        @Override
        public TruffleString execute(Node arg0Value, Object arg1Value) {
            int state_0 = this.state_0_.get(arg0Value);
            if (state_0 != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromNull(LuaNil)] || SpecializationActive[LuaToTruffleStringNode.fromString(String, FromJavaStringNode)] || SpecializationActive[LuaToTruffleStringNode.fromTruffleString(TruffleString)] || SpecializationActive[LuaToTruffleStringNode.fromTruffleString(LuaString)] || SpecializationActive[LuaToTruffleStringNode.fromBoolean(boolean)] || SpecializationActive[LuaToTruffleStringNode.fromLong(long, FromLongNode)] || SpecializationActive[LuaToTruffleStringNode.fromFunction(LuaFunction)] || SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] || SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] */) {
                if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromNull(LuaNil)] */ && LuaTypes.isLuaNil(arg1Value)) {
                    LuaNil arg1Value_ = LuaTypes.asLuaNil(arg1Value);
                    return LuaToTruffleStringNode.fromNull(arg1Value_);
                }
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromString(String, FromJavaStringNode)] */ && arg1Value instanceof String) {
                    String arg1Value_ = (String) arg1Value;
                    {
                        FromJavaStringNode fromJava_ = this.fromJava.get(arg0Value);
                        if (fromJava_ != null) {
                            return LuaToTruffleStringNode.fromString(arg1Value_, fromJava_);
                        }
                    }
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromTruffleString(TruffleString)] */ && arg1Value instanceof TruffleString) {
                    TruffleString arg1Value_ = (TruffleString) arg1Value;
                    return LuaToTruffleStringNode.fromTruffleString(arg1Value_);
                }
                if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromTruffleString(LuaString)] */ && arg1Value instanceof LuaString) {
                    LuaString arg1Value_ = (LuaString) arg1Value;
                    return LuaToTruffleStringNode.fromTruffleString(arg1Value_);
                }
                if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromBoolean(boolean)] */ && arg1Value instanceof Boolean) {
                    boolean arg1Value_ = (boolean) arg1Value;
                    return LuaToTruffleStringNode.fromBoolean(arg1Value_);
                }
                if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromLong(long, FromLongNode)] */ && arg1Value instanceof Long) {
                    long arg1Value_ = (long) arg1Value;
                    {
                        FromLongNode fromLong_ = this.fromLong.get(arg0Value);
                        if (fromLong_ != null) {
                            return LuaToTruffleStringNode.fromLong(arg1Value_, fromLong_);
                        }
                    }
                }
                if ((state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromFunction(LuaFunction)] */ && arg1Value instanceof LuaFunction) {
                    LuaFunction arg1Value_ = (LuaFunction) arg1Value;
                    return LuaToTruffleStringNode.fromFunction(arg1Value_);
                }
                if ((state_0 & 0b110000000) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] || SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] */) {
                    if ((state_0 & 0b10000000) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] */) {
                        FromInterop0Data s7_ = this.fromInterop0_cache.get(arg0Value);
                        while (s7_ != null) {
                            {
                                FromLongNode fromLong_1 = this.fromLong.get(arg0Value);
                                if (fromLong_1 != null) {
                                    FromJavaStringNode fromJava_1 = this.fromJava.get(arg0Value);
                                    if (fromJava_1 != null) {
                                        if ((s7_.interop_.accepts(arg1Value))) {
                                            return LuaToTruffleStringNode.fromInterop(arg1Value, s7_.interop_, fromLong_1, fromJava_1);
                                        }
                                    }
                                }
                            }
                            s7_ = s7_.next_;
                        }
                    }
                    if ((state_0 & 0b100000000) != 0 /* is SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] */) {
                        {
                            FromLongNode fromLong_1 = this.fromLong.get(arg0Value);
                            if (fromLong_1 != null) {
                                FromJavaStringNode fromJava_1 = this.fromJava.get(arg0Value);
                                if (fromJava_1 != null) {
                                    return this.fromInterop1Boundary(state_0, arg0Value, arg1Value, fromLong_1, fromJava_1);
                                }
                            }
                        }
                    }
                }
            }
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(arg0Value, arg1Value);
        }

        @SuppressWarnings("static-method")
        @TruffleBoundary
        private TruffleString fromInterop1Boundary(int state_0, Node arg0Value, Object arg1Value, FromLongNode fromLong_1, FromJavaStringNode fromJava_1) {
            EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
            Node prev_ = encapsulating_.set(arg0Value);
            try {
                {
                    InteropLibrary interop__ = (INTEROP_LIBRARY_.getUncached(arg1Value));
                    return LuaToTruffleStringNode.fromInterop(arg1Value, interop__, fromLong_1, fromJava_1);
                }
            } finally {
                encapsulating_.set(prev_);
            }
        }

        private TruffleString executeAndSpecialize(Node arg0Value, Object arg1Value) {
            int state_0 = this.state_0_.get(arg0Value);
            if (LuaTypes.isLuaNil(arg1Value)) {
                LuaNil arg1Value_ = LuaTypes.asLuaNil(arg1Value);
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaToTruffleStringNode.fromNull(LuaNil)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToTruffleStringNode.fromNull(arg1Value_);
            }
            if (arg1Value instanceof String) {
                String arg1Value_ = (String) arg1Value;
                FromJavaStringNode fromJava_;
                FromJavaStringNode fromJava__shared = this.fromJava.get(arg0Value);
                if (fromJava__shared != null) {
                    fromJava_ = fromJava__shared;
                } else {
                    fromJava_ = arg0Value.insert((FromJavaStringNode.create()));
                    if (fromJava_ == null) {
                        throw new IllegalStateException("A specialization returned a default value for a cached initializer. Default values are not supported for shared cached initializers because the default value is reserved for the uninitialized state.");
                    }
                }
                if (this.fromJava.get(arg0Value) == null) {
                    VarHandle.storeStoreFence();
                    this.fromJava.set(arg0Value, fromJava_);
                }
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaToTruffleStringNode.fromString(String, FromJavaStringNode)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToTruffleStringNode.fromString(arg1Value_, fromJava_);
            }
            if (arg1Value instanceof TruffleString) {
                TruffleString arg1Value_ = (TruffleString) arg1Value;
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaToTruffleStringNode.fromTruffleString(TruffleString)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToTruffleStringNode.fromTruffleString(arg1Value_);
            }
            if (arg1Value instanceof LuaString) {
                LuaString arg1Value_ = (LuaString) arg1Value;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaToTruffleStringNode.fromTruffleString(LuaString)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToTruffleStringNode.fromTruffleString(arg1Value_);
            }
            if (arg1Value instanceof Boolean) {
                boolean arg1Value_ = (boolean) arg1Value;
                state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaToTruffleStringNode.fromBoolean(boolean)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToTruffleStringNode.fromBoolean(arg1Value_);
            }
            if (arg1Value instanceof Long) {
                long arg1Value_ = (long) arg1Value;
                FromLongNode fromLong_;
                FromLongNode fromLong__shared = this.fromLong.get(arg0Value);
                if (fromLong__shared != null) {
                    fromLong_ = fromLong__shared;
                } else {
                    fromLong_ = arg0Value.insert((FromLongNode.create()));
                    if (fromLong_ == null) {
                        throw new IllegalStateException("A specialization returned a default value for a cached initializer. Default values are not supported for shared cached initializers because the default value is reserved for the uninitialized state.");
                    }
                }
                if (this.fromLong.get(arg0Value) == null) {
                    VarHandle.storeStoreFence();
                    this.fromLong.set(arg0Value, fromLong_);
                }
                state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaToTruffleStringNode.fromLong(long, FromLongNode)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToTruffleStringNode.fromLong(arg1Value_, fromLong_);
            }
            if (arg1Value instanceof LuaFunction) {
                LuaFunction arg1Value_ = (LuaFunction) arg1Value;
                state_0 = state_0 | 0b1000000 /* add SpecializationActive[LuaToTruffleStringNode.fromFunction(LuaFunction)] */;
                this.state_0_.set(arg0Value, state_0);
                return LuaToTruffleStringNode.fromFunction(arg1Value_);
            }
            if (((state_0 & 0b100000000)) == 0 /* is-not SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] */) {
                while (true) {
                    int count7_ = 0;
                    FromInterop0Data s7_ = this.fromInterop0_cache.getVolatile(arg0Value);
                    FromInterop0Data s7_original = s7_;
                    while (s7_ != null) {
                        {
                            FromLongNode fromLong_1 = this.fromLong.get(arg0Value);
                            if (fromLong_1 != null) {
                                FromJavaStringNode fromJava_1 = this.fromJava.get(arg0Value);
                                if (fromJava_1 != null) {
                                    if ((s7_.interop_.accepts(arg1Value))) {
                                        break;
                                    }
                                }
                            }
                        }
                        count7_++;
                        s7_ = s7_.next_;
                    }
                    if (s7_ == null) {
                        // assert (s7_.interop_.accepts(arg1Value));
                        if (count7_ < (LuaToTruffleStringNode.LIMIT)) {
                            s7_ = arg0Value.insert(new FromInterop0Data(s7_original));
                            InteropLibrary interop__ = s7_.insert((INTEROP_LIBRARY_.create(arg1Value)));
                            Objects.requireNonNull(interop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                            s7_.interop_ = interop__;
                            FromLongNode fromLong_1;
                            FromLongNode fromLong_1_shared = this.fromLong.get(arg0Value);
                            if (fromLong_1_shared != null) {
                                fromLong_1 = fromLong_1_shared;
                            } else {
                                fromLong_1 = s7_.insert((FromLongNode.create()));
                                if (fromLong_1 == null) {
                                    throw new IllegalStateException("A specialization returned a default value for a cached initializer. Default values are not supported for shared cached initializers because the default value is reserved for the uninitialized state.");
                                }
                            }
                            if (this.fromLong.get(arg0Value) == null) {
                                this.fromLong.set(arg0Value, fromLong_1);
                            }
                            FromJavaStringNode fromJava_1;
                            FromJavaStringNode fromJava_1_shared = this.fromJava.get(arg0Value);
                            if (fromJava_1_shared != null) {
                                fromJava_1 = fromJava_1_shared;
                            } else {
                                fromJava_1 = s7_.insert((FromJavaStringNode.create()));
                                if (fromJava_1 == null) {
                                    throw new IllegalStateException("A specialization returned a default value for a cached initializer. Default values are not supported for shared cached initializers because the default value is reserved for the uninitialized state.");
                                }
                            }
                            if (this.fromJava.get(arg0Value) == null) {
                                this.fromJava.set(arg0Value, fromJava_1);
                            }
                            if (!this.fromInterop0_cache.compareAndSet(arg0Value, s7_original, s7_)) {
                                continue;
                            }
                            state_0 = state_0 | 0b10000000 /* add SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] */;
                            this.state_0_.set(arg0Value, state_0);
                        }
                    }
                    if (s7_ != null) {
                        return LuaToTruffleStringNode.fromInterop(arg1Value, s7_.interop_, this.fromLong.get(arg0Value), this.fromJava.get(arg0Value));
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
                        FromLongNode fromLong_1;
                        FromLongNode fromLong_1_shared = this.fromLong.get(arg0Value);
                        if (fromLong_1_shared != null) {
                            fromLong_1 = fromLong_1_shared;
                        } else {
                            fromLong_1 = arg0Value.insert((FromLongNode.create()));
                            if (fromLong_1 == null) {
                                throw new IllegalStateException("A specialization returned a default value for a cached initializer. Default values are not supported for shared cached initializers because the default value is reserved for the uninitialized state.");
                            }
                        }
                        if (this.fromLong.get(arg0Value) == null) {
                            VarHandle.storeStoreFence();
                            this.fromLong.set(arg0Value, fromLong_1);
                        }
                        FromJavaStringNode fromJava_1;
                        FromJavaStringNode fromJava_1_shared = this.fromJava.get(arg0Value);
                        if (fromJava_1_shared != null) {
                            fromJava_1 = fromJava_1_shared;
                        } else {
                            fromJava_1 = arg0Value.insert((FromJavaStringNode.create()));
                            if (fromJava_1 == null) {
                                throw new IllegalStateException("A specialization returned a default value for a cached initializer. Default values are not supported for shared cached initializers because the default value is reserved for the uninitialized state.");
                            }
                        }
                        if (this.fromJava.get(arg0Value) == null) {
                            VarHandle.storeStoreFence();
                            this.fromJava.set(arg0Value, fromJava_1);
                        }
                        this.fromInterop0_cache.set(arg0Value, null);
                        state_0 = state_0 & 0xffffff7f /* remove SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] */;
                        state_0 = state_0 | 0b100000000 /* add SpecializationActive[LuaToTruffleStringNode.fromInterop(Object, InteropLibrary, FromLongNode, FromJavaStringNode)] */;
                        this.state_0_.set(arg0Value, state_0);
                        return LuaToTruffleStringNode.fromInterop(arg1Value, interop__, fromLong_1, fromJava_1);
                    } finally {
                        encapsulating_.set(prev_);
                    }
                }
            }
        }

    }
    @GeneratedBy(LuaToTruffleStringNode.class)
    @DenyReplace
    private static final class FromInterop0Data extends Node implements SpecializationDataNode {

        @Child FromInterop0Data next_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaToTruffleStringNode#fromInterop}
         *   Parameter: {@link InteropLibrary} interop</pre> */
        @Child InteropLibrary interop_;

        FromInterop0Data(FromInterop0Data next_) {
            this.next_ = next_;
        }

    }
    @GeneratedBy(LuaToTruffleStringNode.class)
    @DenyReplace
    private static final class Uncached extends LuaToTruffleStringNode implements UnadoptableNode {

        @TruffleBoundary
        @Override
        public TruffleString execute(Node arg0Value, Object arg1Value) {
            if (LuaTypes.isLuaNil(arg1Value)) {
                LuaNil arg1Value_ = LuaTypes.asLuaNil(arg1Value);
                return LuaToTruffleStringNode.fromNull(arg1Value_);
            }
            if (arg1Value instanceof String) {
                String arg1Value_ = (String) arg1Value;
                return LuaToTruffleStringNode.fromString(arg1Value_, (FromJavaStringNode.getUncached()));
            }
            if (arg1Value instanceof TruffleString) {
                TruffleString arg1Value_ = (TruffleString) arg1Value;
                return LuaToTruffleStringNode.fromTruffleString(arg1Value_);
            }
            if (arg1Value instanceof LuaString) {
                LuaString arg1Value_ = (LuaString) arg1Value;
                return LuaToTruffleStringNode.fromTruffleString(arg1Value_);
            }
            if (arg1Value instanceof Boolean) {
                boolean arg1Value_ = (boolean) arg1Value;
                return LuaToTruffleStringNode.fromBoolean(arg1Value_);
            }
            if (arg1Value instanceof Long) {
                long arg1Value_ = (long) arg1Value;
                return LuaToTruffleStringNode.fromLong(arg1Value_, (FromLongNode.getUncached()));
            }
            if (arg1Value instanceof LuaFunction) {
                LuaFunction arg1Value_ = (LuaFunction) arg1Value;
                return LuaToTruffleStringNode.fromFunction(arg1Value_);
            }
            return LuaToTruffleStringNode.fromInterop(arg1Value, (INTEROP_LIBRARY_.getUncached(arg1Value)), (FromLongNode.getUncached()), (FromJavaStringNode.getUncached()));
        }

    }
}
