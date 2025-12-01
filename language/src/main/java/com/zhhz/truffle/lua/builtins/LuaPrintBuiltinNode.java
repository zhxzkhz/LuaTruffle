package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.util.Arrays;

/**
 * 实现了Lua的 'print' 内置函数。
 */
@NodeInfo(shortName = "print")
public abstract class LuaPrintBuiltinNode extends LuaBuiltinNode {

    @Child private InteropLibrary interop = InteropLibrary.getFactory().createDispatched(3);
    @Child private TruffleString.ToJavaStringNode toJavaStringNode = TruffleString.ToJavaStringNode.create();


    @Override
    public abstract Object execute(Object[] arguments);

    /**
     * 特化：处理调用 print() 时没有参数的情况。
     */
    @Specialization(guards = "arguments.length == 0")
    @TruffleBoundary
    public Object printWithoutArgs(Object[] arguments) {
        getContext().getOutput().println(); // 打印一个换行符
        return LuaNil.SINGLETON;
    }

    /**
     * 通用特化：处理有一个或多个参数的情况。
     * 使用 `replaces` 来告诉 Truffle，这个特化比上面的更通用。
     * 当 JIT 编译器发现 `arguments.length` 总是 > 0 时，它可以移除对 `printWithoutArgs` 的检查。
     */
    @Specialization(replaces = "printWithoutArgs")
    @TruffleBoundary
    public Object printWithArgs(Object[] arguments,@Cached("create()") LuaCoerceToStringNode toStringNode) {
        getContext().getOutput().println(formatArguments(arguments,toStringNode));
        return LuaNil.SINGLETON;
    }

    @TruffleBoundary
    private String formatArguments(Object[] arguments, LuaCoerceToStringNode toStringNode) {
        if (arguments.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        TruffleString first = toStringNode.execute(arguments[0]);
        sb.append(toJavaStringNode.execute(first));

        for (int i = 1; i < arguments.length; i++) {
            sb.append("\t"); // 参数之间用制表符分隔
            TruffleString next = toStringNode.execute(arguments[i]);
            sb.append(toJavaStringNode.execute(next));
        }
        return sb.toString();
    }

}