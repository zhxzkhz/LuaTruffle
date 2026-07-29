package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import java.util.Objects;

@ExportLibrary(InteropLibrary.class)
public final class LuaMultiValue implements TruffleObject {
    private final Object[] values;

    public LuaMultiValue(Object[] values) {
        this.values = values;
    }

    // --- Interop 实现，让这个对象表现得像一个数组 ---
    @ExportMessage
    boolean hasArrayElements() {
        return true;
    }

    @ExportMessage
    long getArraySize() {
        return values.length;
    }

    @ExportMessage
    boolean isArrayElementReadable(long index) {
        return index >= 0 && index < values.length;
    }

    @ExportMessage
    Object readArrayElement(long index) throws InvalidArrayIndexException {
        if (!isArrayElementReadable(index)) {
            throw InvalidArrayIndexException.create(index);
        }
        return values[Math.toIntExact(index)];
    }

    public Object[] values() {
        return values;
    }

    public Object firstValue() {
        return values.length == 0 ? LuaNil.SINGLETON : values[0];
    }

    public static Object firstValue(Object value) {
        return value instanceof LuaMultiValue multiValue ? multiValue.firstValue() : value;
    }

    public static Object[] collect(Object[] prefixValues, Object lastValue) {
        if (lastValue instanceof LuaMultiValue multiValue) {
            Object[] trailingValues = multiValue.values;
            Object[] result = new Object[prefixValues.length + trailingValues.length];
            System.arraycopy(prefixValues, 0, result, 0, prefixValues.length);
            System.arraycopy(trailingValues, 0, result, prefixValues.length, trailingValues.length);
            return result;
        }

        Object[] result = new Object[prefixValues.length + 1];
        System.arraycopy(prefixValues, 0, result, 0, prefixValues.length);
        result[prefixValues.length] = lastValue;
        return result;
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
