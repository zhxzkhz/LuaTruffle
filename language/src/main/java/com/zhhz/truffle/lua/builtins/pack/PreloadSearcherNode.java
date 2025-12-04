package com.zhhz.truffle.lua.builtins.pack;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.builtins.LuaBuiltinNode;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

@NodeInfo(shortName = "searcher_preload")
public abstract class PreloadSearcherNode extends LuaBuiltinNode {
    @Specialization
    @CompilerDirectives.TruffleBoundary
    public Object search(Object[] args) {
        String name = args[0].toString(); // 简化转换

        // 获取 package.preload
        LuaTable globals = getContext().getGlobals();
        LuaTable pkg = (LuaTable) globals.rawget("package");
        LuaTable preload = (LuaTable) pkg.rawget("preload");

        Object loader = preload.rawget(toTruffleString(name));
        if (loader != null && loader != LuaNil.SINGLETON) {
            return loader; // 找到了加载器函数
        }

        return toTruffleString("\n\tno field package.preload['" + name + "']");
    }
}
