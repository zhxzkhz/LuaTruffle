// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;
import java.lang.invoke.VarHandle;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaSelectBuiltinNode#doSelect}
 *     Activation probability: 1.00000
 *     With/without class size: 24/4 bytes
 * </pre> */
@GeneratedBy(LuaSelectBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaSelectBuiltinNodeGen extends LuaSelectBuiltinNode {

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaSelectBuiltinNode#doSelect}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private SelectData select_cache;

    private LuaSelectBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaSelectBuiltinNode.doSelect(Object[], LuaCoerceToIntegerNode)] */) {
            SelectData s0_ = this.select_cache;
            if (s0_ != null) {
                return doSelect(arg0Value, s0_.coerceValue_);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Object executeAndSpecialize(Object[] arg0Value) {
        int state_0 = this.state_0_;
        SelectData s0_ = this.insert(new SelectData());
        s0_.coerceValue_ = s0_.insert((LuaCoerceToIntegerNode.create()));
        VarHandle.storeStoreFence();
        this.select_cache = s0_;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaSelectBuiltinNode.doSelect(Object[], LuaCoerceToIntegerNode)] */;
        this.state_0_ = state_0;
        return doSelect(arg0Value, s0_.coerceValue_);
    }

    @NeverDefault
    public static LuaSelectBuiltinNode create() {
        return new LuaSelectBuiltinNodeGen();
    }

    @GeneratedBy(LuaSelectBuiltinNode.class)
    @DenyReplace
    private static final class SelectData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaSelectBuiltinNode#doSelect}
         *   Parameter: {@link LuaCoerceToIntegerNode} coerceValue</pre> */
        @Child LuaCoerceToIntegerNode coerceValue_;

        SelectData() {
        }

    }
}
