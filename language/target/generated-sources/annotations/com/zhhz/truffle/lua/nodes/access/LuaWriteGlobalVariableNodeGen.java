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
 *   Specialization {@link LuaWriteGlobalVariableNode#doWrite}
 *     Activation probability: 1.00000
 *     With/without class size: 28/8 bytes
 * </pre> */
@GeneratedBy(LuaWriteGlobalVariableNode.class)
@SuppressWarnings("javadoc")
public final class LuaWriteGlobalVariableNodeGen extends LuaWriteGlobalVariableNode {

    private static final LibraryFactory<DynamicObjectLibrary> DYNAMIC_OBJECT_LIBRARY_ = LibraryFactory.resolve(DynamicObjectLibrary.class);

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaWriteGlobalVariableNode#doWrite}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private WriteData write_cache;

    private LuaWriteGlobalVariableNodeGen(TruffleString name) {
        super(name);
    }

    @Override
    public void execute(VirtualFrame frameValue, Object arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaWriteGlobalVariableNode.doWrite(Object, LuaTable, DynamicObjectLibrary)] */) {
            WriteData s0_ = this.write_cache;
            if (s0_ != null) {
                doWrite(arg0Value, s0_.globals_, s0_.tableLib_);
                return;
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        executeAndSpecialize(arg0Value);
        return;
    }

    private void executeAndSpecialize(Object arg0Value) {
        int state_0 = this.state_0_;
        WriteData s0_ = this.insert(new WriteData());
        s0_.globals_ = (getContext().getGlobals());
        DynamicObjectLibrary tableLib__ = s0_.insert((DYNAMIC_OBJECT_LIBRARY_.createDispatched(3)));
        Objects.requireNonNull(tableLib__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        s0_.tableLib_ = tableLib__;
        VarHandle.storeStoreFence();
        this.write_cache = s0_;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaWriteGlobalVariableNode.doWrite(Object, LuaTable, DynamicObjectLibrary)] */;
        this.state_0_ = state_0;
        doWrite(arg0Value, s0_.globals_, tableLib__);
        return;
    }

    @NeverDefault
    public static LuaWriteGlobalVariableNode create(TruffleString name) {
        return new LuaWriteGlobalVariableNodeGen(name);
    }

    @GeneratedBy(LuaWriteGlobalVariableNode.class)
    @DenyReplace
    private static final class WriteData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaWriteGlobalVariableNode#doWrite}
         *   Parameter: {@link LuaTable} globals</pre> */
        @CompilationFinal LuaTable globals_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaWriteGlobalVariableNode#doWrite}
         *   Parameter: {@link DynamicObjectLibrary} tableLib</pre> */
        @Child DynamicObjectLibrary tableLib_;

        WriteData() {
        }

    }
}
