package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

/**
 * 一个用于遍历 LuaTable 哈希条目的 TruffleObject 迭代器。
 */
@ExportLibrary(InteropLibrary.class)
public final class LuaTableIterator implements TruffleObject {

    private final Object[] arrayKeys;
    private final Object[] hashKeys;
    private int arrayIndex = 0;
    private int hashIndex = 0;

    public LuaTableIterator(Object[] arrayKeys, Object[] hashKeys) {
        this.arrayKeys = arrayKeys;
        this.hashKeys = hashKeys;
    }

    /**
     * 宣告自己是一个迭代器。
     */
    @ExportMessage
    boolean isIterator() {
        return true;
    }

    /**
     * 检查是否还有下一个元素。
     */
    @ExportMessage
    boolean hasIteratorNextElement() {
        return arrayIndex < arrayKeys.length || hashIndex < hashKeys.length;
    }

    /**
     * 获取下一个元素（键）。
     * Interop协议规定，哈希表的迭代器返回的是*键*。
     */
    @ExportMessage
    Object getIteratorNextElement() throws UnsupportedMessageException {
        if (!hasIteratorNextElement()) {
            throw UnsupportedMessageException.create();
        }
        if (arrayIndex < arrayKeys.length) {
            return arrayKeys[arrayIndex++];
        }
        return hashKeys[hashIndex++];
    }
}