package com.zhhz.truffle.lua.test;

import com.zhhz.truffle.lua.bytecode.LuaCompiler;
import com.zhhz.truffle.lua.bytecode.LuaDumper;
import com.zhhz.truffle.lua.bytecode.LuaUndumper;
import com.zhhz.truffle.lua.bytecode.Proto;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LuaCompilerSmokeTest {

    private static Proto compile(String source) {
        return LuaCompiler.compile(source, "@smoke.lua");
    }

    private static void dumpRec(StringBuilder sb, Proto p, String indent) {
        sb.append(indent).append("linedefined=").append(p.linedefined)
                .append(" lastlinedefined=").append(p.lastlinedefined)
                .append(" numparams=").append(p.numparams)
                .append(" isVararg=").append(p.isVararg)
                .append(" maxstack=").append(p.maxstacksize)
                .append(" code=").append(p.sizecode())
                .append(" k=").append(p.sizek())
                .append(" upv=").append(p.sizeupvalues())
                .append(" locvars=").append(p.sizelocvars())
                .append("\n");
        for (int i = 0; i < p.p.length; i++) {
            sb.append(indent).append("proto[").append(i).append("]:\n");
            dumpRec(sb, p.p[i], indent + "  ");
        }
    }

    @Test
    public void compilesAndRoundTrips() {
        String src = """
                local a, b = 1, 2
                local s = "hello" .. " world"
                local t = { x = 1, [2] = 3, 4, 5 }
                local function add(x, y)
                    return x + y
                end
                local function outer()
                    local n = 0
                    return function()
                        n = n + 1
                        return n
                    end
                end
                local result = 0
                for i = 1, 10 do
                    result = result + i
                end
                for k, v in pairs(t) do
                    result = result + 1
                end
                while result < 100 do
                    result = result + add(result, 1)
                    if result > 50 then break end
                end
                repeat
                    result = result - 1
                until result < 10
                if a < b then
                    result = result + 1
                elseif a > b then
                    result = result - 1
                else
                    result = result * 2
                end
                local function variadic(...)
                    return select("#", ...), ...
                end
                local ok, err = pcall(function() error("boom") end)
                local obj = { name = "x" }
                function obj:method(v)
                    return self.name .. v
                end
                print(a, b, s, result, variadic(1, 2, 3), ok, obj:method("!"))
                do
                    goto skip
                    ::unused::
                    ::skip::
                end
                return add, outer
                """;
        Proto p = compile(src);
        String stats = dumpRecString(p);
        System.out.println(stats);
        assertTrue(p.sizecode() > 0, "main chunk must have code");
        assertTrue(p.p.length >= 4, "expected nested protos, got " + p.p.length);

        byte[] dumped = LuaDumper.dump(p, false);
        assertTrue(dumped.length > 0);
        Proto back = LuaUndumper.undump(dumped, "smoke");
        assertEquals(p.sizecode(), back.sizecode());
        assertEquals(p.sizek(), back.sizek());
        assertEquals(p.p.length, back.p.length);
        assertEquals(p.maxstacksize, back.maxstacksize);

        byte[] redumped = LuaDumper.dump(back, false);
        assertTrue(java.util.Arrays.equals(dumped, redumped), "dump/undump must be byte-stable");

        try {
            Path dir = Path.of(System.getProperty("java.io.tmpdir"), "opencode", "cmp");
            Files.createDirectories(dir);
            Files.write(dir.resolve("smoke.lua"), src.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            Path out = dir.resolve("java.bin");
            Files.write(out, dumped);
            System.out.println("wrote " + out.toAbsolutePath() + " (" + dumped.length + " bytes)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void compareTestFilesWithC() throws Exception {
        String testDir = System.getProperty("opencode.testdir", "E:\\lua\\test");
        String lua53 = System.getProperty("opencode.lua53",
                Path.of(System.getenv("TEMP"), "opencode", "lua53build", "lua53.exe").toString());
        Path dir = Path.of(testDir);
        Path exe = Path.of(lua53);
        if (!Files.isDirectory(dir) || !Files.exists(exe)) {
            System.out.println("skip: testdir=" + dir + " lua53=" + exe);
            return;
        }
        String[] files = {
                "constructs.lua", "locals.lua", "literals.lua", "calls.lua",
                "closure.lua", "events.lua", "vararg.lua", "strings.lua",
                "math.lua", "sort.lua", "goto.lua", "attrib.lua",
                "bitwise.lua", "tpack.lua", "pm.lua", "coroutine.lua",
                "strings.lua", "nextvar.lua", "api.lua",
        };
        int ok = 0;
        int bad = 0;
        for (String name : files) {
            Path src = dir.resolve(name);
            if (!Files.exists(src)) {
                continue;
            }
            String chunkName = "@" + name;
            byte[] javaDump;
            try {
                String text = new String(Files.readAllBytes(src), java.nio.charset.StandardCharsets.ISO_8859_1);
                Proto p = LuaCompiler.compile(text, chunkName);
                javaDump = LuaDumper.dump(p, false);
            } catch (Throwable t) {
                System.out.println("COMPILE-FAIL " + name + ": " + t);
                t.printStackTrace(System.out);
                bad++;
                continue;
            }
            byte[] cDump = dumpWithC(exe, dir, name);
            if (cDump == null) {
                System.out.println("C-DUMP-FAIL " + name);
                bad++;
                continue;
            }
            if (java.util.Arrays.equals(javaDump, cDump)) {
                ok++;
            } else {
                bad++;
                System.out.println("MISMATCH " + name + " java=" + javaDump.length + " c=" + cDump.length);
                printFirstByteDiff(javaDump, cDump);
                try {
                    Proto a = LuaUndumper.undump(javaDump, "java");
                    Proto b = LuaUndumper.undump(cDump, "c");
                    diff(name, a, b);
                } catch (Throwable t) {
                    System.out.println("  (undump/diff failed: " + t + ")");
                }
            }
        }
        System.out.println("compareTestFilesWithC: identical=" + ok + " mismatch=" + bad);
        assertTrue(bad == 0, bad + " files mismatched");
    }

    private static byte[] dumpWithC(Path exe, Path dir, String name) throws Exception {
        String script = "local f=assert(loadfile('" + name + "')); "
                + "local d=string.dump(f,false); "
                + "local g=assert(io.open('c_tmp.bin','wb')); g:write(d); g:close()";
        ProcessBuilder pb = new ProcessBuilder(exe.toString(), "-e", script);
        pb.directory(dir.toFile());
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        byte[] out = proc.getInputStream().readAllBytes();
        proc.waitFor();
        Path tmp = dir.resolve("c_tmp.bin");
        if (!Files.exists(tmp)) {
            System.out.println(new String(out));
            return null;
        }
        byte[] data = Files.readAllBytes(tmp);
        Files.delete(tmp);
        return data;
    }

    private static void printFirstByteDiff(byte[] a, byte[] b) {
        int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) {
            if (a[i] != b[i]) {
                System.out.println("  firstByteDiff=" + i + " java=" + (a[i] & 0xFF) + " c=" + (b[i] & 0xFF));
                return;
            }
        }
    }

    @Test
    public void diffAgainstC() throws Exception {
        Path dir = Path.of(System.getProperty("java.io.tmpdir"), "opencode", "cmp");
        Path javaFile = dir.resolve("java.bin");
        Path cFile = dir.resolve("c.bin");
        if (!Files.exists(javaFile) || !Files.exists(cFile)) {
            return;
        }
        Proto a = LuaUndumper.undump(Files.readAllBytes(javaFile), "java");
        Proto b = LuaUndumper.undump(Files.readAllBytes(cFile), "c");
        if (diff("", a, b)) {
            System.out.println("NO STRUCTURAL DIFF");
        }
    }

    private static boolean diff(String path, Proto a, Proto b) {
        boolean d = false;
        d |= cmp(path + ".linedefined", a.linedefined, b.linedefined);
        d |= cmp(path + ".lastlinedefined", a.lastlinedefined, b.lastlinedefined);
        d |= cmp(path + ".numparams", a.numparams, b.numparams);
        d |= cmp(path + ".isVararg", a.isVararg, b.isVararg);
        d |= cmp(path + ".maxstacksize", a.maxstacksize, b.maxstacksize);
        if (a.sizek() != b.sizek()) {
            System.out.println("DIFF .sizek: java=" + a.sizek() + " c=" + b.sizek());
            java.util.Map<String, Integer> ja = constCounts(a);
            java.util.Map<String, Integer> ca = constCounts(b);
            java.util.Set<String> all = new java.util.TreeSet<>();
            all.addAll(ja.keySet());
            all.addAll(ca.keySet());
            for (String kk : all) {
                int jc = ja.getOrDefault(kk, 0);
                int cc2 = ca.getOrDefault(kk, 0);
                if (jc != cc2) {
                    System.out.println("  CONST java x" + jc + " c x" + cc2 + " : " + kk);
                }
            }
            d = true;
        }
        d |= cmp(path + ".sizecode", a.sizecode(), b.sizecode());
        d |= cmp(path + ".sizep", a.p.length, b.p.length);
        d |= cmp(path + ".sizelocvars", a.sizelocvars(), b.sizelocvars());
        d |= cmp(path + ".sizeupvalues", a.sizeupvalues(), b.sizeupvalues());
        if (a.sizek() == b.sizek()) {
            for (int i = 0; i < a.sizek(); i++) {
                d |= cmp(path + ".k[" + i + "]", a.k[i], b.k[i]);
            }
        }
        {
            int m = Math.min(a.sizecode(), b.sizecode());
            boolean found = false;
            for (int i = 0; i < m; i++) {
                if (a.code[i] != b.code[i]) {
                    System.out.println(path + " FIRST CODE DIFF at " + i);
                    printCode(a, "java", i - 4, i + 6);
                    printCode(b, "c   ", i - 4, i + 6);
                    d = true;
                    found = true;
                    break;
                }
            }
            if (!found && a.sizecode() != b.sizecode()) {
                int i = m;
                System.out.println(path + " CODE TAIL DIFF from " + i
                        + " (java len=" + a.sizecode() + " c len=" + b.sizecode() + ")");
                printCode(a, "java", i - 4, a.sizecode());
                printCode(b, "c   ", i - 4, b.sizecode());
                d = true;
            }
        }
        if (a.sizecode() == b.sizecode()) {
            int n = 0;
            for (int i = 0; i < a.sizecode(); i++) {
                int la = i < a.lineinfo.length ? a.lineinfo[i] : -1;
                int lb = i < b.lineinfo.length ? b.lineinfo[i] : -1;
                if (la != lb) {
                    n++;
                    if (n <= 20) {
                        System.out.println("DIFF " + path + ".lineinfo[" + i + "]: java=" + la + " c=" + lb);
                        printCode(a, "  java", i, i + 1);
                        printCode(b, "  c   ", i, i + 1);
                    }
                    d = true;
                }
            }
            if (n > 0) {
                System.out.println("  (" + path + ": " + n + " lineinfo diffs)");
            }
        }
        if (a.p.length == b.p.length) {
            for (int i = 0; i < a.p.length; i++) {
                d |= diff(path + ".p[" + i + "]", a.p[i], b.p[i]);
            }
        }
        return d;
    }

    private static String kList(Proto p) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < p.sizek(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(i).append(":").append(kStr(p.k[i]));
        }
        return sb.append("]").toString();
    }

    private static java.util.Map<String, Integer> constCounts(Proto p) {
        java.util.Map<String, Integer> m = new java.util.TreeMap<>();
        for (int i = 0; i < p.sizek(); i++) {
            String s = kStr(p.k[i]);
            m.merge(s, 1, Integer::sum);
        }
        return m;
    }

    private static String kStr(com.zhhz.truffle.lua.bytecode.LuaConstant k) {
        if (k instanceof com.zhhz.truffle.lua.bytecode.LuaConstant.Str s) {
            return "\"" + s.asString().replace("\n", "\\n") + "\"";
        }
        return k.toString();
    }

    private static boolean cmp(String what, Object x, Object y) {
        if (x instanceof com.zhhz.truffle.lua.bytecode.LuaConstant.Str sx
                && y instanceof com.zhhz.truffle.lua.bytecode.LuaConstant.Str sy) {
            if (sx.equals(sy)) {
                return false;
            }
            System.out.println("DIFF " + what + ": java=\"" + sx.asString() + "\" c=\"" + sy.asString() + "\"");
            return true;
        }
        if (java.util.Objects.equals(x, y)) {
            return false;
        }
        System.out.println("DIFF " + what + ": java=" + x + " c=" + y);
        return true;
    }

    private static void printAll(Proto p, String tag) {
        for (int i = 0; i < p.sizecode(); i++) {
            printCode(p, tag, i, i + 1);
        }
    }

    private static void printCode(Proto p, String tag, int from, int to) {
        for (int i = Math.max(0, from); i < Math.min(p.sizecode(), to); i++) {
            int ins = p.code[i];
            var op = com.zhhz.truffle.lua.bytecode.Instruction.getOpcode(ins);
            String args;
            if (op.opMode() == com.zhhz.truffle.lua.bytecode.OpMode.iABC) {
                args = "A=" + com.zhhz.truffle.lua.bytecode.Instruction.getA(ins)
                        + " B=" + com.zhhz.truffle.lua.bytecode.Instruction.getB(ins)
                        + " C=" + com.zhhz.truffle.lua.bytecode.Instruction.getC(ins);
            } else if (op.opMode() == com.zhhz.truffle.lua.bytecode.OpMode.iABx
                    || op.opMode() == com.zhhz.truffle.lua.bytecode.OpMode.iAsBx) {
                int bx = com.zhhz.truffle.lua.bytecode.Instruction.getBx(ins);
                args = "A=" + com.zhhz.truffle.lua.bytecode.Instruction.getA(ins) + " Bx=" + bx
                        + (op.opMode() == com.zhhz.truffle.lua.bytecode.OpMode.iAsBx
                                ? " sBx=" + (bx - com.zhhz.truffle.lua.bytecode.Instruction.MAXARG_sBx) : "");
            } else {
                args = "Ax=" + com.zhhz.truffle.lua.bytecode.Instruction.getAx(ins);
            }
            System.out.printf("%s %3d line=%3d %-9s %s%n",
                    tag, i, i < p.lineinfo.length ? p.lineinfo[i] : -1, op, args);
        }
    }

    private static String dumpRecString(Proto p) {
        StringBuilder sb = new StringBuilder();
        dumpRec(sb, p, "");
        return sb.toString();
    }

    @Test
    public void compilesPlainSource() {
        Proto p = compile("return 1 + 2 * 3");
        assertEquals(1, p.p.length == 0 ? 1 : p.p.length);
        assertTrue(p.sizecode() > 0);
    }

    @Test
    public void compilesWithNonAscii() {
        Proto p = compile("local s = \"中文\"\nreturn s");
        assertTrue(p.sizek() >= 1);
        byte[] dumped = LuaDumper.dump(p, false);
        assertTrue(dumped.length > 0);
    }
}
