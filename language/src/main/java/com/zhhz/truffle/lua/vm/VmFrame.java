package com.zhhz.truffle.lua.vm;

import com.zhhz.truffle.lua.bytecode.LocVar;
import com.zhhz.truffle.lua.bytecode.Proto;
import com.zhhz.truffle.lua.runtime.LuaCell;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 字节码 VM 的一个逻辑调用帧，供 {@code debug} 库做层级查询（getinfo/getlocal/traceback/hook）。
 *
 * <p>VM 内部沿用 {@code lvm.c} 的寄存器模型，整条 Lua 调用栈都运行在同一个 Truffle 帧内。
 * 因此这里显式维护每个 VM 调用对应的 Lua 级帧信息（函数、Proto、寄存器、当前 pc/行、
 * 以及「该函数是如何被调用的」name/namewhat），由 {@link LuaVm} 在进入/退出函数时压/弹栈，
 * {@code DebugLibrary} 读取。
 */
public final class VmFrame {

    private final LuaFunction function;
    private final Proto proto;
    Object[] regs;

    /** vararg 参数（不含固定参数）；无 vararg 时为空数组。 */
    Object[] varargs = new Object[0];

    /** 当前指令索引（用于解析活跃局部变量与行号）。 */
    int pc;

    /** 上次触发行钩子的指令索引（复刻 C 版 L->oldpc，用于回边强制重触发）。 */
    int lastHookPc = -1;

    /** 调用点解析出的函数名；null 表示无名。 */
    String name;

    /** 调用点解析出的 namewhat（"local"/"global"/"field"/"upvalue"/"method"/""）。 */
    String namewhat;

    VmFrame(LuaFunction function, Proto proto, Object[] regs) {
        this.function = function;
        this.proto = proto;
        this.regs = regs;
    }

    public LuaFunction function() {
        return function;
    }

    public Proto proto() {
        return proto;
    }

    public int currentLine() {
        int[] lineinfo = proto.lineinfo;
        if (lineinfo != null && pc >= 0 && pc < lineinfo.length) {
            return lineinfo[pc];
        }
        return -1;
    }

    public String name() {
        return name;
    }

    public String namewhat() {
        return namewhat;
    }

    /**
     * 第 n 个（1-based）活跃局部变量的名字；不存在返回 null。
     * 复刻 {@code luaF_getlocalname}：按 locvars 声明顺序，取第 n 个
     * {@code startpc <= pc < endpc} 的变量名。
     */
    public String localName(int n) {
        if (n <= 0) {
            return null;
        }
        LocVar[] locvars = proto.locvars;
        int remaining = n;
        for (int i = 0; i < locvars.length && locvars[i].startpc <= pc; i++) {
            if (pc < locvars[i].endpc) {
                remaining--;
                if (remaining == 0) {
                    return locvars[i].varname;
                }
            }
        }
        return null;
    }

    /** 第 n 个（1-based）局部变量的值（未捕获时直接读寄存器，捕获时解包 cell）。 */
    public Object localValue(int n) {
        if (n <= 0 || n - 1 >= regs.length) {
            return LuaNil.SINGLETON;
        }
        return unwrap(regs[n - 1]);
    }

    /** 写入第 n 个（1-based）局部变量；成功返回 true。 */
    public boolean setLocal(int n, Object value) {
        if (n <= 0 || n - 1 >= regs.length) {
            return false;
        }
        int idx = n - 1;
        Object existing = regs[idx];
        if (existing instanceof LuaCell cell) {
            cell.value = value;
        } else {
            regs[idx] = value;
        }
        return true;
    }

    /** vararg 第 n 个（1-based）值；越界返回 nil。 */
    public Object vararg(int n) {
        if (n < 1 || n > varargs.length) {
            return LuaNil.SINGLETON;
        }
        return varargs[n - 1];
    }

    public boolean setVararg(int n, Object value) {
        if (n < 1 || n > varargs.length) {
            return false;
        }
        varargs[n - 1] = value;
        return true;
    }

    public boolean isVararg() {
        return proto.isVararg != 0;
    }

    /** vararg 参数个数。 */
    public int varargCount() {
        return varargs.length;
    }

    private static Object unwrap(Object value) {
        if (value instanceof LuaCell cell) {
            return cell.value;
        }
        return value;
    }
}
