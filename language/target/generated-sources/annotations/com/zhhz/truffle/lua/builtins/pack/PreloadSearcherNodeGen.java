// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins.pack;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link PreloadSearcherNode#search}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(PreloadSearcherNode.class)
@SuppressWarnings("javadoc")
public final class PreloadSearcherNodeGen extends PreloadSearcherNode {

    private PreloadSearcherNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return search(arg0Value);
    }

    @NeverDefault
    public static PreloadSearcherNode create() {
        return new PreloadSearcherNodeGen();
    }

}
