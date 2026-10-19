package com.zhhz.truffle.lua.bytecode;

/**
 * 将 {@link Proto} 序列化为 Lua 5.3 预编译 chunk (对应 ldump.c)。
 */
public final class LuaDumper {

    private final LuaChunkWriter w = new LuaChunkWriter();
    private final boolean strip;

    private LuaDumper(boolean strip) {
        this.strip = strip;
    }

    /** 导出函数原型为二进制 chunk。 */
    public static byte[] dump(Proto f, boolean strip) {
        LuaDumper d = new LuaDumper(strip);
        d.dumpHeader();
        d.w.writeByte(f.sizeupvalues());
        d.dumpFunction(f, null);
        return d.w.toByteArray();
    }

    private void dumpHeader() {
        w.writeBytes(LuaBytecodeConstants.LUA_SIGNATURE);
        w.writeByte(LuaBytecodeConstants.LUAC_VERSION);
        w.writeByte(LuaBytecodeConstants.LUAC_FORMAT);
        w.writeBytes(LuaBytecodeConstants.LUAC_DATA);
        w.writeByte(LuaBytecodeConstants.SIZEOF_INT);
        w.writeByte(LuaBytecodeConstants.SIZEOF_SIZE_T);
        w.writeByte(LuaBytecodeConstants.SIZEOF_INSTRUCTION);
        w.writeByte(LuaBytecodeConstants.SIZEOF_LUA_INTEGER);
        w.writeByte(LuaBytecodeConstants.SIZEOF_LUA_NUMBER);
        w.writeInteger(LuaBytecodeConstants.LUAC_INT);
        w.writeNumber(LuaBytecodeConstants.LUAC_NUM);
    }

    private void dumpFunction(Proto f, String psource) {
        if (strip || java.util.Objects.equals(f.source, psource)) {
            w.writeString(null);
        } else {
            w.writeString(f.source);
        }
        w.writeInt(f.linedefined);
        w.writeInt(f.lastlinedefined);
        w.writeByte(f.numparams);
        w.writeByte(f.isVararg);
        w.writeByte(f.maxstacksize);
        dumpCode(f);
        dumpConstants(f);
        dumpUpvalues(f);
        dumpProtos(f);
        dumpDebug(f);
    }

    private void dumpCode(Proto f) {
        w.writeInt(f.sizecode());
        w.writeIntVector(f.code, f.sizecode());
    }

    private void dumpConstants(Proto f) {
        w.writeInt(f.sizek());
        for (LuaConstant c : f.k) {
            w.writeByte(c.tag());
            switch (c) {
                case LuaConstant.Nil ignored -> {
                }
                case LuaConstant.Bool b -> w.writeByte(b.value() ? 1 : 0);
                case LuaConstant.Flt flt -> w.writeNumber(flt.value());
                case LuaConstant.Int i -> w.writeInteger(i.value());
                case LuaConstant.Str s -> w.writeByteString(s.bytes());
            }
        }
    }

    private void dumpUpvalues(Proto f) {
        w.writeInt(f.sizeupvalues());
        for (Upvaldesc u : f.upvalues) {
            w.writeByte(u.instack ? 1 : 0);
            w.writeByte(u.idx);
        }
    }

    private void dumpProtos(Proto f) {
        w.writeInt(f.sizep());
        for (Proto child : f.p) {
            dumpFunction(child, f.source);
        }
    }

    private void dumpDebug(Proto f) {
        int nline = strip ? 0 : f.sizelineinfo();
        w.writeInt(nline);
        w.writeIntVector(f.lineinfo, nline);

        int nloc = strip ? 0 : f.sizelocvars();
        w.writeInt(nloc);
        for (int i = 0; i < nloc; i++) {
            LocVar lv = f.locvars[i];
            w.writeString(lv.varname);
            w.writeInt(lv.startpc);
            w.writeInt(lv.endpc);
        }

        int nup = strip ? 0 : f.sizeupvalues();
        w.writeInt(nup);
        for (int i = 0; i < nup; i++) {
            w.writeString(f.upvalues[i].name);
        }
    }
}
