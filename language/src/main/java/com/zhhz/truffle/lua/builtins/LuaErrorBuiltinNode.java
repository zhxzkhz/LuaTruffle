package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaString;

/**
 * 实现了Lua的 'error' 内置函数。
 */
@NodeInfo(shortName = "error")
public abstract class LuaErrorBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary // 异常创建和栈遍历是慢速路径
    protected Object doError(Object[] arguments) {
        Object message = LuaNil.SINGLETON;
        long level = 1; // 默认 level 是 1

        // 1. 解析参数
        if (arguments.length >= 1) {
            message = arguments[0];
        }
        if (arguments.length >= 2) {
            level = parseLevel(arguments[1]);
        }

        // 2. 根据 'level' 参数查找错误位置
        Node errorLocation = findErrorLocation(level);

        // 3. 标准 Lua 行为：level >= 1 时为字符串消息添加 "source:line: " 位置前缀；
        //    level 0 与非字符串消息不加前缀
        Object errorObject = message;
        if (level > 0) {
            TruffleString messageString = asString(message);
            if (messageString != null && errorLocation != null) {
                SourceSection section = errorLocation.getEncapsulatingSourceSection();
                if (section != null && section.isAvailable()) {
                    errorObject = LuaContext.toTruffleString(
                            section.getSource().getName() + ":" + section.getStartLine() + ": "
                                    + messageString.toJavaStringUncached());
                }
            }
        }

        // 4. 抛出统一的运行时异常，errorObject 直接传递给 pcall
        throw LuaException.create(errorObject, errorLocation);
    }

    /**
     * 解析 level 参数。
     * <p>
     * 注意：Lua 数字在运行时以原生 {@link Long}/{@link Double} 表示（{@link LuaNumber}
     * 是遗留包装类，不会被构造），两者都必须接受，否则 level 会被静默忽略。
     */
    private static long parseLevel(Object value) {
        if (value instanceof LuaNumber number) {
            return (long) number.getValue();
        }
        if (value instanceof Long number) {
            return number;
        }
        if (value instanceof Double number && number == Math.rint(number)
                && !number.isInfinite() && !number.isNaN()) {
            return number.longValue();
        }
        throw LuaException.create("bad argument #2 to 'error' (number expected)", null);
    }

    private static TruffleString asString(Object value) {
        if (value instanceof TruffleString ts) {
            return ts;
        }
        if (value instanceof LuaString luaString) {
            return luaString.getValue();
        }
        return null;
    }

    /**
     * 根据 'level' 在 Truffle 调用栈中查找错误报告位置的节点。
     * <p>
     * 帧遍历顺序（内层优先）：第 1 帧是 error 内建自身（callNode 为 null），
     * 第 2 帧是调用 error 的函数，以此类推。帧的 callNode 是该帧内
     * 「调用其被调者」的节点，即该函数的当前执行位置。
     * 因此 Lua 语义 level N（第 N 层函数中 error 的位置 / 调用第 N 层函数的位置）
     * 对应第 N+1 帧的 callNode。
     *
     * @param level The stack level to report the error at.
     * @return 对应调用节点；level 0 返回 null；找不到时返回 this（无位置信息）
     */
    private Node findErrorLocation(long level) {
        if (level == 0) {
            return null; // level 0 表示不报告位置
        }
        Node callNode = frameCallNode(level + 1);
        return callNode != null ? callNode : this;
    }

    /**
     * 返回调用栈中第 index 帧（1-based，内层优先）的 callNode。
     * <p>
     * 【关键】{@link com.oracle.truffle.api.frame.FrameInstanceVisitor} 的契约是
     * 返回 null 继续遍历、返回非 null 终止遍历并作为结果——不是 boolean 语义。
     * 返回 {@code true} 会在第一帧后就静默终止遍历。
     */
    private static Node frameCallNode(long index) {
        Node[] found = {null};
        long[] current = {0};
        Truffle.getRuntime().iterateFrames(frameInstance -> {
            current[0]++;
            if (current[0] == index) {
                found[0] = frameInstance.getCallNode();
                return Boolean.TRUE; // 已找到，终止遍历
            }
            return null; // 返回 null 才会继续遍历后续帧
        });
        return found[0];
    }
}
