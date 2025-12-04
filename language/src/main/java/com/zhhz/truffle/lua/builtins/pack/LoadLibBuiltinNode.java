package com.zhhz.truffle.lua.builtins.pack;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.builtins.LuaBuiltinNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

@NodeInfo(shortName = "package.loadlib")
public abstract class LoadLibBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doLoadLib(Object[] args) {
        // Truffle 默认不支持直接通过 Lua 的 loadlib 加载 C 库
        // 除非你集成了 Sulong (GraalVM 的 LLVM 运行时)

        // 标准行为：加载失败返回 nil, 错误信息, "absent"
        return new Object[] {
                LuaNil.SINGLETON,
                toTruffleString("dynamic libraries not enabled in Truffle/Java mode"),
                toTruffleString("absent")
        };
    }
}