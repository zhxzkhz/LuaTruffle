package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import java.util.Objects;

@ExportLibrary(InteropLibrary.class)
public final class LuaMultiValue implements TruffleObject {
    private final Object values;

    private final boolean multiValue;

    public LuaMultiValue(Object[] values) {
        this.values = values;
        multiValue = true;
    }

    public LuaMultiValue(Object value) {
        this.values = new Object[]{value};
        multiValue = false;
        throw new RuntimeException("不该使用这方法");
    }

    public boolean isMultiValue() {
        return multiValue;
    }

    // --- Interop 实现，让这个对象表现得像一个数组 ---
    @ExportMessage
    boolean hasArrayElements() {
        return multiValue;
    }

    @ExportMessage
    long getArraySize() {
        if (multiValue){
            return ((Object[])values).length;
        }
        return 0;
    }

    @ExportMessage
    boolean isArrayElementReadable(long index) {
        return index >= 0 && multiValue && index < ((Object[])values).length;
    }

    @ExportMessage
    Object readArrayElement(long index) throws InvalidArrayIndexException {
        if (!isArrayElementReadable(index)) {
            throw InvalidArrayIndexException.create(index);
        }
        if (multiValue){
            return ((Object[])values)[Math.toIntExact(index)];
        }
        return null;
    }

    public Object values() {
        return values;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LuaMultiValue) obj;
        return Objects.equals(this.values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "LuaMultiValue[" +
                "values=" + values + ']';
    }

}