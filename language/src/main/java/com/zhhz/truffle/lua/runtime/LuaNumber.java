package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

import java.math.BigInteger;

@ExportLibrary(InteropLibrary.class)
public final class LuaNumber extends LuaValue  {

    private final double value;

    public LuaNumber(Shape shape, double value) {
        super(shape);
        this.value = value;
    }

    public double getValue() {
        return value;
    }


    // --- 消息1：让它表现得像一个数字 ---
    @ExportMessage
    public boolean isNumber() {
        return true;
    }

    // 导出语言身份：标识此宿主对象归属 Lua 语言（工具与调试器依赖）
    @ExportMessage
    public boolean hasLanguageId() {
        return true;
    }

    @ExportMessage
    public String getLanguageId() {
        return com.zhhz.truffle.lua.LuaLanguage.ID;
    }


    /**
     * 辅助方法：检查这个double值是否是一个没有小数部分的整数。
     * 这是安全地转换为各种整型的先决条件。
     */
    private boolean isExactInteger() {
        // 必须不是无穷大，不是NaN，且没有小数部分
        return !Double.isInfinite(value) && !Double.isNaN(value) && value % 1 == 0;
    }

    // --- fitsIn... messages ---

    @ExportMessage
    public boolean fitsInByte() {
        return isExactInteger() && value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
    }

    @ExportMessage
    public boolean fitsInShort() {
        return isExactInteger() && value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
    }

    @ExportMessage
    public boolean fitsInInt() {
        return isExactInteger() && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
    }

    @ExportMessage
    public boolean fitsInLong() {
        return isExactInteger() && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE;
    }

    @ExportMessage
    public boolean fitsInFloat() {
        // 检查是否在float的表示范围内
        return value >= -Float.MAX_VALUE && value <= Float.MAX_VALUE;
    }

    @ExportMessage
    public boolean fitsInDouble() {
        // 它本身就是一个double，所以总能fit
        return true;
    }

    @ExportMessage
    public boolean fitsInBigInteger() {
        // 只要是没有小数部分的整数，理论上都可以转为BigInteger
        return isExactInteger();
    }


    // --- as... messages ---
    // 每个 as... 方法都先用对应的 fitsIn... 方法进行检查，这是最佳实践。

    @ExportMessage
    public byte asByte() throws UnsupportedMessageException {
        if (!fitsInByte()) {
            throw UnsupportedMessageException.create();
        }
        return (byte) value;
    }

    @ExportMessage
    public short asShort() throws UnsupportedMessageException {
        if (!fitsInShort()) {
            throw UnsupportedMessageException.create();
        }
        return (short) value;
    }

    @ExportMessage
    public int asInt() throws UnsupportedMessageException {
        if (!fitsInInt()) {
            throw UnsupportedMessageException.create();
        }
        return (int) value;
    }

    @ExportMessage
    public long asLong() throws UnsupportedMessageException {
        if (!fitsInLong()) {
            throw UnsupportedMessageException.create();
        }
        return (long) value;
    }

    @ExportMessage
    public float asFloat() throws UnsupportedMessageException {
        if (!fitsInFloat()) {
            throw UnsupportedMessageException.create();
        }
        return (float) value;
    }

    @ExportMessage
    public double asDouble() {
        // 因为内部就是double，所以这个转换永远不会失败
        return value;
    }

    @ExportMessage
    public BigInteger asBigInteger() throws UnsupportedMessageException {
        if (!fitsInBigInteger()) {
            throw UnsupportedMessageException.create();
        }
        // 注意：直接 (BigInteger) value 是不行的，需要用 longValue()
        return BigInteger.valueOf((long) value);
    }

    @ExportMessage
    public boolean isString(){
        return false;
    }

    // 提供一个比基类更具体的 asString/toString 实现
    @ExportMessage
    public String asString() {
        return LuaNumberFormat.toString(value);
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public String getTypeName() {
        return "number";
    }


}
