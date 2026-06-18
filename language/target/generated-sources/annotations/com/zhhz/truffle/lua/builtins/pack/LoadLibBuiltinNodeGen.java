// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins.pack;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LoadLibBuiltinNode#doLoadLib}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LoadLibBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LoadLibBuiltinNodeGen extends LoadLibBuiltinNode {

    private LoadLibBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doLoadLib(arg0Value);
    }

    @NeverDefault
    public static LoadLibBuiltinNode create() {
        return new LoadLibBuiltinNodeGen();
    }

}
