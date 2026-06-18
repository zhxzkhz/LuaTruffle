// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins.pack;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link SearchPathBuiltinNode#doSearch}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(SearchPathBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class SearchPathBuiltinNodeGen extends SearchPathBuiltinNode {

    private SearchPathBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doSearch(arg0Value);
    }

    @NeverDefault
    public static SearchPathBuiltinNode create() {
        return new SearchPathBuiltinNodeGen();
    }

}
