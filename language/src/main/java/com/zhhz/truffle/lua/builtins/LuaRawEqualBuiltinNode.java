package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;

/**
 * 实现 {@code rawequal(v1, v2)}：不触发任何元方法地进行相等性比较。
 */
@GenerateInline(false)
@NodeInfo(shortName = "rawequal")
public abstract class LuaRawEqualBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doRawEqual(Object[] arguments) {
        if (arguments.length < 1) {
            throw LuaException.create("bad argument #1 to 'rawequal' (value expected)", this);
        }
        if (arguments.length < 2) {
            throw LuaException.create("bad argument #2 to 'rawequal' (value expected)", this);
        }
        return rawEquals(arguments[0], arguments[1]);
    }

    static boolean rawEquals(Object left, Object right) {
        if (left == right) {
            return true;
        }
        // 显式分派到 Long/Double，避免对 Number 的虚调用把 JDK 中其他
        // Number 实现（如 BigInteger）拉进 Native Image 的运行期编译范围。
        if (left instanceof Long l && right instanceof Long r) {
            return l.longValue() == r.longValue();
        }
        if (left instanceof Double l && right instanceof Double r) {
            return l.doubleValue() == r.doubleValue();
        }
        if (left instanceof Long l && right instanceof Double r) {
            return l.doubleValue() == r.doubleValue();
        }
        if (left instanceof Double l && right instanceof Long r) {
            return l.doubleValue() == r.doubleValue();
        }
        if (left instanceof TruffleString l && right instanceof TruffleString r) {
            return TruffleString.EqualNode.getUncached().execute(l, r, LuaLanguage.STRING_ENCODING);
        }
        return false;
    }
}
