package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

/**
 * 一个用于向 Interop 暴露 LuaTable 键集合的 TruffleObject。
 * 它表现得像一个只读的字符串/对象数组。
 */
@ExportLibrary(InteropLibrary.class)
public record LuaTableKeys(Object[] keys) implements TruffleObject {

    @ExportMessage
    public boolean hasArrayElements() {
        return true;
    }

    @ExportMessage
    public long getArraySize() {
        return keys.length;
    }

    @ExportMessage
    public boolean isArrayElementReadable(long index) {
        return index >= 0 && index < keys.length;
    }

    @ExportMessage
    public Object readArrayElement(long index) throws InvalidArrayIndexException {
        if (!isArrayElementReadable(index)) {
            throw InvalidArrayIndexException.create(index);
        }
        return keys[(int) index];
    }
}
