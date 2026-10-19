package com.zhhz.truffle.lua.bytecode;

/**
 * 将 Lua 5.3 预编译 chunk 反序列化为 {@link Proto} (对应 lundump.c)。
 */
public final class LuaUndumper {

    private final LuaChunkReader r;

    private LuaUndumper(LuaChunkReader r) {
        this.r = r;
    }

    /**
     * 解析 chunk 头部与顶层函数。
     *
     * @param data 完整 chunk 字节
     * @param name 由 load 传入的名字 (用于错误消息)
     * @return 顶层函数原型
     */
    public static Proto undump(byte[] data, String name) {
        String stateName = deriveName(name);
        LuaChunkReader reader = new LuaChunkReader(data, stateName);
        LuaUndumper u = new LuaUndumper(reader);
        u.checkHeader();
        u.r.readByte(); // 闭包 upvalue 个数，应与顶层 sizeupvalues 一致
        Proto p = u.loadFunction(null);
        return p;
    }

    /** 复刻 luaU_undump 对 name 的规范化。 */
    private static String deriveName(String name) {
        if (name == null || name.isEmpty()) {
            return "binary string";
        }
        char c0 = name.charAt(0);
        if (c0 == '@' || c0 == '=') {
            return name.substring(1);
        }
        if ((c0 & 0xFF) == LuaBytecodeConstants.LUA_SIGNATURE_0) {
            return "binary string";
        }
        return name;
    }

    private void checkHeader() {
        // 校验完整签名 ESC + "Lua"
        r.checkLiteral(LuaBytecodeConstants.LUA_SIGNATURE, "not a");
        if (r.readByte() != LuaBytecodeConstants.LUAC_VERSION) {
            throw LuaBytecodeException.chunkError(r.name(), "version mismatch in");
        }
        if (r.readByte() != LuaBytecodeConstants.LUAC_FORMAT) {
            throw LuaBytecodeException.chunkError(r.name(), "format mismatch in");
        }
        r.checkLiteral(LuaBytecodeConstants.LUAC_DATA, "corrupted");
        r.checkSize(LuaBytecodeConstants.SIZEOF_INT, "int");
        r.checkSize(LuaBytecodeConstants.SIZEOF_SIZE_T, "size_t");
        r.checkSize(LuaBytecodeConstants.SIZEOF_INSTRUCTION, "Instruction");
        r.checkSize(LuaBytecodeConstants.SIZEOF_LUA_INTEGER, "lua_Integer");
        r.checkSize(LuaBytecodeConstants.SIZEOF_LUA_NUMBER, "lua_Number");
        if (r.readInteger() != LuaBytecodeConstants.LUAC_INT) {
            throw LuaBytecodeException.chunkError(r.name(), "endianness mismatch in");
        }
        if (r.readNumber() != LuaBytecodeConstants.LUAC_NUM) {
            throw LuaBytecodeException.chunkError(r.name(), "float format mismatch in");
        }
    }

    private Proto loadFunction(String psource) {
        Proto f = new Proto();
        f.source = r.readString();
        if (f.source == null) {
            f.source = psource;
        }
        f.linedefined = r.readInt();
        f.lastlinedefined = r.readInt();
        f.numparams = r.readByte();
        f.isVararg = r.readByte();
        f.maxstacksize = r.readByte();
        loadCode(f);
        loadConstants(f);
        loadUpvalues(f);
        loadProtos(f);
        loadDebug(f);
        return f;
    }

    private void loadCode(Proto f) {
        int n = r.readInt();
        f.code = r.readIntVector(n);
    }

    private void loadConstants(Proto f) {
        int n = r.readInt();
        f.k = new LuaConstant[n];
        for (int i = 0; i < n; i++) {
            int t = r.readByte();
            f.k[i] = switch (t) {
                case LuaBytecodeConstants.LUA_TNIL -> LuaConstant.Nil.INSTANCE;
                case LuaBytecodeConstants.LUA_TBOOLEAN -> new LuaConstant.Bool(r.readByte() != 0);
                case LuaBytecodeConstants.LUA_TNUMFLT -> new LuaConstant.Flt(r.readNumber());
                case LuaBytecodeConstants.LUA_TNUMINT -> new LuaConstant.Int(r.readInteger());
                case LuaBytecodeConstants.LUA_TSHRSTR, LuaBytecodeConstants.LUA_TLNGSTR ->
                        new LuaConstant.Str(r.readStringBytes(), t);
                default -> throw LuaBytecodeException.chunkError(r.name(), "invalid constant tag in");
            };
        }
    }

    private void loadUpvalues(Proto f) {
        int n = r.readInt();
        f.upvalues = new Upvaldesc[n];
        for (int i = 0; i < n; i++) {
            Upvaldesc u = new Upvaldesc();
            u.instack = r.readByte() != 0;
            u.idx = r.readByte();
            f.upvalues[i] = u;
        }
    }

    private void loadProtos(Proto f) {
        int n = r.readInt();
        f.p = new Proto[n];
        for (int i = 0; i < n; i++) {
            f.p[i] = loadFunction(f.source);
        }
    }

    private void loadDebug(Proto f) {
        int n = r.readInt();
        f.lineinfo = r.readIntVector(n);

        int nloc = r.readInt();
        f.locvars = new LocVar[nloc];
        for (int i = 0; i < nloc; i++) {
            LocVar lv = new LocVar();
            lv.varname = r.readString();
            lv.startpc = r.readInt();
            lv.endpc = r.readInt();
            f.locvars[i] = lv;
        }

        int nup = r.readInt();
        for (int i = 0; i < nup; i++) {
            f.upvalues[i].name = r.readString();
        }
    }
}
