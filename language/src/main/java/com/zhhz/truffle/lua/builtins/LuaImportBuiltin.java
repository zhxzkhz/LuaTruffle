package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNodeGen;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 内置函数，用于从多语言绑定中导入符号。
 */
@NodeInfo(shortName = "import")
public abstract class LuaImportBuiltin extends LuaBuiltinNode {

    @Child private LuaCoerceToStringNode toStringNode = LuaCoerceToStringNodeGen.create();

    @Specialization
    public Object importSymbol(Object[] symbol,
                               @Cached TruffleString.ToJavaStringNode toJavaStringNode,
                               @CachedLibrary(limit = "3") InteropLibrary arrays,
                               @Bind LuaContext context) {
        try {
            return arrays.readMember(context.getPolyglotBindings(), toJavaStringNode.execute(toStringNode.execute(symbol[0])));
        } catch (UnsupportedMessageException | UnknownIdentifierException e) {
            return LuaNil.SINGLETON;
        } catch (SecurityException e) {
            throw LuaException.create("No polyglot access allowed.", this);
        }
    }

}
