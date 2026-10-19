package com.zhhz.truffle.lua.test;

import com.zhhz.truffle.lua.bytecode.Instruction;
import com.zhhz.truffle.lua.bytecode.LocVar;
import com.zhhz.truffle.lua.bytecode.LuaBytecodeConstants;
import com.zhhz.truffle.lua.bytecode.LuaBytecodeException;
import com.zhhz.truffle.lua.bytecode.LuaConstant;
import com.zhhz.truffle.lua.bytecode.LuaDumper;
import com.zhhz.truffle.lua.bytecode.LuaUndumper;
import com.zhhz.truffle.lua.bytecode.OpCode;
import com.zhhz.truffle.lua.bytecode.Proto;
import com.zhhz.truffle.lua.bytecode.Upvaldesc;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BytecodeRoundTripTest {

    private static Proto buildProto() {
        Proto child = new Proto();
        child.source = "@test";
        child.linedefined = 5;
        child.lastlinedefined = 7;
        child.numparams = 2;
        child.isVararg = LuaBytecodeConstants.VARARG_ISVARARG;
        child.maxstacksize = 4;
        child.code = new int[]{
                Instruction.createABC(OpCode.MOVE, 0, 1, 0),
                Instruction.createAsBx(OpCode.JMP, 0, -1),
                Instruction.createABx(OpCode.LOADK, 3, 1),
        };
        child.k = new LuaConstant[]{new LuaConstant.Int(7), new LuaConstant.Bool(true)};
        child.upvalues = new Upvaldesc[]{
                new Upvaldesc("a", true, 1),
                new Upvaldesc("b", false, 0),
        };
        child.lineinfo = new int[]{5, 6, 7};
        child.locvars = new LocVar[]{new LocVar("local1", 0, 3)};
        child.p = new Proto[0];

        Proto main = new Proto();
        main.source = "@test";
        main.linedefined = 0;
        main.lastlinedefined = 9;
        main.numparams = 0;
        main.isVararg = 0;
        main.maxstacksize = 3;
        main.code = new int[]{
                Instruction.createABx(OpCode.LOADK, 0, 0),
                Instruction.createAx(OpCode.EXTRAARG, 12345),
                Instruction.createABC(OpCode.RETURN, 0, 2, 0),
        };
        main.k = new LuaConstant[]{
                new LuaConstant.Str("hello"),
                new LuaConstant.Int(42),
                new LuaConstant.Flt(3.5),
                LuaConstant.Nil.INSTANCE,
        };
        main.upvalues = new Upvaldesc[]{new Upvaldesc("up1", true, 0)};
        main.lineinfo = new int[]{0, 1, 2};
        main.locvars = new LocVar[]{new LocVar("x", 0, 3)};
        main.p = new Proto[]{child};
        return main;
    }

    private static void assertEqualsProto(Proto expected, Proto actual, boolean stripped) {
        assertEquals(expected.numparams, actual.numparams);
        assertEquals(expected.isVararg, actual.isVararg);
        assertEquals(expected.maxstacksize, actual.maxstacksize);
        assertEquals(expected.linedefined, actual.linedefined);
        assertEquals(expected.lastlinedefined, actual.lastlinedefined);
        assertArrayEquals(expected.code, actual.code, "code");
        assertEquals(expected.sizek(), actual.sizek(), "sizek");
        for (int i = 0; i < expected.sizek(); i++) {
            assertEquals(expected.k[i], actual.k[i], "constant[" + i + "]");
        }
        assertEquals(expected.sizeupvalues(), actual.sizeupvalues());
        for (int i = 0; i < expected.sizeupvalues(); i++) {
            assertEquals(expected.upvalues[i].instack, actual.upvalues[i].instack);
            assertEquals(expected.upvalues[i].idx, actual.upvalues[i].idx);
            if (stripped) {
                assertNull(actual.upvalues[i].name);
            } else {
                assertEquals(expected.upvalues[i].name, actual.upvalues[i].name);
            }
        }
        if (stripped) {
            assertEquals(0, actual.sizelineinfo());
            assertEquals(0, actual.sizelocvars());
        } else {
            assertArrayEquals(expected.lineinfo, actual.lineinfo, "lineinfo");
            assertEquals(expected.sizelocvars(), actual.sizelocvars());
            for (int i = 0; i < expected.sizelocvars(); i++) {
                assertEquals(expected.locvars[i].varname, actual.locvars[i].varname);
                assertEquals(expected.locvars[i].startpc, actual.locvars[i].startpc);
                assertEquals(expected.locvars[i].endpc, actual.locvars[i].endpc);
            }
        }
        assertEquals(expected.sizep(), actual.sizep());
        for (int i = 0; i < expected.sizep(); i++) {
            assertEqualsProto(expected.p[i], actual.p[i], stripped);
        }
    }

    @Test
    void roundTripPreservesAllFields() {
        Proto main = buildProto();
        byte[] chunk = LuaDumper.dump(main, false);
        Proto back = LuaUndumper.undump(chunk, "@test");
        assertEqualsProto(main, back, false);
    }

    @Test
    void strippedRoundTripDropsDebugAndNames() {
        Proto main = buildProto();
        byte[] chunk = LuaDumper.dump(main, true);
        Proto back = LuaUndumper.undump(chunk, "@test");
        assertEqualsProto(main, back, true);
    }

    @Test
    void headerMatchesOfficialFormat() {
        byte[] chunk = LuaDumper.dump(buildProto(), false);
        byte[] expectedPrefix = {0x1b, 'L', 'u', 'a',
                (byte) LuaBytecodeConstants.LUAC_VERSION,
                (byte) LuaBytecodeConstants.LUAC_FORMAT,
                0x19, (byte) 0x93, '\r', '\n', 0x1a, '\n',
                4, 8, 4, 8, 8};
        assertArrayEquals(expectedPrefix, Arrays.copyOf(chunk, expectedPrefix.length));
    }

    @Test
    void truncatedChunkIsRejected() {
        byte[] chunk = LuaDumper.dump(buildProto(), false);
        byte[] truncated = Arrays.copyOf(chunk, chunk.length / 2);
        LuaBytecodeException ex = assertThrows(LuaBytecodeException.class,
                () -> LuaUndumper.undump(truncated, "@test"));
        assertTrue(ex.getMessage().contains("truncated"), ex.getMessage());
    }

    @Test
    void badVersionIsRejected() {
        byte[] chunk = LuaDumper.dump(buildProto(), false);
        chunk[4] = 0x52; // 改成 5.2
        LuaBytecodeException ex = assertThrows(LuaBytecodeException.class,
                () -> LuaUndumper.undump(chunk, "@test"));
        assertTrue(ex.getMessage().contains("version mismatch"), ex.getMessage());
    }
}
