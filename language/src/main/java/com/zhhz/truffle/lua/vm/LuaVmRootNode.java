package com.zhhz.truffle.lua.vm;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 字节码 VM 的统一入口 RootNode。
 *
 * <p>所有由字节码 {@code load} 产生的 VM 闭包共用一个由 {@link LuaContext#getVmCallTarget()}
 * 创建的 CallTarget；该 CallTarget 的根节点就是本类。当 Truffle interop 协议执行一个 VM
 * 闭包时，{@code frame.getArguments()[0]} 是 {@link LuaFunction}，其后为实际参数。
 */
public final class LuaVmRootNode extends RootNode {

    public LuaVmRootNode(LuaLanguage language) {
        super(language);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object[] args = frame.getArguments();
        LuaFunction function = (LuaFunction) args[0];
        Object[] userArguments = new Object[args.length - 1];
        System.arraycopy(args, 1, userArguments, 0, userArguments.length);

        LuaContext context = LuaContext.get(this);
        LuaVm vm = new LuaVm(context);
        context.enterVmContainer(vm);
        Object[] results;
        try {
            results = vm.execute(function, userArguments);
        } finally {
            context.exitVmContainer(vm);
        }

        if (results.length == 0) {
            return LuaNil.SINGLETON;
        }
        if (results.length == 1) {
            return results[0];
        }
        return new LuaMultiValue(results);
    }
}
