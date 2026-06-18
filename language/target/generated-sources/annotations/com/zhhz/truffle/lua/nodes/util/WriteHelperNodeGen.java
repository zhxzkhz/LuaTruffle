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
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.zhhz.truffle.lua.runtime.LuaTable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link WriteHelperNode#writeNonTable}
 *     Activation probability: 0.65000
 *     With/without class size: 22/8 bytes
 *   Specialization {@link WriteHelperNode#writeNonTable}
 *     Activation probability: 0.35000
 *     With/without class size: 11/4 bytes
 * </pre> */
@GeneratedBy(WriteHelperNode.class)
@SuppressWarnings({"javadoc", "unused"})
public final class WriteHelperNodeGen extends WriteHelperNode {

    static final ReferenceField<WriteNonTable0Data> WRITE_NON_TABLE0_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "writeNonTable0_cache", WriteNonTable0Data.class);
    private static final LibraryFactory<DynamicObjectLibrary> DYNAMIC_OBJECT_LIBRARY_ = LibraryFactory.resolve(DynamicObjectLibrary.class);
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link WriteHelperNode#writeNonTable}
     *   1: SpecializationActive {@link WriteHelperNode#writeNonTable}
     * </pre> */
    @CompilationFinal private int state_0_;
    @UnsafeAccessedField @Child private WriteNonTable0Data writeNonTable0_cache;
    /**
     * Source Info: <pre>
     *   Specialization: {@link WriteHelperNode#writeNonTable}
     *   Parameter: {@link InteropLibrary} interop</pre> */
    @Child private InteropLibrary writeNonTable1_interop_;

    private WriteHelperNodeGen() {
    }

    @ExplodeLoop
    @Override
    public void executeWrite(Object arg0Value, Object arg1Value, Object arg2Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[WriteHelperNode.writeNonTable(LuaTable, Object, Object, DynamicObjectLibrary, InteropLibrary)] || SpecializationActive[WriteHelperNode.writeNonTable(LuaTable, Object, Object, DynamicObjectLibrary, InteropLibrary)] */ && arg0Value instanceof LuaTable) {
            LuaTable arg0Value_ = (LuaTable) arg0Value;
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[WriteHelperNode.writeNonTable(LuaTable, Object, Object, DynamicObjectLibrary, InteropLibrary)] */) {
                WriteNonTable0Data s0_ = this.writeNonTable0_cache;
                while (s0_ != null) {
                    if ((s0_.objLib_.accepts(arg0Value_))) {
                        writeNonTable(arg0Value_, arg1Value, arg2Value, s0_.objLib_, s0_.interop_);
                        return;
                    }
                    s0_ = s0_.next_;
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[WriteHelperNode.writeNonTable(LuaTable, Object, Object, DynamicObjectLibrary, InteropLibrary)] */) {
                {
                    InteropLibrary interop__ = this.writeNonTable1_interop_;
                    if (interop__ != null) {
                        this.writeNonTable1Boundary(state_0, arg0Value_, arg1Value, arg2Value, interop__);
                        return;
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        executeAndSpecialize(arg0Value, arg1Value, arg2Value);
        return;
    }

    @SuppressWarnings("static-method")
    @TruffleBoundary
    private void writeNonTable1Boundary(int state_0, LuaTable arg0Value_, Object arg1Value, Object arg2Value, InteropLibrary interop__) {
        {
            DynamicObjectLibrary objLib__ = (DYNAMIC_OBJECT_LIBRARY_.getUncached(arg0Value_));
            writeNonTable(arg0Value_, arg1Value, arg2Value, objLib__, interop__);
            return;
        }
    }

    private void executeAndSpecialize(Object arg0Value, Object arg1Value, Object arg2Value) {
        int state_0 = this.state_0_;
        if (arg0Value instanceof LuaTable) {
            LuaTable arg0Value_ = (LuaTable) arg0Value;
            if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[WriteHelperNode.writeNonTable(LuaTable, Object, Object, DynamicObjectLibrary, InteropLibrary)] */) {
                while (true) {
                    int count0_ = 0;
                    WriteNonTable0Data s0_ = WRITE_NON_TABLE0_CACHE_UPDATER.getVolatile(this);
                    WriteNonTable0Data s0_original = s0_;
                    while (s0_ != null) {
                        if ((s0_.objLib_.accepts(arg0Value_))) {
                            break;
                        }
                        count0_++;
                        s0_ = s0_.next_;
                    }
                    if (s0_ == null) {
                        // assert (s0_.objLib_.accepts(arg0Value_));
                        if (count0_ < (3)) {
                            s0_ = this.insert(new WriteNonTable0Data(s0_original));
                            DynamicObjectLibrary objLib__ = s0_.insert((DYNAMIC_OBJECT_LIBRARY_.create(arg0Value_)));
                            Objects.requireNonNull(objLib__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                            s0_.objLib_ = objLib__;
                            InteropLibrary interop__ = s0_.insert((INTEROP_LIBRARY_.createDispatched(3)));
                            Objects.requireNonNull(interop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                            s0_.interop_ = interop__;
                            if (!WRITE_NON_TABLE0_CACHE_UPDATER.compareAndSet(this, s0_original, s0_)) {
                                continue;
                            }
                            state_0 = state_0 | 0b1 /* add SpecializationActive[WriteHelperNode.writeNonTable(LuaTable, Object, Object, DynamicObjectLibrary, InteropLibrary)] */;
                            this.state_0_ = state_0;
                        }
                    }
                    if (s0_ != null) {
                        writeNonTable(arg0Value_, arg1Value, arg2Value, s0_.objLib_, s0_.interop_);
                        return;
                    }
                    break;
                }
            }
            {
                DynamicObjectLibrary objLib__ = null;
                objLib__ = (DYNAMIC_OBJECT_LIBRARY_.getUncached(arg0Value_));
                InteropLibrary interop__ = this.insert((INTEROP_LIBRARY_.createDispatched(3)));
                Objects.requireNonNull(interop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                VarHandle.storeStoreFence();
                this.writeNonTable1_interop_ = interop__;
                this.writeNonTable0_cache = null;
                state_0 = state_0 & 0xfffffffe /* remove SpecializationActive[WriteHelperNode.writeNonTable(LuaTable, Object, Object, DynamicObjectLibrary, InteropLibrary)] */;
                state_0 = state_0 | 0b10 /* add SpecializationActive[WriteHelperNode.writeNonTable(LuaTable, Object, Object, DynamicObjectLibrary, InteropLibrary)] */;
                this.state_0_ = state_0;
                writeNonTable(arg0Value_, arg1Value, arg2Value, objLib__, interop__);
                return;
            }
        }
        throw new UnsupportedSpecializationException(this, null, arg0Value, arg1Value, arg2Value);
    }

    @NeverDefault
    public static WriteHelperNode create() {
        return new WriteHelperNodeGen();
    }

    @GeneratedBy(WriteHelperNode.class)
    @DenyReplace
    private static final class WriteNonTable0Data extends Node implements SpecializationDataNode {

        @Child WriteNonTable0Data next_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link WriteHelperNode#writeNonTable}
         *   Parameter: {@link DynamicObjectLibrary} objLib</pre> */
        @Child DynamicObjectLibrary objLib_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link WriteHelperNode#writeNonTable}
         *   Parameter: {@link InteropLibrary} interop</pre> */
        @Child InteropLibrary interop_;

        WriteNonTable0Data(WriteNonTable0Data next_) {
            this.next_ = next_;
        }

    }
}
