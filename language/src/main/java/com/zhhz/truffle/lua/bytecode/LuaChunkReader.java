package com.zhhz.truffle.lua.bytecode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 以小端序读取 Lua 5.3 预编译 chunk (对应 lundump.c 的 LoadState)。
 *
 * <p>越界读取与 lundump.c 一致：抛出 {@code "... truncated precompiled chunk"}。
 */
public final class LuaChunkReader {

    private final byte[] buf;
    private final String name;
    private int pos;

    public LuaChunkReader(byte[] buf, String name) {
        this.buf = buf;
        this.name = name;
    }

    public LuaChunkReader(byte[] buf) {
        this(buf, "binary string");
    }

    public String name() {
        return name;
    }

    public int position() {
        return pos;
    }

    public boolean eof() {
        return pos >= buf.length;
    }

    private void require(int n) {
        if (pos + n > buf.length) {
            throw LuaBytecodeException.chunkError(name, "truncated");
        }
    }

    public int readByte() {
        require(1);
        return buf[pos++] & 0xFF;
    }

    public byte[] readBytes(int n) {
        require(n);
        byte[] out = Arrays.copyOfRange(buf, pos, pos + n);
        pos += n;
        return out;
    }

    /** 读取 4 字节小端 int。 */
    public int readInt() {
        require(4);
        int v = (buf[pos] & 0xFF)
                | ((buf[pos + 1] & 0xFF) << 8)
                | ((buf[pos + 2] & 0xFF) << 16)
                | ((buf[pos + 3] & 0xFF) << 24);
        pos += 4;
        return v;
    }

    /** 读取 8 字节小端 long (size_t / lua_Integer)。 */
    public long readSizeT() {
        require(8);
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v |= ((long) (buf[pos + i] & 0xFF)) << (8 * i);
        }
        pos += 8;
        return v;
    }

    public long readInteger() {
        return readSizeT();
    }

    /** 读取 8 字节小端 IEEE-754 double。 */
    public double readNumber() {
        return Double.longBitsToDouble(readSizeT());
    }

    /** 读取 n 个 int (每条 4 字节小端)。 */
    public int[] readIntVector(int n) {
        int[] v = new int[n];
        for (int i = 0; i < n; i++) {
            v[i] = readInt();
        }
        return v;
    }

    /**
     * 按 LoadString 规则读取字符串，返回原始字节；空串长度标记返回 null。
     */
    public byte[] readStringBytes() {
        long size = readByte();
        if (size == 0xFF) {
            size = readSizeT();
        }
        if (size == 0) {
            return null;
        }
        return readBytes((int) (size - 1));
    }

    /** 读取字符串并转成 ISO-8859-1 文本；空标记返回 null。 */
    public String readString() {
        byte[] b = readStringBytes();
        return b == null ? null : new String(b, StandardCharsets.ISO_8859_1);
    }

    /** 校验期望的字面量字节串。 */
    public void checkLiteral(byte[] literal, String msg) {
        byte[] got = readBytes(literal.length);
        if (!Arrays.equals(got, literal)) {
            throw LuaBytecodeException.chunkError(name, msg);
        }
    }

    /** 校验头部声明的类型大小。 */
    public void checkSize(int expected, String typeName) {
        if (readByte() != expected) {
            throw LuaBytecodeException.chunkError(name, typeName + " size mismatch in");
        }
    }
}
