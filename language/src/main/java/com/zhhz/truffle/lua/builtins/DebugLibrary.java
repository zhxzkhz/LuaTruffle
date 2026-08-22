package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaRootNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * Lua 5.3 {@code debug} 标准库的实现。
 *
 * <p>debug 库是非性能关键的反射型 API，因此这里使用 {@link LuaBuiltin} +
 * 反射的批量注册模式（与 {@code os}/{@code math} 等库一致）。
 *
 * <p>栈帧信息通过 {@code Truffle.getRuntime().iterateFrames(...)} 获取。
 * 层级（level）语义遵循标准 Lua：level 1 表示调用 debug 函数的函数，
 * 内层（debug 内建自身的 BuiltinRootNode 帧）会被跳过。
 *
 * <p>限制（与宿主实现相关的取舍）：
 * <ul>
 *   <li>{@code sethook}/{@code gethook} 为占位实现（不触发钩子）。</li>
 *   <li>{@code getupvalue}/{@code setupvalue} 基于闭包的父帧命名槽位枚举，
 *       可能包含未被捕获的父函数局部变量（无精确的 upvalue 调试信息）。</li>
 *   <li>{@code name}/{@code namewhat}/{@code nparams}/{@code isvararg} 等
 *       依赖编译期调试信息的字段目前返回 nil/false。</li>
 * </ul>
 */
public final class DebugLibrary {

    private DebugLibrary() {
    }

    private static LuaContext context() {
        return LuaContext.get(null);
    }

    // =================================================================================
    // debug.getmetatable / debug.setmetatable
    // =================================================================================

