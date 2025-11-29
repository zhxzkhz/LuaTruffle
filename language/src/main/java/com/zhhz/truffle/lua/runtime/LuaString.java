package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.utilities.TriState;
import com.zhhz.truffle.lua.LuaLanguage;

@ExportLibrary(InteropLibrary.class)
public final class LuaString extends LuaValue {

    private final TruffleString value;

    public LuaString(Shape shape, TruffleString value) {
        super(shape);
        this.value = value;
    }

    // 注意：TruffleString.Encoding.UTF_16 是Java String的内部编码
    public LuaString(Shape shape, String javaString) {
        super(shape);
        this.value = TruffleString.fromJavaStringUncached(javaString, LuaLanguage.STRING_ENCODING);
    }

    public TruffleString getValue() {
        return value;
    }

    // --- 继承自 LuaValue ---
    @Override
    public String getTypeName() {
        return "string";
    }

    // --- InteropLibrary 实现 ---

    @ExportMessage
    public boolean isString() {
        return true;
    }

    @ExportMessage
    public String asString() {
        // TruffleString 有高效的方法转换回 Java String
        // 注意：这可能是一个有开销的操作，所以只在与外部Java交互时使用
        return value.toJavaStringUncached();
    }

    // 同样，为 toDisplayString 提供实现
    @ExportMessage
    public Object toDisplayString(boolean allowSideEffects) {
        return value; // TruffleString 本身就可以被调试器很好地显示
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LuaString other = (LuaString) obj;
        // 比较内部的 TruffleString
        return TruffleString.EqualNode.getUncached().execute(this.value, other.value, TruffleString.Encoding.UTF_16);
    }

    @Override
    public int hashCode() {
        // 使用内部 TruffleString 的哈希码
        return TruffleString.HashCodeNode.getUncached().execute(this.value, TruffleString.Encoding.UTF_16);
    }

    /**
     * 提供与 isIdenticalOrUndefined 相一致的身份哈希码。
     * 因为我们的身份是基于值的，所以我们的身份哈希码也基于值的哈希码。
     * @return The identity hash code.
     */
    @ExportMessage
    public int identityHashCode() {
        // 直接返回我们为 Java hashCode() 计算的同一个值。
        return this.hashCode();
    }

    /**
     * 这是Truffle判断值相等性的核心消息。
     * 我们在这里定义了两个LuaString相等的条件。
     */
    @ExportMessage
    public TriState isIdenticalOrUndefined(Object other) {
        // 如果比较的对象是同一个LuaString实例
        if (this == other) {
            return TriState.TRUE; // 1 表示 IDENTICAL (相同)
        }
        // 如果比较的对象是另一个LuaString
        if (other instanceof LuaString otherString) {
            // 检查内部的TruffleString是否相等
            boolean isEqual = TruffleString.EqualNode.getUncached().execute(
                    this.value, otherString.value, TruffleString.Encoding.UTF_16
            );
            return isEqual ? TriState.TRUE : TriState.FALSE; // 0 表示 DISJOINT (不相同)
        }
        // 对于所有其他类型的对象，我们无法判断
        return TriState.UNDEFINED; // -1 表示 UNDEFINED (未定义)
    }

    @Override
    public String toString() {
        return getTypeName() + "(" + value + ")" + ": 0x" + Integer.toHexString(hashCode());
    }
}
