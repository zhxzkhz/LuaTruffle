package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
@GenerateInline(false)

@NodeInfo(shortName = "collectgarbage")
public abstract class LuaCollectGarbageBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    public Object doGarbage(Object[] arguments) {
        String option = "collect"; // 默认选项

        if (arguments.length > 0 && arguments[0] != null
                && arguments[0] != LuaNil.SINGLETON) {
            if (arguments[0] instanceof TruffleString ts) {
                option = ts.toJavaStringUncached();
            } else {
                String name = com.zhhz.truffle.lua.nodes.util.LuaTypesUtil
                        .globalFuncName(com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.currentFunction());
                throw com.zhhz.truffle.lua.LuaException.create("bad argument #1 to '"
                        + (name != null ? name : "collectgarbage") + "' (string expected, got "
                        + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(arguments[0])
                        + ")", null);
            }
        }

        return handleOption(option, arguments);
    }

    @TruffleBoundary
    private Object handleOption(String option, Object[] arguments) {
        switch (option) {
            // --- 我们可以【模拟】一些无害的选项 ---

            case "count":
                // 返回 JVM 使用的内存（以 KB 为单位的浮点数）
                // 这是一个合理的模拟
                MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
                long usedHeap = memBean.getHeapMemoryUsage().getUsed();
                return (double) usedHeap / 1024.0;

            case "isrunning":
                return getContext().isGcRunning();

            case "incremental":
            case "generational": {
                // 与 5.4 的 pushmode 一致：返回切换前的模式名
                String previous = getContext().getGcMode();
                getContext().setGcMode(option);
                return com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString(previous);
            }

            case "setpause": {
                long previous = getContext().getGcPause();
                getContext().setGcPause(numberArgument(arguments, 1, 0));
                return previous;
            }

            case "setstepmul": {
                long previous = getContext().getGcStepMul();
                getContext().setGcStepMul(numberArgument(arguments, 1, 0));
                return previous;
            }

            // --- 我们【忽略】那些有副作用的选项 ---

            case "collect":
            case "step":
            case "stop":
            case "restart": {
                if ("stop".equals(option)) {
                    getContext().setGcRunning(false);
                } else if ("restart".equals(option)) {
                    getContext().setGcRunning(true);
                } else {
                    System.gc(); // 【警告】可以尝试调用，但不保证立即执行！
                    // 模拟 FILE* 的 __gc 终结器：关闭已不可达的打开文件句柄。
                    getContext().collectFileHandles();
                }
                // 5.4 的 luaB_collectgarbage 对这些选项返回整数结果
                return "step".equals(option) ? Boolean.TRUE : 0L;
            }

            default:
                // 无法识别的选项：与标准 Lua 的 luaL_checkoption 一致
                {
                    String name = com.zhhz.truffle.lua.nodes.util.LuaTypesUtil
                            .globalFuncName(com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.currentFunction());
                    throw com.zhhz.truffle.lua.LuaException.create("bad argument #1 to '"
                            + (name != null ? name : "collectgarbage")
                            + "' (invalid option '" + option + "')", null);
                }
        }
    }

    /** 读取可选的数值参数；缺省（或 nil）时返回 {@code fallback}（5.4 的 luaL_optinteger 语义）。 */
    private static long numberArgument(Object[] arguments, int index, long fallback) {
        if (index >= arguments.length || arguments[index] == null
                || arguments[index] == LuaNil.SINGLETON) {
            return fallback;
        }
        Object value = arguments[index];
        if (value instanceof Long number) return number;
        if (value instanceof Double number) return number.longValue();
        throw com.zhhz.truffle.lua.LuaException.create("bad argument #" + (index + 1)
                + " to 'collectgarbage' (number expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value) + ")", null);
    }
}