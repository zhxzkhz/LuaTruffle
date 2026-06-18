// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.LibraryFactory;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaXpcallBuiltinNode#doXpcall}
 *     Activation probability: 1.00000
 *     With/without class size: 24/4 bytes
 * </pre> */
@GeneratedBy(LuaXpcallBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaXpcallBuiltinNodeGen extends LuaXpcallBuiltinNode {

    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaXpcallBuiltinNode#doXpcall}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaXpcallBuiltinNode#doXpcall}
     *   Parameter: {@link InteropLibrary} interop</pre> */
    @Child private InteropLibrary interop_;

    private LuaXpcallBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaXpcallBuiltinNode.doXpcall(Object[], InteropLibrary)] */) {
            {
                InteropLibrary interop__ = this.interop_;
                if (interop__ != null) {
                    return doXpcall(arg0Value, interop__);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Object executeAndSpecialize(Object[] arg0Value) {
        int state_0 = this.state_0_;
        InteropLibrary interop__ = this.insert((INTEROP_LIBRARY_.createDispatched(3)));
        Objects.requireNonNull(interop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.interop_ = interop__;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaXpcallBuiltinNode.doXpcall(Object[], InteropLibrary)] */;
        this.state_0_ = state_0;
        return doXpcall(arg0Value, interop__);
    }

    @NeverDefault
    public static LuaXpcallBuiltinNode create() {
        return new LuaXpcallBuiltinNodeGen();
    }

}
