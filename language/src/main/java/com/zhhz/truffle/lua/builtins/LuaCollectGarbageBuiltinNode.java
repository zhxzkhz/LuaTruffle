package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@NodeInfo(shortName = "collectgarbage")
public abstract class LuaCollectGarbageBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doGarbage(Object[] arguments) {
        String option = "collect"; // 默认选项

        if (arguments.length > 0 && arguments[0] instanceof TruffleString) {
            option = ((TruffleString) arguments[0]).toJavaStringUncached();
        }

        return handleOption(option);
    }

    @TruffleBoundary
    private Object handleOption(String option) {
        switch (option) {
            // --- 我们可以【模拟】一些无害的选项 ---

            case "count":
                // 返回 JVM 使用的内存（以 KB 为单位的浮点数）
                // 这是一个合理的模拟
                MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
                long usedHeap = memBean.getHeapMemoryUsage().getUsed();
                return (double) usedHeap / 1024.0;

            case "isrunning":
                // JVM 的 GC 总是在后台“运行”，所以返回 true 是合理的
                return true;

            // --- 我们【忽略】那些有副作用的选项 ---

            case "collect":
            case "stop":
            case "restart":
            case "step":
                // 这些是控制 GC 的命令。我们不能控制 JVM 的 GC。
                // 最安全的做法是【什么都不做】。
                // `collectgarbage("collect")` 在 Lua 中没有返回值。
                // 我们可以返回 null 来代表 nil。
                System.gc(); // 【警告】可以尝试调用，但不保证立即执行！
                return LuaNil.SINGLETON;

            // ... 其他更复杂的选项，如 "setpause", "setstepmul" ...
            // 我们也忽略它们

            default:
                // 如果是无法识别的选项，可以返回 nil 或抛出错误
                return LuaNil.SINGLETON;
        }
    }
}