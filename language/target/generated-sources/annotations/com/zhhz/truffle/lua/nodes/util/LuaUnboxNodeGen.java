// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.dsl.InlineSupport.ReferenceField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.FromJavaStringNode;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaTypes;
import com.zhhz.truffle.lua.nodes.LuaTypesGen;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaUnboxNode#fromString}
 *     Activation probability: 0.19111
 *     With/without class size: 7/4 bytes
 *   Specialization {@link LuaUnboxNode#fromTruffleString}
 *     Activation probability: 0.17111
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaUnboxNode#fromLong}
 *     Activation probability: 0.15111
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaUnboxNode#fromDouble}
 *     Activation probability: 0.13111
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaUnboxNode#fromFunction}
 *     Activation probability: 0.11111
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaUnboxNode#fromNil}
 *     Activation probability: 0.09111
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaUnboxNode#unboxNil}
 *     Activation probability: 0.07111
 *     With/without class size: 4/0 bytes
 *   Specialization {@link LuaUnboxNode#fromForeign}
 *     Activation probability: 0.05111
 *     With/without class size: 5/4 bytes
 *   Specialization {@link LuaUnboxNode#fromForeign}
 *     Activation probability: 0.03111
 *     With/without class size: 4/0 bytes
 * </pre> */
@GeneratedBy(LuaUnboxNode.class)
@SuppressWarnings({"javadoc", "unused"})
public final class LuaUnboxNodeGen extends LuaUnboxNode {

    static final ReferenceField<FromForeign0Data> FROM_FOREIGN0_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "fromForeign0_cache", FromForeign0Data.class);
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    @Child private LuaExpressionNode child0_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaUnboxNode#fromString}
     *   1: SpecializationActive {@link LuaUnboxNode#fromTruffleString}
     *   2: SpecializationActive {@link LuaUnboxNode#fromLong}
     *   3: SpecializationActive {@link LuaUnboxNode#fromDouble}
     *   4: SpecializationActive {@link LuaUnboxNode#fromFunction}
     *   5: SpecializationActive {@link LuaUnboxNode#fromNil}
     *   6: SpecializationActive {@link LuaUnboxNode#unboxNil}
     *   7: SpecializationActive {@link LuaUnboxNode#fromForeign}
     *   8: SpecializationActive {@link LuaUnboxNode#fromForeign}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaUnboxNode#fromString}
     *   Parameter: {@link FromJavaStringNode} fromJavaStringNode</pre> */
    @Child private FromJavaStringNode fromString_fromJavaStringNode_;
    @UnsafeAccessedField @Child private FromForeign0Data fromForeign0_cache;

    private LuaUnboxNodeGen(LuaExpressionNode child0) {
        this.child0_ = child0;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b111111011) == 0 /* only-active SpecializationActive[LuaUnboxNode.fromLong(long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaUnboxNode.fromString(String, FromJavaStringNode)] && SpecializationActive[LuaUnboxNode.fromTruffleString(TruffleString)] && SpecializationActive[LuaUnboxNode.fromLong(long)] && SpecializationActive[LuaUnboxNode.fromDouble(double)] && SpecializationActive[LuaUnboxNode.fromFunction(LuaFunction)] && SpecializationActive[LuaUnboxNode.fromNil(LuaNil)] && SpecializationActive[LuaUnboxNode.unboxNil(Object)] && SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] && SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */)) {
            return executeGeneric_long0(state_0, frameValue);
        } else {
            return executeGeneric_generic1(state_0, frameValue);
        }
    }

    private Object executeGeneric_long0(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        long child0Value_;
        try {
            child0Value_ = this.child0_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(ex.getResult());
        }
        assert (state_0 & 0b100) != 0 /* is SpecializationActive[LuaUnboxNode.fromLong(long)] */;
        return LuaUnboxNode.fromLong(child0Value_);
    }

    @SuppressWarnings("static-method")
    @TruffleBoundary
    private Object fromForeign1Boundary(int state_0, Object child0Value_) {
        EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
        Node prev_ = encapsulating_.set(this);
        try {
            {
                InteropLibrary interop__ = (INTEROP_LIBRARY_.getUncached(child0Value_));
                Node node__ = (this);
                return LuaUnboxNode.fromForeign(child0Value_, interop__, node__);
            }
        } finally {
            encapsulating_.set(prev_);
        }
    }

    @ExplodeLoop
    private Object executeGeneric_generic1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object child0Value_ = this.child0_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaUnboxNode.fromString(String, FromJavaStringNode)] || SpecializationActive[LuaUnboxNode.fromTruffleString(TruffleString)] || SpecializationActive[LuaUnboxNode.fromLong(long)] || SpecializationActive[LuaUnboxNode.fromDouble(double)] || SpecializationActive[LuaUnboxNode.fromFunction(LuaFunction)] || SpecializationActive[LuaUnboxNode.fromNil(LuaNil)] || SpecializationActive[LuaUnboxNode.unboxNil(Object)] || SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] || SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaUnboxNode.fromString(String, FromJavaStringNode)] */ && child0Value_ instanceof String) {
                String child0Value__ = (String) child0Value_;
                {
                    FromJavaStringNode fromJavaStringNode__ = this.fromString_fromJavaStringNode_;
                    if (fromJavaStringNode__ != null) {
                        return LuaUnboxNode.fromString(child0Value__, fromJavaStringNode__);
                    }
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaUnboxNode.fromTruffleString(TruffleString)] */ && child0Value_ instanceof TruffleString) {
                TruffleString child0Value__ = (TruffleString) child0Value_;
                return LuaUnboxNode.fromTruffleString(child0Value__);
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaUnboxNode.fromLong(long)] */ && child0Value_ instanceof Long) {
                long child0Value__ = (long) child0Value_;
                return LuaUnboxNode.fromLong(child0Value__);
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaUnboxNode.fromDouble(double)] */ && child0Value_ instanceof Double) {
                double child0Value__ = (double) child0Value_;
                return LuaUnboxNode.fromDouble(child0Value__);
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaUnboxNode.fromFunction(LuaFunction)] */ && child0Value_ instanceof LuaFunction) {
                LuaFunction child0Value__ = (LuaFunction) child0Value_;
                return LuaUnboxNode.fromFunction(child0Value__);
            }
            if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaUnboxNode.fromNil(LuaNil)] */ && LuaTypes.isLuaNil(child0Value_)) {
                LuaNil child0Value__ = LuaTypes.asLuaNil(child0Value_);
                return LuaUnboxNode.fromNil(child0Value__);
            }
            if ((state_0 & 0b111000000) != 0 /* is SpecializationActive[LuaUnboxNode.unboxNil(Object)] || SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] || SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */) {
                if ((state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaUnboxNode.unboxNil(Object)] */) {
                    if ((child0Value_ == null)) {
                        return LuaUnboxNode.unboxNil(child0Value_);
                    }
                }
                if ((state_0 & 0b10000000) != 0 /* is SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */) {
                    FromForeign0Data s7_ = this.fromForeign0_cache;
                    while (s7_ != null) {
                        if ((s7_.interop_.accepts(child0Value_)) && (child0Value_ != null)) {
                            Node node__ = (s7_);
                            return LuaUnboxNode.fromForeign(child0Value_, s7_.interop_, node__);
                        }
                        s7_ = s7_.next_;
                    }
                }
                if ((state_0 & 0b100000000) != 0 /* is SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */) {
                    if ((child0Value_ != null)) {
                        return this.fromForeign1Boundary(state_0, child0Value_);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(child0Value_);
    }

    @Override
    public long executeLong(VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = this.state_0_;
        if ((state_0 & 0b111000000) != 0 /* is SpecializationActive[LuaUnboxNode.unboxNil(Object)] || SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] || SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */) {
            return LuaTypesGen.expectLong(this.executeGeneric(frameValue));
        }
        long child0Value_;
        try {
            child0Value_ = this.child0_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return LuaTypesGen.expectLong(executeAndSpecialize(ex.getResult()));
        }
        if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaUnboxNode.fromLong(long)] */) {
            return LuaUnboxNode.fromLong(child0Value_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectLong(executeAndSpecialize(child0Value_));
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        try {
            if ((state_0 & 0b111111011) == 0 /* only-active SpecializationActive[LuaUnboxNode.fromLong(long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaUnboxNode.fromString(String, FromJavaStringNode)] && SpecializationActive[LuaUnboxNode.fromTruffleString(TruffleString)] && SpecializationActive[LuaUnboxNode.fromLong(long)] && SpecializationActive[LuaUnboxNode.fromDouble(double)] && SpecializationActive[LuaUnboxNode.fromFunction(LuaFunction)] && SpecializationActive[LuaUnboxNode.fromNil(LuaNil)] && SpecializationActive[LuaUnboxNode.unboxNil(Object)] && SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] && SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */)) {
                this.executeLong(frameValue);
                return;
            }
            this.executeGeneric(frameValue);
            return;
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return;
        }
    }

    private Object executeAndSpecialize(Object child0Value) {
        int state_0 = this.state_0_;
        if (child0Value instanceof String) {
            String child0Value_ = (String) child0Value;
            FromJavaStringNode fromJavaStringNode__ = this.insert((FromJavaStringNode.create()));
            Objects.requireNonNull(fromJavaStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
            VarHandle.storeStoreFence();
            this.fromString_fromJavaStringNode_ = fromJavaStringNode__;
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaUnboxNode.fromString(String, FromJavaStringNode)] */;
            this.state_0_ = state_0;
            return LuaUnboxNode.fromString(child0Value_, fromJavaStringNode__);
        }
        if (child0Value instanceof TruffleString) {
            TruffleString child0Value_ = (TruffleString) child0Value;
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaUnboxNode.fromTruffleString(TruffleString)] */;
            this.state_0_ = state_0;
            return LuaUnboxNode.fromTruffleString(child0Value_);
        }
        if (child0Value instanceof Long) {
            long child0Value_ = (long) child0Value;
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaUnboxNode.fromLong(long)] */;
            this.state_0_ = state_0;
            return LuaUnboxNode.fromLong(child0Value_);
        }
        if (child0Value instanceof Double) {
            double child0Value_ = (double) child0Value;
            state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaUnboxNode.fromDouble(double)] */;
            this.state_0_ = state_0;
            return LuaUnboxNode.fromDouble(child0Value_);
        }
        if (child0Value instanceof LuaFunction) {
            LuaFunction child0Value_ = (LuaFunction) child0Value;
            state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaUnboxNode.fromFunction(LuaFunction)] */;
            this.state_0_ = state_0;
            return LuaUnboxNode.fromFunction(child0Value_);
        }
        if (LuaTypes.isLuaNil(child0Value)) {
            LuaNil child0Value_ = LuaTypes.asLuaNil(child0Value);
            state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaUnboxNode.fromNil(LuaNil)] */;
            this.state_0_ = state_0;
            return LuaUnboxNode.fromNil(child0Value_);
        }
        if ((child0Value == null)) {
            state_0 = state_0 | 0b1000000 /* add SpecializationActive[LuaUnboxNode.unboxNil(Object)] */;
            this.state_0_ = state_0;
            return LuaUnboxNode.unboxNil(child0Value);
        }
        {
            Node node__ = null;
            if (((state_0 & 0b100000000)) == 0 /* is-not SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */) {
                while (true) {
                    int count7_ = 0;
                    FromForeign0Data s7_ = FROM_FOREIGN0_CACHE_UPDATER.getVolatile(this);
                    FromForeign0Data s7_original = s7_;
                    while (s7_ != null) {
                        if ((s7_.interop_.accepts(child0Value)) && (child0Value != null)) {
                            node__ = (s7_);
                            break;
                        }
                        count7_++;
                        s7_ = s7_.next_;
                    }
                    if (s7_ == null) {
                        if ((child0Value != null) && count7_ < (LuaUnboxNode.LIMIT)) {
                            // assert (s7_.interop_.accepts(child0Value));
                            s7_ = this.insert(new FromForeign0Data(s7_original));
                            InteropLibrary interop__ = s7_.insert((INTEROP_LIBRARY_.create(child0Value)));
                            Objects.requireNonNull(interop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                            s7_.interop_ = interop__;
                            node__ = (s7_);
                            if (!FROM_FOREIGN0_CACHE_UPDATER.compareAndSet(this, s7_original, s7_)) {
                                continue;
                            }
                            state_0 = state_0 | 0b10000000 /* add SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */;
                            this.state_0_ = state_0;
                        }
                    }
                    if (s7_ != null) {
                        return LuaUnboxNode.fromForeign(child0Value, s7_.interop_, node__);
                    }
                    break;
                }
            }
        }
        {
            Node node__ = null;
            InteropLibrary interop__ = null;
            {
                EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
                Node prev_ = encapsulating_.set(this);
                try {
                    if ((child0Value != null)) {
                        interop__ = (INTEROP_LIBRARY_.getUncached(child0Value));
                        node__ = (this);
                        this.fromForeign0_cache = null;
                        state_0 = state_0 & 0xffffff7f /* remove SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */;
                        state_0 = state_0 | 0b100000000 /* add SpecializationActive[LuaUnboxNode.fromForeign(Object, InteropLibrary, Node)] */;
                        this.state_0_ = state_0;
                        return LuaUnboxNode.fromForeign(child0Value, interop__, node__);
                    }
                } finally {
                    encapsulating_.set(prev_);
                }
            }
        }
        throw new UnsupportedSpecializationException(this, new Node[] {this.child0_}, child0Value);
    }

    @NeverDefault
    public static LuaUnboxNode create(LuaExpressionNode child0) {
        return new LuaUnboxNodeGen(child0);
    }

    @GeneratedBy(LuaUnboxNode.class)
    @DenyReplace
    private static final class FromForeign0Data extends Node implements SpecializationDataNode {

        @Child FromForeign0Data next_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaUnboxNode#fromForeign}
         *   Parameter: {@link InteropLibrary} interop</pre> */
        @Child InteropLibrary interop_;

        FromForeign0Data(FromForeign0Data next_) {
            this.next_ = next_;
        }

    }
}
