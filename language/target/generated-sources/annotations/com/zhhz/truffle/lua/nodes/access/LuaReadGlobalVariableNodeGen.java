// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaTable;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaReadGlobalVariableNode#readGlobal}
 *     Activation probability: 1.00000
 *     With/without class size: 28/8 bytes
 * </pre> */
@GeneratedBy(LuaReadGlobalVariableNode.class)
@SuppressWarnings("javadoc")
public final class LuaReadGlobalVariableNodeGen extends LuaReadGlobalVariableNode {

    private static final LibraryFactory<DynamicObjectLibrary> DYNAMIC_OBJECT_LIBRARY_ = LibraryFactory.resolve(DynamicObjectLibrary.class);

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaReadGlobalVariableNode#readGlobal}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private ReadGlobalData readGlobal_cache;

    private LuaReadGlobalVariableNodeGen(TruffleString name) {
        super(name);
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaReadGlobalVariableNode.readGlobal(LuaTable, DynamicObjectLibrary)] */) {
            ReadGlobalData s0_ = this.readGlobal_cache;
            if (s0_ != null) {
                return readGlobal(s0_.globals_, s0_.tableLib_);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize();
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeGeneric(frameValue);
        return;
    }

    private Object executeAndSpecialize() {
        int state_0 = this.state_0_;
        ReadGlobalData s0_ = this.insert(new ReadGlobalData());
        s0_.globals_ = (getContext().getGlobals());
        DynamicObjectLibrary tableLib__ = s0_.insert((DYNAMIC_OBJECT_LIBRARY_.createDispatched(3)));
        Objects.requireNonNull(tableLib__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        s0_.tableLib_ = tableLib__;
        VarHandle.storeStoreFence();
        this.readGlobal_cache = s0_;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaReadGlobalVariableNode.readGlobal(LuaTable, DynamicObjectLibrary)] */;
        this.state_0_ = state_0;
        return readGlobal(s0_.globals_, tableLib__);
    }

    @NeverDefault
    public static LuaReadGlobalVariableNode create(TruffleString name) {
        return new LuaReadGlobalVariableNodeGen(name);
    }

    @GeneratedBy(LuaReadGlobalVariableNode.class)
    @DenyReplace
    private static final class ReadGlobalData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadGlobalVariableNode#readGlobal}
         *   Parameter: {@link LuaTable} globals</pre> */
        @CompilationFinal LuaTable globals_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadGlobalVariableNode#readGlobal}
         *   Parameter: {@link DynamicObjectLibrary} tableLib</pre> */
        @Child DynamicObjectLibrary tableLib_;

        ReadGlobalData() {
        }

    }
}