    @LuaBuiltin(name = "getmetatable")
    @TruffleBoundary
    public static Object getMetatable(Object[] arguments) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'getmetatable' (value expected)", null);
        }
        LuaTable metatable = context().getEffectiveMetatable(arguments[0]);
        // 与全局 getmetatable 不同，debug 版本无视 __metatable 保护字段
        return metatable != null ? metatable : LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "setmetatable")
    @TruffleBoundary
    public static Object setMetatable(Object[] arguments) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaTable table)) {
            throw LuaException.create("bad argument #1 to 'setmetatable' (table expected)", null);
        }
        Object metatable = arguments.length > 1 ? arguments[1] : LuaNil.SINGLETON;
        if (metatable == null || metatable == LuaNil.SINGLETON) {
            table.setMetatable(null);
            return arguments[0];
        }
        if (!(metatable instanceof LuaTable luaMetatable)) {
            throw LuaException.create("bad argument #2 to 'setmetatable' (nil or table expected)", null);
        }
        table.setMetatable(luaMetatable);
        return arguments[0];
    }

    // =================================================================================
    // debug.getregistry
    // =================================================================================

    @LuaBuiltin(name = "getregistry")
    @TruffleBoundary
    public static LuaTable getRegistry(@SuppressWarnings("unused") Object[] arguments) {
        return context().getGlobals();
    }

    // =================================================================================
    // debug.getinfo
    // =================================================================================

    @LuaBuiltin(name = "getinfo")
    @TruffleBoundary
    public static Object getInfo(Object[] arguments) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'getinfo' (value expected)", null);
        }
        String what = "flnSrtu";
        if (arguments.length > 1 && stringValue(arguments[1]) != null) {
            what = stringValue(arguments[1]);
        }

        LuaContext context = context();
        Object target = arguments[0];

        if (target instanceof LuaNumber luaNumber) {
            long level = (long) luaNumber.getValue();
            LuaFrameInfo info = frameAtLevel(level);
            if (info == null) {
                return LuaNil.SINGLETON;
            }
            return info.toInfoTable(context, what, null);
        }
        if (target instanceof Number javaNumber) {
            long level = javaNumber.longValue();
            LuaFrameInfo info = frameAtLevel(level);
            if (info == null) {
                return LuaNil.SINGLETON;
            }
            return info.toInfoTable(context, what, null);
        }

        if (target instanceof LuaFunction function) {
            // 非激活函数：只返回静态可得的信息；若该函数正出现在调用栈中，
            // 则补充 currentline 等动态信息。
            LuaFrameInfo active = findActiveFrame(function);
            return new LuaFrameInfo(null, null, function).toInfoTable(context, what, active);
        }

        throw LuaException.create("bad argument #1 to 'getinfo' (number or function expected)", null);
    }

    // =================================================================================
    // debug.getlocal / debug.setlocal
    // =================================================================================

    @LuaBuiltin(name = "getlocal")
    @TruffleBoundary
    public static Object getLocal(Object[] arguments) {
        long level = integerArgument(arguments, 0, "getlocal");
        long n = integerArgument(arguments, 1, "getlocal");
        LuaFrameInfo info = frameAtLevel(level);
        if (info == null) {
            return LuaNil.SINGLETON;
        }
        Frame frame = info.frame();
        int slot = namedSlot(frame, (int) n);
        if (slot < 0) {
            return LuaNil.SINGLETON;
        }
        Object identifier = frame.getFrameDescriptor().getSlotName(slot);
        Object value = readSlot(frame, slot);
        return new com.zhhz.truffle.lua.runtime.LuaMultiValue(
                new Object[]{LuaContext.toTruffleString(identifier.toString()), value});
    }

    @LuaBuiltin(name = "setlocal")
    @TruffleBoundary
    public static Object setLocal(Object[] arguments) {
        long level = integerArgument(arguments, 0, "setlocal");
        long n = integerArgument(arguments, 1, "setlocal");
        Object value = arguments.length > 2 ? arguments[2] : LuaNil.SINGLETON;
        LuaFrameInfo info = frameAtLevel(level);
        if (info == null) {
            return LuaNil.SINGLETON;
        }
        if (!(info.frame() instanceof MaterializedFrame frame)) {
            return LuaNil.SINGLETON;
        }
        int slot = namedSlot(frame, (int) n);
        if (slot < 0) {
            return LuaNil.SINGLETON;
        }
        writeSlot(frame, slot, value);
        Object identifier = frame.getFrameDescriptor().getSlotName(slot);
        return LuaContext.toTruffleString(identifier.toString());
    }

    // =================================================================================
    // debug.getupvalue / debug.setupvalue
    // =================================================================================

    @LuaBuiltin(name = "getupvalue")
    @TruffleBoundary
    public static Object getUpvalue(Object[] arguments) {
        LuaFunction function = functionArgument(arguments, 0, "getupvalue");
        long n = integerArgument(arguments, 1, "getupvalue");
        Frame parent = function.getParentFrame();
        if (parent == null) {
            return LuaNil.SINGLETON;
        }
        int slot = namedSlot(parent, (int) n);
        if (slot < 0) {
            return LuaNil.SINGLETON;
        }
        Object identifier = parent.getFrameDescriptor().getSlotName(slot);
        return new com.zhhz.truffle.lua.runtime.LuaMultiValue(
                new Object[]{LuaContext.toTruffleString(identifier.toString()), readSlot(parent, slot)});
    }

    @LuaBuiltin(name = "setupvalue")
    @TruffleBoundary
    public static Object setUpvalue(Object[] arguments) {
        LuaFunction function = functionArgument(arguments, 0, "setupvalue");
        long n = integerArgument(arguments, 1, "setupvalue");
        Object value = arguments.length > 2 ? arguments[2] : LuaNil.SINGLETON;
        Frame parent = function.getParentFrame();
        if (parent == null) {
            return LuaNil.SINGLETON;
        }
        int slot = namedSlot(parent, (int) n);
        if (slot < 0) {
            return LuaNil.SINGLETON;
        }
        if (parent instanceof MaterializedFrame materialized) {
            writeSlot(materialized, slot, value);
        } else {
            return LuaNil.SINGLETON;
        }
        Object identifier = parent.getFrameDescriptor().getSlotName(slot);
        return LuaContext.toTruffleString(identifier.toString());
    }

    // =================================================================================
    // debug.upvalueid / debug.upvaluejoin（占位实现）
    // =================================================================================

    @LuaBuiltin(name = "upvalueid")
    @TruffleBoundary
    public static Object upvalueId(@SuppressWarnings("unused") Object[] arguments) {
        return LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "upvaluejoin")
    @TruffleBoundary
    public static Object upvalueJoin(@SuppressWarnings("unused") Object[] arguments) {
        return LuaNil.SINGLETON;
    }

    // =================================================================================
    // debug.traceback
    // =================================================================================

    @LuaBuiltin(name = "traceback")
    @TruffleBoundary
    public static Object traceback(Object[] arguments) {
        Object message = arguments.length > 0 ? arguments[0] : LuaNil.SINGLETON;
        long level = 1;
        if (arguments.length > 1) {
            level = integerArgument(arguments, 1, "traceback");
        }

        // 标准行为：非字符串消息时原样返回（pcall 的处理器依赖这一点）
        TruffleString messageString = stringValueTs(message);
        if (messageString != null || message == LuaNil.SINGLETON) {
            StringBuilder builder = new StringBuilder();
            if (messageString != null) {
                builder.append(messageString.toJavaStringUncached()).append('\n');
            }
            builder.append("stack traceback:");
            appendFrames(builder, level);
            return LuaContext.toTruffleString(builder.toString());
        }
        return message;
    }

    // =================================================================================
    // debug.sethook / debug.gethook / debug.debug（占位实现）
    // =================================================================================

    @LuaBuiltin(name = "sethook")
    @TruffleBoundary
    public static Object setHook(@SuppressWarnings("unused") Object[] arguments) {
        // 当前运行时不支持调试钩子，接受参数但不注册任何钩子
        return LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "gethook")
    @TruffleBoundary
    public static Object getHook(@SuppressWarnings("unused") Object[] arguments) {
        return LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "debug")
    @TruffleBoundary
    public static Object debug(@SuppressWarnings("unused") Object[] arguments) {
        // 标准 Lua 的 debug.debug()：在 stderr 上打印 "lua_debug> " 提示符，
        // 从 stdin 逐行读取命令并执行，读到 "cont" 或 EOF 时返回。
        // 逐字节读取以避免缓冲吞掉后续的 stdin 内容
        LuaContext context = context();
        java.io.PrintWriter error = context.getError();
        java.io.InputStream in = context.getEnv().in();
        while (true) {
            error.print("lua_debug> ");
            error.flush();
            String line = readLine(in);
            if (line == null || line.equals("cont")) {
                return LuaNil.SINGLETON;
            }
            try {
                Source source = Source.newBuilder(LuaLanguage.ID, line, "=(debug command)").build();
                CallTarget target = context.getEnv().parseInternal(source);
                target.call();
            } catch (Exception exception) {
                String message = exception.getMessage();
                error.println(message == null ? exception.getClass().getSimpleName() : message);
            }
        }
    }

    private static String readLine(java.io.InputStream in) {
        try {
            int c = in.read();
            if (c == -1) {
                return null;
            }
            StringBuilder line = new StringBuilder();
            while (c != -1 && c != '\n') {
                line.append((char) c);
                c = in.read();
            }
            int length = line.length();
            if (length > 0 && line.charAt(length - 1) == '\r') {
                line.setLength(length - 1);
            }
            return line.toString();
        } catch (java.io.IOException exception) {
            return null;
        }
    }

    // =================================================================================
    // 内部实现：调用栈遍历与帧信息
    // =================================================================================

    /** 一个 Lua 栈层级的快照：调用节点 + 物化帧（内建 C 函数的帧可能只有 root 名字）。 */
    private record LuaFrameInfo(Node callNode, Frame frame, LuaFunction function) {

        private RootNode root() {
            return callNode != null ? callNode.getRootNode() : null;
        }

        private boolean isLuaFrame() {
            return root() instanceof LuaRootNode;
        }

        private String functionName() {
            RootNode root = root();
            if (root == null) {
                return function != null && function.getName() != null
                        ? function.getName().toJavaStringUncached()
                        : "?";
            }
            String name = root.getName();
            if (name != null && !"anonymous".equals(name)) {
                return name;
            }
            // 主块（chunk）在标准 Lua 中显示为 "main chunk"
            if (root.getSourceSection() == null) {
                return "main chunk";
            }
            return "?";
        }

        private String what() {
            return isLuaFrame() ? "Lua" : "C";
        }

        private String shortSource() {
            SourceSection section = sourceSection();
            if (section != null) {
                return section.getSource().getName();
            }
            return "=?";
        }

        private SourceSection sourceSection() {
            RootNode root = root();
            if (root == null) {
                return null;
            }
            SourceSection rootSection = root.getSourceSection();
            // RootNode 的 section 覆盖函数定义（linedefined/lastlinedefined），
            // 调用节点的 section 给出 currentline
            return rootSection != null ? rootSection : null;
        }

        private int currentLine(LuaFrameInfo active) {
            LuaFrameInfo target = active != null ? active : this;
            Node node = target.callNode();
            if (node != null) {
                SourceSection section = node.getEncapsulatingSourceSection();
                if (section != null) {
                    return section.getStartLine();
                }
            }
            return -1;
        }

        private LuaTable toInfoTable(LuaContext context, String what, LuaFrameInfo active) {
            LuaTable table = new LuaTable(context.getTableShape());
            for (int i = 0; i < what.length(); i++) {
                switch (what.charAt(i)) {
                    case 'S' -> {
                        SourceSection section = sourceSection();
                        String source = "=?";
                        String shortSrc = "=?";
                        long lineDefined = -1L;
                        long lastLineDefined = -1L;
                        if (section != null) {
                            source = section.getSource().getName();
                            shortSrc = section.getSource().getName();
                            lineDefined = section.getStartLine();
                            lastLineDefined = section.getEndLine();
                        }
                        table.rawset("source", LuaContext.toTruffleString(source));
                        table.rawset("short_src", LuaContext.toTruffleString(shortSrc));
                        table.rawset("what", LuaContext.toTruffleString(what()));
                        table.rawset("linedefined", lineDefined);
                        table.rawset("lastlinedefined", lastLineDefined);
                    }
                    case 'l' -> table.rawset("currentline", (long) currentLine(active));
                    case 'n' -> {
                        table.rawset("name", LuaNil.SINGLETON);
                        table.rawset("namewhat", LuaNil.SINGLETON);
                    }
                    case 'f' -> table.rawset("func",
                            function != null ? function : LuaNil.SINGLETON);
                    case 'u' -> {
                        table.rawset("nups", 0L);
                        table.rawset("nparams", 0L);
                        table.rawset("isvararg", false);
                    }
                    case 't' -> table.rawset("istailcall", false);
                    case 'L' -> {
                        LuaTable lines = new LuaTable(context.getTableShape());
                        SourceSection section = sourceSection();
                        if (section != null) {
                            for (long line = section.getStartLine(); line <= section.getEndLine(); line++) {
                                lines.rawset((Object) line, true);
                            }
                        }
                        table.rawset("activelines", lines);
                    }
                    default -> {
                        // 忽略未知标志
                    }
                }
            }
            return table;
        }
    }

    /**
     * 返回调用栈中指定层级的帧信息。level 1 = 调用 debug 内建函数的帧；
     * 最内层（debug 内建自身的帧）被跳过。
     */
    private static LuaFrameInfo frameAtLevel(long level) {
        if (level <= 0) {
            return null;
        }
        LuaFrameInfo[] result = {null};
        long[] current = {0};
        Truffle.getRuntime().iterateFrames(frameInstance -> {
            // 跳过最内层帧（debug 内建自身）
            if (current[0] == 0) {
                current[0] = 1;
                return null;
            }
            long depth = ++current[0];
            if (depth == level + 1) {
                result[0] = toFrameInfo(frameInstance);
                return Boolean.TRUE; // 非空结果终止遍历
            }
            return null; // 返回 null 才会继续遍历后续帧
        });
        return result[0];
    }

    /** 在调用栈中查找指定函数的激活帧（用于对函数值调用 getinfo 时补充动态信息）。 */
    private static LuaFrameInfo findActiveFrame(LuaFunction function) {
        LuaFrameInfo[] result = {null};
        Truffle.getRuntime().iterateFrames(frameInstance -> {
            Node callNode = frameInstance.getCallNode();
            if (callNode != null && callNode.getRootNode() instanceof LuaRootNode luaRoot) {
                if (luaRoot.getCallTarget() == function.getCallTarget()) {
                    result[0] = toFrameInfo(frameInstance);
                    return Boolean.TRUE;
                }
            }
            return null;
        });
        return result[0];
    }

    private static LuaFrameInfo toFrameInfo(FrameInstance frameInstance) {
        Node callNode = frameInstance.getCallNode();
        Frame frame = frameInstance.getFrame(FrameAccess.MATERIALIZE);
        LuaFunction function = null;
        if (callNode != null && callNode.getRootNode() instanceof LuaRootNode luaRoot) {
            TruffleString name = luaRoot.getTSName();
            function = new LuaFunction(context().getFunctionShape(), luaRoot.getCallTarget(), null, name);
        }
        return new LuaFrameInfo(callNode, frame, function);
    }

    private static boolean isBuiltinRoot(FrameInstance frameInstance) {
        Node callNode = frameInstance.getCallNode();
        return callNode != null && callNode.getRootNode() instanceof BuiltinRootNode;
    }

    private static void appendFrames(StringBuilder builder, long startLevel) {
        long[] current = {0};
        Truffle.getRuntime().iterateFrames(frameInstance -> {
            // 跳过最内层帧（debug 内建自身）
            if (current[0] == 0) {
                current[0] = 1;
                return null;
            }
            long depth = ++current[0];
            if (depth < startLevel + 1) {
                return null;
            }
            LuaFrameInfo info = toFrameInfo(frameInstance);
            if (info.isLuaFrame()) {
                SourceSection callSection = info.callNode() != null
                        ? info.callNode().getEncapsulatingSourceSection()
                        : null;
                if ("main chunk".equals(info.functionName())) {
                    builder.append("\n\t").append(info.shortSource()).append(":")
                            .append(callSection != null ? callSection.getStartLine() : '?')
                            .append(": in main chunk");
                } else {
                    builder.append("\n\t").append(info.shortSource()).append(":")
                            .append(callSection != null ? callSection.getStartLine() : '?')
                            .append(": in function '").append(info.functionName()).append("'");
                }
            } else {
                builder.append("\n\t[C]: in function '").append(info.functionName()).append("'");
            }
            return null;
        });
    }

    // =================================================================================
    // 内部实现：帧槽位工具
    // =================================================================================

    /**
     * 返回帧中第 n 个（1-based）带字符串名字的槽位的索引；
     * 隐藏槽位（非 TruffleString 标识符）会被跳过。
     */
    private static int namedSlot(Frame frame, int n) {
        if (n <= 0) {
            return -1;
        }
        int count = 0;
        int slots = frame.getFrameDescriptor().getNumberOfSlots();
        for (int slot = 0; slot < slots; slot++) {
            if (frame.getFrameDescriptor().getSlotName(slot) instanceof TruffleString) {
                count++;
                if (count == n) {
                    return slot;
                }
            }
        }
        return -1;
    }

    /** 按槽位类型标记读取值，未写入的槽位视为 nil。 */
    private static Object readSlot(Frame frame, int slot) {
        FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(slot);
        try {
            if (kind == FrameSlotKind.Long) {
                return frame.getLong(slot);
            }
            if (kind == FrameSlotKind.Double) {
                return frame.getDouble(slot);
            }
            if (kind == FrameSlotKind.Boolean) {
                return frame.getBoolean(slot);
            }
            if (kind == FrameSlotKind.Object) {
                Object value = frame.getObject(slot);
                return value != null ? value : LuaNil.SINGLETON;
            }
            return LuaNil.SINGLETON;
        } catch (FrameSlotTypeException e) {
            return LuaNil.SINGLETON;
        }
    }

    /** 按槽位当前类型标记写入值（模仿 LuaWriteLocalVariableNode 的类型感知写入）。 */
    private static void writeSlot(Frame frame, int slot, Object value) {
        var descriptor = frame.getFrameDescriptor();
        FrameSlotKind kind = descriptor.getSlotKind(slot);
        if (value instanceof Long longValue && (kind == FrameSlotKind.Long || kind == FrameSlotKind.Illegal)) {
            descriptor.setSlotKind(slot, FrameSlotKind.Long);
            frame.setLong(slot, longValue);
        } else if (value instanceof Double doubleValue && (kind == FrameSlotKind.Double || kind == FrameSlotKind.Illegal)) {
            descriptor.setSlotKind(slot, FrameSlotKind.Double);
            frame.setDouble(slot, doubleValue);
        } else if (value instanceof Boolean booleanValue && (kind == FrameSlotKind.Boolean || kind == FrameSlotKind.Illegal)) {
            descriptor.setSlotKind(slot, FrameSlotKind.Boolean);
            frame.setBoolean(slot, booleanValue);
        } else {
            descriptor.setSlotKind(slot, FrameSlotKind.Object);
            frame.setObject(slot, value);
        }
    }

    // =================================================================================
    // 参数解析工具
    // =================================================================================

    private static long integerArgument(Object[] arguments, int index, String function) {
        Object value = arguments.length > index ? arguments[index] : null;
        if (value instanceof LuaNumber number) {
            return (long) number.getValue();
        }
        if (value instanceof Number javaNumber) {
            return javaNumber.longValue();
        }
        throw LuaException.create(
                "bad argument #" + (index + 1) + " to '" + function + "' (number expected)", null);
    }

    private static LuaFunction functionArgument(Object[] arguments, int index, String function) {
        if (arguments.length <= index || !(arguments[index] instanceof LuaFunction luaFunction)) {
            throw LuaException.create(
                    "bad argument #" + (index + 1) + " to '" + function + "' (function expected)", null);
        }
        return luaFunction;
    }

    private static String stringValue(Object value) {
        TruffleString ts = stringValueTs(value);
        return ts != null ? ts.toJavaStringUncached() : null;
    }

    private static TruffleString stringValueTs(Object value) {
        if (value instanceof TruffleString ts) {
            return ts;
        }
        if (value instanceof com.zhhz.truffle.lua.runtime.LuaString luaString) {
            return luaString.getValue();
        }
        return null;
    }
}
