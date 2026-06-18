// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaMetatableNode#doMetatableOp}
 *     Activation probability: 1.00000
 *     With/without class size: 28/8 bytes
 * </pre> */
@GeneratedBy(LuaMetatableNode.class)
@SuppressWarnings("javadoc")
public final class LuaMetatableNodeGen extends LuaMetatableNode {

    private static final Uncached UNCACHED = new Uncached();
    private static final LibraryFactory<DynamicObjectLibrary> DYNAMIC_OBJECT_LIBRARY_ = LibraryFactory.resolve(DynamicObjectLibrary.class);
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaMetatableNode#doMetatableOp}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaMetatableNode#doMetatableOp}
     *   Parameter: {@link DynamicObjectLibrary} objLib</pre> */
    @Child private DynamicObjectLibrary objLib_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaMetatableNode#doMetatableOp}
     *   Parameter: {@link InteropLibrary} interop</pre> */
    @Child private InteropLibrary interop_;

    private LuaMetatableNodeGen() {
    }

    @Override
    public Object execute(Object arg0Value, Object arg1Value, TruffleString arg2Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaMetatableNode.doMetatableOp(Object, Object, TruffleString, DynamicObjectLibrary, InteropLibrary)] */) {
            {
                DynamicObjectLibrary objLib__ = this.objLib_;
                if (objLib__ != null) {
                    InteropLibrary interop__ = this.interop_;
                    if (interop__ != null) {
                        return doMetatableOp(arg0Value, arg1Value, arg2Value, objLib__, interop__);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value, arg1Value, arg2Value);
    }

    private Object executeAndSpecialize(Object arg0Value, Object arg1Value, TruffleString arg2Value) {
        int state_0 = this.state_0_;
        DynamicObjectLibrary objLib__ = this.insert((DYNAMIC_OBJECT_LIBRARY_.createDispatched(3)));
        Objects.requireNonNull(objLib__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.objLib_ = objLib__;
        InteropLibrary interop__ = this.insert((INTEROP_LIBRARY_.createDispatched(3)));
        Objects.requireNonNull(interop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.interop_ = interop__;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaMetatableNode.doMetatableOp(Object, Object, TruffleString, DynamicObjectLibrary, InteropLibrary)] */;
        this.state_0_ = state_0;
        return doMetatableOp(arg0Value, arg1Value, arg2Value, objLib__, interop__);
    }

    @NeverDefault
    public static LuaMetatableNode create() {
        return new LuaMetatableNodeGen();
    }

    @NeverDefault
    public static LuaMetatableNode getUncached() {
        return LuaMetatableNodeGen.UNCACHED;
    }

    @GeneratedBy(LuaMetatableNode.class)
    @DenyReplace
    private static final class Uncached extends LuaMetatableNode implements UnadoptableNode {

        @TruffleBoundary
        @Override
        public Object execute(Object arg0Value, Object arg1Value, TruffleString arg2Value) {
            return doMetatableOp(arg0Value, arg1Value, arg2Value, (DYNAMIC_OBJECT_LIBRARY_.getUncached()), (INTEROP_LIBRARY_.getUncached()));
        }

    }
}
