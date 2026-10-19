package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.nodes.Node;
import com.zhhz.truffle.lua.LuaException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 解释器中断协作机制。
 * <p>
 * 纯解释器（非 Graal 编译）运行时，{@code Context.interrupt()} 会整体中止 eval
 * 而不是以可捕获异常的方式展开（pcall 接不住）。因此宿主侧（如启动器的
 * --interrupt-file 轮询线程）通过 {@link #request()} 置位静态标志，
 * 循环节点在迭代边界调用 {@link #check(Node)} 自检并抛出普通的 LuaException，
 * 从而能被 pcall 捕获、脚本得以继续执行（与标准 lua 的 SIGINT 语义一致）。
 * <p>
 * 启动器进程内只有一个 Context，静态标志即进程级语义。
 */
public final class LuaInterrupt {

    private static final AtomicBoolean REQUESTED = new AtomicBoolean(false);

    private LuaInterrupt() {
    }

    /** 请求中断：下一次循环迭代边界抛出 "interrupted" 错误。 */
    public static void request() {
        REQUESTED.set(true);
    }

    /** 循环节点在每轮迭代时调用：若已有中断请求则消耗它并抛出可捕获的 Lua 错误。 */
    public static void check(Node location) {
        if (REQUESTED.get()) {
            REQUESTED.set(false);
            throw LuaException.create("interrupted", location);
        }
    }
}
