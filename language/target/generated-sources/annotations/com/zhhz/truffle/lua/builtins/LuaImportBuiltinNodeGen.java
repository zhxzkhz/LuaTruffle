// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.strings.TruffleString.ToJavaStringNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaImportBuiltin#importSymbol}
 *     Activation probability: 1.00000
 *     With/without class size: 28/8 bytes
 * </pre> */
@GeneratedBy(LuaImportBuiltin.class)
@SuppressWarnings("javadoc")
public final class LuaImportBuiltinNodeGen extends LuaImportBuiltin {

    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaImportBuiltin#importSymbol}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaImportBuiltin#importSymbol}
     *   Parameter: {@link ToJavaStringNode} toJavaStringNode</pre> */
    @Child private ToJavaStringNode toJavaStringNode_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaImportBuiltin#importSymbol}
     *   Parameter: {@link InteropLibrary} arrays</pre> */
    @Child private InteropLibrary arrays_;

    private LuaImportBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaImportBuiltin.importSymbol(Object[], ToJavaStringNode, InteropLibrary, LuaContext)] */) {
            {
                ToJavaStringNode toJavaStringNode__ = this.toJavaStringNode_;
                if (toJavaStringNode__ != null) {
                    InteropLibrary arrays__ = this.arrays_;
                    if (arrays__ != null) {
                        LuaContext context__ = (LuaContext.get(this));
                        return importSymbol(arg0Value, toJavaStringNode__, arrays__, context__);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Object executeAndSpecialize(Object[] arg0Value) {
        int state_0 = this.state_0_;
        {
            LuaContext context__ = null;
            ToJavaStringNode toJavaStringNode__ = this.insert((ToJavaStringNode.create()));
            Objects.requireNonNull(toJavaStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
            VarHandle.storeStoreFence();
            this.toJavaStringNode_ = toJavaStringNode__;
            InteropLibrary arrays__ = this.insert((INTEROP_LIBRARY_.createDispatched(3)));
            Objects.requireNonNull(arrays__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
            VarHandle.storeStoreFence();
            this.arrays_ = arrays__;
            context__ = (LuaContext.get(this));
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaImportBuiltin.importSymbol(Object[], ToJavaStringNode, InteropLibrary, LuaContext)] */;
            this.state_0_ = state_0;
            return importSymbol(arg0Value, toJavaStringNode__, arrays__, context__);
        }
    }

    @NeverDefault
    public static LuaImportBuiltin create() {
        return new LuaImportBuiltinNodeGen();
    }

}
