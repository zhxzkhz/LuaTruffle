// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link IPairsIteratorBuiltinNode#doIteration}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(IPairsIteratorBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class IPairsIteratorBuiltinNodeGen extends IPairsIteratorBuiltinNode {

    private IPairsIteratorBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doIteration(arg0Value);
    }

    @NeverDefault
    public static IPairsIteratorBuiltinNode create() {
        return new IPairsIteratorBuiltinNodeGen();
    }

}
