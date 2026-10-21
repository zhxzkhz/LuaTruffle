package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 实现了Lua的 'error' 内置函数。
 */
@GenerateInline(false)
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

        // 2. 可字符串化的消息（字符串与数字——Lua 5.3.0 的 lua_isstring 语义）：
        //    level >= 1 时添加 "source:line: " 位置前缀，前缀后的字符串成为错误对象
        //    （与 5.3.0 的 lua_concat 行为一致，pcall 返回带前缀的字符串）；
        //    level 0 不加前缀，原对象保持不变。
        //    其余类型（表等）：无位置前缀，展示消息经 __tostring 格式化。
        String messageText = coercibleString(message);
        if (messageText != null) {
            Node errorLocation = findErrorLocation(level);
            if (level > 0 && errorLocation != null) {
                SourceSection section = errorLocation.getEncapsulatingSourceSection();
                if (section != null && section.isAvailable()) {
                    // 与 luaL_where 一致：源码名先经 luaO_chunkid 规范化（去掉 '@' 前缀等）。
                    Object errorObject = LuaContext.toTruffleString(
                            LuaException.chunkId(section.getSource().getName()) + ":"
                                    + section.getStartLine() + ": " + messageText);
                    throw LuaException.positioned(errorObject, String.valueOf(errorObject), errorLocation);
                }
            }
            // level == 0：不加前缀，且标记已处理，避免 VM 调用边界再补前缀。
            // level > 0 但位置不可用（如运行在字节码 VM 上）：保持无标记，
            // 交给 VM 在调用边界用当前 chunkid:line 补充前缀。
            if (level == 0) {
                throw LuaException.positioned(message, String.valueOf(message), errorLocation);
            }
            throw LuaException.create(message, errorLocation);
        }

        // 非字符串错误对象：在抛出前（此时调用栈仍然活着）经 tostring 内建格式化，
        // 使 __tostring 内的 debug.getinfo 能看到原始调用栈。
        // 帧链与标准 lua 的 message handler 一致：
        //   [__tostring(1), tostring 内建(2/C), error 内建(3/C), 调用者(4)]
        // 故 __tostring 内 getinfo(4).currentline == error() 调用行。
        // pcall 语义不受影响：errorObject 原样返回，只有展示消息被格式化。
        String display = formatErrorObject(message);
        throw LuaException.create(message, display, null);
    }

    /**
     * Lua 5.3.0 的 lua_isstring 语义：字符串与数字可强制转换为字符串（返回其文本），
     * 其余类型返回 null。
     */
    private static String coercibleString(Object value) {
        TruffleString ts = asString(value);
        if (ts != null) {
            return ts.toJavaStringUncached();
        }
        if (value instanceof Long number) {
            return number.toString();
        }
        if (value instanceof Double number) {
            return number.isInfinite() || number.isNaN()
                    ? Double.toString(number)
                    : (number == Math.rint(number) ? number.longValue() + ".0" : number.toString());
        }
        if (value instanceof LuaNumber number) {
            double d = number.getValue();
            return d == Math.rint(d) && !Double.isInfinite(d) ? (long) d + ".0" : Double.toString(d);
        }
        return null;
    }

    /**
     * 模拟标准 lua CLI message handler 对非字符串错误对象的处理：
     * 仅当对象有 __tostring 元方法时才（经 tostring 内建，保持帧深度）调用它，
     * 元方法不可用或不返回字符串时退回 "(error object is a %s value)" 占位描述
     * （与 lua.c msghandler 一致——注意不能直接对无元方法的对象调 tostring，
     * 那会得到 "table: 0x..." 而非占位描述）。
     */
    @TruffleBoundary
    private String formatErrorObject(Object errorObject) {
        LuaContext context = getContext();
        LuaTable metatable = context.getEffectiveMetatable(errorObject);
        boolean hasTostring = metatable != null
                && metatable.rawget(LuaMetatables.__TOSTRING) != LuaNil.SINGLETON;
        if (hasTostring && context.getTostringFunction() != null) {
            try {
                Object result = InteropLibrary.getFactory().getUncached()
                        .execute(context.getTostringFunction(), errorObject);
                if (result instanceof TruffleString ts) {
                    return ts.toJavaStringUncached();
                }
            } catch (Exception ignored) {
                // __tostring 出错或不返回字符串：退回占位描述
            }
        }
        return "(error object is a " + LuaTypesUtil.getTypeName(errorObject) + " value)";
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
