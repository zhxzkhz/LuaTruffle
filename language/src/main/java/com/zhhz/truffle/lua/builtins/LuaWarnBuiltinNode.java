package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 实现 Lua 5.4 的 {@code warn}：把参数拼接成一条警告消息输出到标准错误。
 *
 * <p>与参考实现一致：默认不输出，{@code warn("@on")}/{@code warn("@off")}
 * 打开或关闭警告；以 {@code '@'} 开头的其它消息视为控制消息并被忽略。
 * 字符串与数字可作参数。
 */
@NodeInfo(shortName = "warn")
public abstract class LuaWarnBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    protected Object warn(Object[] arguments) {
        int count = arguments.length;
        if (count == 0) {
            throw LuaException.create("bad argument #1 to 'warn' (string expected, got no value)", this);
        }
        LuaContext context = getContext();
        for (int i = 0; i < count; i++) {
            context.warning(messageOf(arguments[i], i), i == count - 1);
        }
        return LuaNil.SINGLETON;
    }

    @TruffleBoundary
    private String messageOf(Object value, int index) {
        if (value instanceof TruffleString string) {
            return string.toJavaStringUncached();
        }
        if (value instanceof Long number) {
            return Long.toString(number);
        }
        if (value instanceof Double number) {
            return Double.toString(number);
        }
        throw LuaException.create("bad argument #" + (index + 1) + " to 'warn' (string expected, got "
                + LuaTypesUtil.getArgumentTypeName(value) + ")", this);
    }
}
