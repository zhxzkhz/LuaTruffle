// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaRawGetBuiltinNode#doRawGet}
 *     Activation probability: 1.00000
 *     With/without class size: 24/4 bytes
 * </pre> */
@GeneratedBy(LuaRawGetBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaRawGetBuiltinNodeGen extends LuaRawGetBuiltinNode {

    private static final LibraryFactory<DynamicObjectLibrary> DYNAMIC_OBJECT_LIBRARY_ = LibraryFactory.resolve(DynamicObjectLibrary.class);

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaRawGetBuiltinNode#doRawGet}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaRawGetBuiltinNode#doRawGet}
     *   Parameter: {@link DynamicObjectLibrary} objLib</pre> */
    @Child private DynamicObjectLibrary objLib_;

    private LuaRawGetBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaRawGetBuiltinNode.doRawGet(Object[], DynamicObjectLibrary)] */) {
            {
                DynamicObjectLibrary objLib__ = this.objLib_;
                if (objLib__ != null) {
                    return doRawGet(arg0Value, objLib__);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Object executeAndSpecialize(Object[] arg0Value) {
        int state_0 = this.state_0_;
        DynamicObjectLibrary objLib__ = this.insert((DYNAMIC_OBJECT_LIBRARY_.createDispatched(3)));
        Objects.requireNonNull(objLib__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.objLib_ = objLib__;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaRawGetBuiltinNode.doRawGet(Object[], DynamicObjectLibrary)] */;
        this.state_0_ = state_0;
        return doRawGet(arg0Value, objLib__);
    }

    @NeverDefault
    public static LuaRawGetBuiltinNode create() {
        return new LuaRawGetBuiltinNodeGen();
    }

}
