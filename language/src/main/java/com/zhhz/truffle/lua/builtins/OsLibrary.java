package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.lang.management.ManagementFactory;

/**
 * 实现了Lua的 'os' 内置库。
 */
public final class OsLibrary {

    @LuaBuiltin(name = "clock")
    @TruffleBoundary
    public static double clock() {
        // ... (实现 os.clock 的逻辑)
        return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() / 1.0e9;
    }

    @LuaBuiltin(name = "time")
    @TruffleBoundary
    public static long time() {
        // ... (实现 os.time 的逻辑)
        return System.currentTimeMillis() / 1000L;
    }

    @LuaBuiltin(name = "exit")
    @TruffleBoundary
    public static Object exit(Object[] args) {
        int status = 0;
        if (args.length > 0 && args[0] instanceof Long) {
            status = ((Long) args[0]).intValue();
        }
        System.exit(status);
        return LuaNil.SINGLETON; // Unreachable
    }
}
