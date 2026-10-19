package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;

/**
 * debug.sethook 的钩子触发点。支持标准 Lua 的四种事件：
 * <ul>
 *   <li>{@code "line"}：行钩子（'l' 掩码）。
 *       两个来源汇入 {@link #fireLine}：普通语句由 Truffle instrumentation
 *       （StatementTag 监听器，见 DebugLibrary.sethook）触发；控制流节点
 *       （if/while/repeat/for）在 AST 分支点显式触发，以复刻真实 Lua 字节码的
 *       行归属（then/else/end 关键字行、循环回边强制重触发、then 分支收尾跳
 *       end 行而 else 不跳、break 退出不触发 end 行等）。</li>
 *   <li>{@code "call"} / {@code "return"}：调用 / 返回钩子（'c'/'r' 掩码）。
 *       由 {@link #fireCall} / {@link #fireReturn} 在函数调用边界显式触发
 *       （见 LuaInvokeNode/LuaMethodCallNode）。</li>
 *   <li>{@code "count"}：指令计数钩子（count 参数）。由 {@link #fireCount}
 *       在语句边界触发；每 count 条“指令”（此处近似为语句计数）后触发一次。</li>
 * </ul>
 *
 * <p>连续去重：line 与上次相同且非 force 时不触发；循环回边（force）绕过去重。
 * 钩子执行期间（inHook）不再触发，防止递归。
 */
public final class LuaLineHook {

    /** 标准事件名（缓存为 TruffleString，避免每次构造）。 */
    private static final TruffleString EVENT_LINE = LuaContext.toTruffleString("line");
    private static final TruffleString EVENT_CALL = LuaContext.toTruffleString("call");
    private static final TruffleString EVENT_RETURN = LuaContext.toTruffleString("return");
    private static final TruffleString EVENT_COUNT = LuaContext.toTruffleString("count");

    private LuaLineHook() {
    }

    /** 触发一次行事件（line <= 0 表示无行信息，忽略）。 */
    public static void fireLine(Node location, int line, boolean force) {
        fireLine(LuaContext.get(location), line, force);
    }

    /** 触发一次行事件（VM 解释器循环直接调用，无需 Node）。 */
    public static void fireLine(LuaContext context, int line, boolean force) {
        if (line <= 0) {
            return;
        }
        if (!context.isHookMask('l')) {
            return;
        }
        Object hook = context.getHookFunction();
        if (hook == null || context.isInHook()) {
            return;
        }
        if (!force && line == context.getLastHookLine()) {
            return;
        }
        context.setLastHookLine(line);
        invokeHook(context, hook, EVENT_LINE, (long) line);
    }

    /** 触发一次 call 事件。调用前调用。 */
    public static void fireCall(Node location) {
        LuaContext context = LuaContext.get(location);
        if (!context.isHookMask('c')) {
            return;
        }
        Object hook = context.getHookFunction();
        if (hook == null || context.isInHook()) {
            return;
        }
        invokeHook(context, hook, EVENT_CALL, null);
    }

    /** 触发一次 return 事件。调用返回后调用。 */
    public static void fireReturn(Node location) {
        LuaContext context = LuaContext.get(location);
        if (!context.isHookMask('r')) {
            return;
        }
        Object hook = context.getHookFunction();
        if (hook == null || context.isInHook()) {
            return;
        }
        invokeHook(context, hook, EVENT_RETURN, null);
    }

    /** 触发一次 count 事件。语句计数达到阈值时调用。 */
    public static void fireCount(Node location) {
        fireCount(LuaContext.get(location));
    }

    /** 触发一次 count 事件（VM 解释器循环直接调用）。 */
    public static void fireCount(LuaContext context) {
        int count = context.getHookCount();
        if (count <= 0) {
            return;
        }
        Object hook = context.getHookFunction();
        if (hook == null || context.isInHook()) {
            return;
        }
        int next = context.getInstructionCount() + 1;
        if (next < count) {
            context.setInstructionCount(next);
            return;
        }
        // 达到阈值：触发并重置计数
        context.setInstructionCount(0);
        invokeHook(context, hook, EVENT_COUNT, null);
    }

    /** 内部：执行钩子函数，统一处理 interop 异常与重入保护。 */
    private static void invokeHook(LuaContext context, Object hook, TruffleString event, Long lineArg) {
        context.setInHook(true);
        try {
            if (lineArg != null) {
                InteropLibrary.getFactory().getUncached().execute(hook, event, lineArg);
            } else {
                InteropLibrary.getFactory().getUncached().execute(hook, event);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (ArityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedTypeException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedMessageException e) {
            throw new RuntimeException(e);
        } finally {
            context.setInHook(false);
        }
    }

    /**
     * sethook 设置钩子时调用：将 lastHookLine 锚定到 sethook 调用语句所在行，
     * 模拟标准 Lua 的 {@code L->oldpc = L->ci->u.l.savedpc}（当前指令行）。
     * 这样同行的后续语句会被去重，与标准 Lua 行为一致。
     * 钩子函数执行期间（inHook）不更新，防止钩子内部的函数调用干扰调用者帧的行去重。
     */
    public static void setLastLine(Node location, int line) {
        if (line <= 0) {
            return;
        }
        LuaContext context = LuaContext.get(location);
        if (context.isInHook()) {
            return;
        }
        context.setLastHookLine(line);
    }

    /** sethook 重置时使用：允许无位置上下文的安全调用。 */
    @TruffleBoundary
    public static void reset(LuaContext context) {
        context.setLastHookLine(-1);
        context.setInHook(false);
        context.setInstructionCount(0);
    }
}
