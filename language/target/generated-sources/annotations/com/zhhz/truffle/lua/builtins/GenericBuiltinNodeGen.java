// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import java.lang.reflect.Method;

/**
 * Debug Info: <pre>
 *   Specialization {@link GenericBuiltinNode#doExecute}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(GenericBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class GenericBuiltinNodeGen extends GenericBuiltinNode {

    private final Method targetMethod;

    private GenericBuiltinNodeGen(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    @Override
    protected Method getTargetMethod() {
        return this.targetMethod;
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doExecute(arg0Value);
    }

    @NeverDefault
    public static GenericBuiltinNode create(Method targetMethod) {
        return new GenericBuiltinNodeGen(targetMethod);
    }

}
