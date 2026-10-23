package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.zhhz.truffle.lua.LuaLanguage;

/**
 * polyglot 作用域视图：{@code Context.getBindings("lua")} 通过它读写全局变量。
 *
 * <p>{@link InteropLibrary#isScope(Object)} 是类型判断，必须始终返回 {@code true}，
 * 因此不能用普通 {@link LuaTable} 条件性实现该消息；全局表由本类包装后交给
 * {@code LuaLanguage.getScope}。
 */
@ExportLibrary(InteropLibrary.class)
public final class LuaScope implements TruffleObject {

    private static final InteropLibrary GLOBALS = InteropLibrary.getUncached();

    private final LuaTable globals;

    public LuaScope(LuaTable globals) {
        this.globals = globals;
    }

    public LuaTable getGlobals() {
        return globals;
    }

    @ExportMessage
    boolean isScope() {
        return true;
    }

    @ExportMessage
    boolean hasMembers() {
        return true;
    }

    @ExportMessage
    boolean hasLanguageId() {
        return true;
    }

    @ExportMessage
    String getLanguageId() {
        return LuaLanguage.ID;
    }

    @ExportMessage
    Object getMembers(boolean includeInternal) throws UnsupportedMessageException {
        return GLOBALS.getMembers(globals, includeInternal);
    }

    @ExportMessage
    boolean isMemberReadable(String member) {
        return GLOBALS.isMemberReadable(globals, member);
    }

    @ExportMessage
    Object readMember(String member) throws UnknownIdentifierException, UnsupportedMessageException {
        return com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.normalizeHostValue(GLOBALS.readMember(globals, member));
    }

    @ExportMessage
    boolean isMemberModifiable(String member) {
        // 已存在的全局变量可修改；不存在的按插入处理（interop 要求二者互斥）。
        return GLOBALS.isMemberReadable(globals, member);
    }

    @ExportMessage
    boolean isMemberInsertable(String member) {
        return !GLOBALS.isMemberReadable(globals, member);
    }

    @ExportMessage
    void writeMember(String member, Object value)
            throws UnknownIdentifierException, UnsupportedTypeException, UnsupportedMessageException {
        GLOBALS.writeMember(globals, member, com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.normalizeHostValue(value));
    }

    @ExportMessage
    Object toDisplayString(boolean allowSideEffects) {
        return "Lua global scope";
    }
}
