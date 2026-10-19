package com.zhhz.truffle.lua.bytecode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 以 Lua 5.3 的小端序格式写出预编译 chunk (对应 ldump.c)。
 */
public final class LuaChunkWriter {

    private byte[] buf = new byte[256];
    private int len;

    public int size() {
        return len;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buf, len);
    }

    private void ensure(int extra) {
        if (len + extra > buf.length) {
            int cap = Math.max(buf.length * 2, len + extra);
            buf = Arrays.copyOf(buf, cap);
        }
    }

    public void writeByte(int v) {
        ensure(1);
        buf[len++] = (byte) v;
    }

    public void writeBytes(byte[] b) {
        ensure(b.length);
        System.arraycopy(b, 0, buf, len, b.length);
        len += b.length;
    }

    /** 写出一个 int (4 字节小端)。 */
    public void writeInt(int v) {
        ensure(4);
        buf[len++] = (byte) v;
        buf[len++] = (byte) (v >>> 8);
        buf[len++] = (byte) (v >>> 16);
        buf[len++] = (byte) (v >>> 24);
    }

    /** 写出一个 size_t (8 字节小端)。 */
    public void writeSizeT(long v) {
        ensure(8);
        for (int i = 0; i < 8; i++) {
            buf[len++] = (byte) (v >>> (8 * i));
        }
    }

    /** 写出一个 lua_Integer (8 字节小端)。 */
    public void writeInteger(long v) {
        writeSizeT(v);
    }

    /** 写出一个 lua_Number (IEEE-754 double，8 字节小端)。 */
    public void writeNumber(double v) {
        writeSizeT(Double.doubleToRawLongBits(v));
    }

    /** 写出一个 int 向量 (每条 4 字节小端)。 */
    public void writeIntVector(int[] v, int n) {
        for (int i = 0; i < n; i++) {
            writeInt(v[i]);
        }
    }

    /** 写出原始字符串 (无长度前缀)。 */
    public void writeRawString(String s) {
        writeBytes(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    /** 按 DumpString 规则写出一个字符串；null 写单字节 0。 */
    public void writeString(String s) {
        if (s == null) {
            writeByte(0);
            return;
        }
        writeByteString(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    /** 按 DumpString 规则写出原始字节串 (二进制安全)。 */
    public void writeByteString(byte[] bytes) {
        long size = (long) bytes.length + 1; // 含结尾 '\0'
        if (size < 0xFFL) {
            writeByte((int) size);
        } else {
            writeByte(0xFF);
            writeSizeT(size);
        }
        writeBytes(bytes); // 不保存结尾 '\0'
    }
}
