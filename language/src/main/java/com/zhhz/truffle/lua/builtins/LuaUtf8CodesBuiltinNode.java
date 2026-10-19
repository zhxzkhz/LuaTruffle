package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;

/**
 * 实现 {@code utf8.codes(s)}：返回迭代三元组 {@code (iterator, s, 0)}。
 */
@GenerateInline(false)
@NodeInfo(shortName = "utf8.codes")
public abstract class LuaUtf8CodesBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doCodes(Object[] arguments) {
        if (arguments.length == 0) {
            throw com.zhhz.truffle.lua.LuaException.create(
                    "bad argument #1 to 'codes' (string expected, got no value)", this);
        }
        byte[] bytes = LuaStringSupport.ensureBytes(arguments, 0, "codes");
        return new LuaMultiValue(new Object[]{
                getContext().getUtf8Iterator(),
                LuaStringSupport.fromBytes(bytes),
                0L
        });
    }
}
