package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventBinding;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.ExecutionEventListener;
import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaRootNode;
import com.zhhz.truffle.lua.runtime.LuaCell;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaLineHook;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaTable;
import com.zhhz.truffle.lua.runtime.LuaThread;

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
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'setmetatable' (value expected)", null);
        }
        Object metatable = arguments.length > 1 ? arguments[1] : LuaNil.SINGLETON;
        LuaTable newMetatable;
        if (metatable == null || metatable == LuaNil.SINGLETON) {
            newMetatable = null;
        } else if (metatable instanceof LuaTable luaMetatable) {
            newMetatable = luaMetatable;
        } else {
            throw LuaException.create("bad argument #2 to 'setmetatable' (nil or table expected)", null);
        }
        Object value = arguments[0];
        if (value instanceof LuaTable table) {
            table.setMetatable(newMetatable);
        } else {
            context().setTypeMetatable(value, newMetatable);
        }
        return value;
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
            return info.toInfoTable(context, what, null, level == 1 && context.isInHook());
        }
        if (target instanceof Number javaNumber) {
            long level = javaNumber.longValue();
            LuaFrameInfo info = frameAtLevel(level);
            if (info == null) {
                return LuaNil.SINGLETON;
            }
            return info.toInfoTable(context, what, null, level == 1 && context.isInHook());
        }

        if (target instanceof LuaFunction function) {
            // 非激活函数：只返回静态可得的信息；若该函数正出现在调用栈中，
            // 则补充 currentline 等动态信息。
            LuaFrameInfo active = findActiveFrame(function);
            return new LuaFrameInfo(null, null, null, function).toInfoTable(context, what, active, false);
        }

        throw LuaException.create("bad argument #1 to 'getinfo' (number or function expected)", null);
    }

    // =================================================================================
    // debug.getlocal / debug.setlocal
    // =================================================================================

    @LuaBuiltin(name = "getlocal")
    @TruffleBoundary
    public static Object getLocal(Object[] arguments) {
        // Lua 5.3: debug.getlocal ([thread,] f, var)
        // - thread 可选；f 可以是函数（返回参数名）或层级数（返回局部变量）
        int base = 0;
        if (arguments.length > 0 && arguments[0] instanceof LuaThread) {
            // TODO: 真正切到指定线程的栈；目前 main 线程下与默认一致
            base = 1;
        }
        Object first = arguments.length > base ? arguments[base] : null;
        if (first instanceof LuaFunction func) {
            long n = integerArgument(arguments, base + 1, "getlocal");
            String name = getParamName(func, (int) n);
            return name != null ? LuaContext.toTruffleString(name) : LuaNil.SINGLETON;
        }
        long level = integerArgument(arguments, base, "getlocal");
        long n = integerArgument(arguments, base + 1, "getlocal");
        LuaFrameInfo info = frameAtLevel(level);
        if (info == null) {
            throw LuaException.create("level out of range", null);
        }
        Frame frame = info.frame();
        // 负 n：返回 vararg（"(*vararg)" + 值）
        if (n < 0) {
            Object[] varargs = varargsOf(info, frame);
            int idx = (int) -n;
            if (idx < 1 || idx > varargs.length) {
                return LuaNil.SINGLETON;
            }
            return new com.zhhz.truffle.lua.runtime.LuaMultiValue(new Object[]{
                    LuaContext.toTruffleString("(*vararg)"), varargs[idx - 1]});
        }
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
            throw LuaException.create("level out of range", null);
        }
        if (!(info.frame() instanceof MaterializedFrame frame)) {
            return LuaNil.SINGLETON;
        }
        // 负 n：写入 vararg（返回 "(*vararg)"）
        if (n < 0) {
            int idx = (int) -n;
            if (idx < 1 || !setVararg(info, frame, idx - 1, value)) {
                return LuaNil.SINGLETON;
            }
            return LuaContext.toTruffleString("(*vararg)");
        }
        int slot = namedSlot(frame, (int) n);
        if (slot < 0) {
            return LuaNil.SINGLETON;
        }
        writeSlot(frame, slot, value);
        Object identifier = frame.getFrameDescriptor().getSlotName(slot);
        return LuaContext.toTruffleString(identifier.toString());
    }

    /**
     * 计算帧的 vararg 列表（frame.getArguments() 中除第一个上下文参数和固定参数外的部分）。
     * 返回的是对原始参数数组的【切片视图】（每次复制），写入需通过 {@link #setVararg} 修改原数组。
     */
    private static Object[] varargsOf(LuaFrameInfo info, Frame frame) {
        Object[] args;
        if (frame instanceof MaterializedFrame mf) {
            args = mf.getArguments();
        } else {
            args = frame.getArguments();
        }
        int fixedParamCount = -1;
        boolean variadic = false;
        if (info.callNode() != null && info.callNode().getRootNode() instanceof LuaRootNode root) {
            fixedParamCount = root.getParamCount();
            variadic = root.isVariadic();
        }
        if (!variadic || fixedParamCount < 0) {
            return new Object[0];
        }
        int start = 1 + fixedParamCount;
        if (start >= args.length) {
            return new Object[0];
        }
        Object[] varargs = new Object[args.length - start];
        System.arraycopy(args, start, varargs, 0, varargs.length);
        return varargs;
    }

    /** 直接在帧的原始参数数组上写入 vararg 值。 */
    private static boolean setVararg(LuaFrameInfo info, Frame frame, int varargIndex, Object value) {
        Object[] args;
        if (frame instanceof MaterializedFrame mf) {
            args = mf.getArguments();
        } else {
            args = frame.getArguments();
        }
        int fixedParamCount = -1;
        boolean variadic = false;
        if (info.callNode() != null && info.callNode().getRootNode() instanceof LuaRootNode root) {
            fixedParamCount = root.getParamCount();
            variadic = root.isVariadic();
        }
        if (!variadic || fixedParamCount < 0) {
            return false;
        }
        int start = 1 + fixedParamCount;
        int absIdx = start + varargIndex;
        if (absIdx < 0 || absIdx >= args.length) {
            return false;
        }
        args[absIdx] = value;
        return true;
    }

    // =================================================================================
    // debug.getupvalue / debug.setupvalue
    // =================================================================================

    private static LuaRootNode.UpvalueDescriptor[] upvalueDescriptors(LuaFunction function) {
        if (function.getCallTarget() instanceof RootCallTarget rct
                && rct.getRootNode() instanceof LuaRootNode root) {
            return root.getUpvalueDescriptors();
        }
        return null;
    }

    private static boolean isChunkFunction(LuaFunction function) {
        return function.getCallTarget() instanceof RootCallTarget rct
                && rct.getRootNode() instanceof LuaRootNode root && root.isChunk();
    }

    private static Object environmentOf(LuaFunction function) {
        Object environment = function.getEnvironment();
        return environment != null ? environment : context().getGlobals();
    }

    private static Object readCell(Object[] cells, int idx) {
        if (idx < 0 || idx >= cells.length) {
            return LuaNil.SINGLETON;
        }
        Object entry = cells[idx];
        Object value = entry instanceof LuaCell cell ? cell.value : entry;
        return value == null ? LuaNil.SINGLETON : value;
    }

    @LuaBuiltin(name = "getupvalue")
    @TruffleBoundary
    public static Object getUpvalue(Object[] arguments) {
        LuaFunction function = functionArgument(arguments, 0, "getupvalue");
        long n = integerArgument(arguments, 1, "getupvalue");
        if (n <= 0) {
            return LuaNil.SINGLETON;
        }
        if (isChunkFunction(function)) {
            return n == 1
                    ? new com.zhhz.truffle.lua.runtime.LuaMultiValue(
                            new Object[]{LuaContext.toTruffleString("_ENV"), environmentOf(function)})
                    : LuaNil.SINGLETON;
        }
        LuaRootNode.UpvalueDescriptor[] ups = upvalueDescriptors(function);
        Object[] cells = function.getUpvalues();
        if (ups == null || n > ups.length) {
            return LuaNil.SINGLETON;
        }
        return new com.zhhz.truffle.lua.runtime.LuaMultiValue(
                new Object[]{ups[(int) n - 1].name(), readCell(cells, (int) n - 1)});
    }

    @LuaBuiltin(name = "setupvalue")
    @TruffleBoundary
    public static Object setUpvalue(Object[] arguments) {
        LuaFunction function = functionArgument(arguments, 0, "setupvalue");
        long n = integerArgument(arguments, 1, "setupvalue");
        Object value = arguments.length > 2 ? arguments[2] : LuaNil.SINGLETON;
        if (n <= 0) {
            return LuaNil.SINGLETON;
        }
        if (isChunkFunction(function)) {
            if (n == 1) {
                function.setEnvironment(value);
                return LuaContext.toTruffleString("_ENV");
            }
            return LuaNil.SINGLETON;
        }
        LuaRootNode.UpvalueDescriptor[] ups = upvalueDescriptors(function);
        Object[] cells = function.getUpvalues();
        if (ups == null || n > ups.length) {
            return LuaNil.SINGLETON;
        }
        int idx = (int) n - 1;
        if (idx < cells.length) {
            if (cells[idx] instanceof LuaCell cell) {
                cell.value = value;
            } else {
                cells[idx] = new LuaCell(value);
            }
        }
        return ups[idx].name();
    }

    // =================================================================================
    // debug.upvalueid / debug.upvaluejoin
    // =================================================================================

    @LuaBuiltin(name = "upvalueid")
    @TruffleBoundary
    public static Object upvalueId(Object[] arguments) {
        LuaFunction function = functionArgument(arguments, 0, "upvalueid");
        long n = integerArgument(arguments, 1, "upvalueid");
        if (n <= 0) {
            throw LuaException.create("bad argument #2 to 'upvalueid' (invalid upvalue index)", null);
        }
        if (isChunkFunction(function)) {
            // 主 chunk 的 upvalue #1 是 _ENV；以函数自身作为稳定身份对象。
            if (n == 1) {
                return function;
            }
            throw LuaException.create("bad argument #2 to 'upvalueid' (invalid upvalue index)", null);
        }
        LuaRootNode.UpvalueDescriptor[] ups = upvalueDescriptors(function);
        if (ups == null) {
            // 非 Lua 闭包（如内置迭代器）：Lua 中它们带有内部 upvalue，这里以函数自身作为身份。
            if (n == 1) {
                return function;
            }
            throw LuaException.create("bad argument #2 to 'upvalueid' (invalid upvalue index)", null);
        }
        Object[] cells = function.getUpvalues();
        if (n > ups.length) {
            throw LuaException.create("bad argument #2 to 'upvalueid' (invalid upvalue index)", null);
        }
        int idx = (int) n - 1;
        if (idx >= cells.length || !(cells[idx] instanceof LuaCell cell)) {
            throw LuaException.create("bad argument #2 to 'upvalueid' (invalid upvalue index)", null);
        }
        // 同一 cell 始终返回同一身份对象；不同 cell 返回不同对象（供 == 比较）。
        if (cell.upvalueId == null) {
            cell.upvalueId = new com.zhhz.truffle.lua.runtime.LuaLightUserdata(cell);
        }
        return cell.upvalueId;
    }

    // =================================================================================
    // debug.getuservalue / debug.setuservalue
    // =================================================================================

    @LuaBuiltin(name = "getuservalue")
    @TruffleBoundary
    public static Object getuservalue(Object[] arguments) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'getuservalue' (value expected)", null);
        }
        // 我们只区分 full userdata 与 light userdata；没有真正的 user value 存储。
        if (arguments[0] instanceof com.zhhz.truffle.lua.runtime.LuaLightUserdata) {
            return LuaNil.SINGLETON;
        }
        return LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "setuservalue")
    @TruffleBoundary
    public static Object setuservalue(Object[] arguments) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'setuservalue' (userdata expected, got no value)", null);
        }
        Object target = arguments[0];
        if (target instanceof com.zhhz.truffle.lua.runtime.LuaLightUserdata) {
            throw LuaException.create(
                    "bad argument #1 to 'setuservalue' (userdata expected, got light userdata)", null);
        }
        throw LuaException.create("bad argument #1 to 'setuservalue' (userdata expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName(target) + ")", null);
    }

    @LuaBuiltin(name = "upvaluejoin")
    @TruffleBoundary
    public static Object upvalueJoin(Object[] arguments) {
        LuaFunction f1 = functionArgument(arguments, 0, "upvaluejoin");
        long n1 = integerArgument(arguments, 1, "upvaluejoin");
        LuaFunction f2 = functionArgument(arguments, 2, "upvaluejoin");
        long n2 = integerArgument(arguments, 3, "upvaluejoin");
        validateUpvalue(f1, n1, 2);
        validateUpvalue(f2, n2, 4);
        Object[] c1 = f1.getUpvalues();
        Object[] c2 = f2.getUpvalues();
        int i1 = (int) n1 - 1;
        int i2 = (int) n2 - 1;
        c1[i1] = c2[i2];
        return LuaNil.SINGLETON;
    }

    private static void validateUpvalue(LuaFunction function, long n, int argIndex) {
        boolean valid = false;
        if (n > 0) {
            if (isChunkFunction(function)) {
                valid = n == 1;
            } else {
                LuaRootNode.UpvalueDescriptor[] ups = upvalueDescriptors(function);
                if (ups != null && n <= ups.length) {
                    int idx = (int) n - 1;
                    Object[] cells = function.getUpvalues();
                    valid = idx < cells.length && cells[idx] != null;
                }
            }
        }
        if (!valid) {
            throw LuaException.create("bad argument #" + argIndex + " to 'upvaluejoin' (invalid upvalue index)", null);
        }
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
    // debug.sethook / debug.gethook
    // =================================================================================

    /**
     * 钩子实现（行/调用/返回/计数）。各事件的触发来源：
     * <ul>
     *   <li>'l' 行钩子：instrumentation 监听 StatementTag（普通语句）+
     *       控制流节点（if/while/repeat/for）AST 分支点显式触发（{@link LuaLineHook#fireLine}），
     *       复刻真实 Lua 字节码的行归属（then/else/end 行、循环回边强制重触发等）。</li>
     *   <li>'c' 调用钩子 / 'r' 返回钩子：instrumentation 监听 CallTag
     *       （{@link LuaInvokeNode}/{@link LuaMethodCallNode}）。
     *       onEnter → "call"；onReturnValue → "return"；onReturnExceptional → "return"
     *       （异常退出也算返回，否则上层不会收到 return 事件导致栈不平衡）。</li>
     *   <li>count 计数钩子：instrumentation 监听 StatementTag，每次语句执行
     *       计数 +1，达阈值触发 "count" 事件并重置计数。</li>
     * </ul>
     * 仅支持主线程钩子；协程级 sethook/gethook 暂不支持（首参数为 thread 时忽略）。
     */
    @LuaBuiltin(name = "sethook")
    @TruffleBoundary
    public static Object setHook(Object[] arguments) {
        LuaContext context = context();
        // 解除现有钩子
        detachHook(context);
        // 标准 Lua：可选的第一参数为 thread（协程），此处不支持，从下一参数开始取
        int base = 0;
        if (arguments.length > 0 && arguments[0] instanceof LuaTable) {
            // 协程级 sethook 未实现：忽略 thread 参数
            base = 1;
        }
        Object hookArg = arguments.length > base ? arguments[base] : null;
        if (hookArg == null || hookArg == LuaNil.SINGLETON) {
            return LuaNil.SINGLETON;
        }
        String mask = "";
        long count = 0;
        if (arguments.length > base + 1 && arguments[base + 1] instanceof TruffleString ts) {
            mask = ts.toJavaStringUncached();
        }
        if (arguments.length > base + 2) {
            count = integerArgument(arguments, base + 2, "sethook");
        }
        // 计数上界与标准 Lua 5.3 一致（2^24 - 1）
        if (count < 0) {
            count = 0;
        }
        Instrumenter instrumenter = context.lookupHookInstrumenter();
        if (instrumenter == null) {
            // 无 instrumenter 环境（受限嵌入）：仅记录函数
            context.setHookFunction(hookArg);
            context.setHookMask(mask);
            context.setHookCount((int) count);
            return LuaNil.SINGLETON;
        }
        boolean needLine = mask.indexOf('l') >= 0;
        boolean needCount = count > 0;
        // 行钩子与计数钩子都监听 StatementTag，可共用同一监听器
        if (needLine || needCount) {
            EventBinding<ExecutionEventListener> stmtBinding = instrumenter.attachExecutionEventListener(
                    SourceSectionFilter.newBuilder().tagIs(StandardTags.StatementTag.class).build(),
                    new ExecutionEventListener() {
                        @Override
                        public void onEnter(EventContext eventContext, VirtualFrame frame) {
                            if (needCount) {
                                LuaLineHook.fireCount(eventContext.getInstrumentedNode());
                            }
                            if (needLine) {
                                SourceSection section = eventContext.getInstrumentedSourceSection();
                                LuaLineHook.fireLine(eventContext.getInstrumentedNode(),
                                        section != null ? section.getStartLine() : -1, false);
                            }
                        }

                        @Override
                        public void onReturnValue(EventContext eventContext, VirtualFrame frame, Object result) {
                        }

                        @Override
                        public void onReturnExceptional(EventContext eventContext, VirtualFrame frame,
                                                         Throwable exception) {
                        }
                    });
            context.setHookBinding(stmtBinding);
        }
        // 调用/返回钩子监听 CallTag；行钩子也需 CallTag 以在函数返回时更新 lastHookLine
        // （模拟标准 Lua luaD_poscall 的 L->oldpc = ci->previous->u.l.savedpc）
        boolean needCall = mask.indexOf('c') >= 0;
        boolean needReturn = mask.indexOf('r') >= 0;
        if (needLine || needCall || needReturn) {
            EventBinding<ExecutionEventListener> callBinding = instrumenter.attachExecutionEventListener(
                    SourceSectionFilter.newBuilder().tagIs(StandardTags.CallTag.class).build(),
                    new ExecutionEventListener() {
                        @Override
                        public void onEnter(EventContext eventContext, VirtualFrame frame) {
                            if (needCall) {
                                LuaLineHook.fireCall(eventContext.getInstrumentedNode());
                            }
                        }

                        @Override
                        public void onReturnValue(EventContext eventContext, VirtualFrame frame, Object result) {
                            if (needReturn) {
                                LuaLineHook.fireReturn(eventContext.getInstrumentedNode());
                            }
                            if (needLine) {
                                // 函数返回时，将 lastHookLine 锚定到调用语句行，
                                // 使调用者中同行的后续语句被去重
                                SourceSection section = eventContext.getInstrumentedSourceSection();
                                if (section != null) {
                                    LuaLineHook.setLastLine(eventContext.getInstrumentedNode(),
                                            section.getStartLine());
                                }
                            }
                        }

                        @Override
                        public void onReturnExceptional(EventContext eventContext, VirtualFrame frame,
                                                         Throwable exception) {
                            if (needReturn) {
                                LuaLineHook.fireReturn(eventContext.getInstrumentedNode());
                            }
                            if (needLine) {
                                SourceSection section = eventContext.getInstrumentedSourceSection();
                                if (section != null) {
                                    LuaLineHook.setLastLine(eventContext.getInstrumentedNode(),
                                            section.getStartLine());
                                }
                            }
                        }
                    });
            if (context.getHookBinding() != null) {
                // 已存在 StatementTag 监听器：将 CallTag 监听器记入副绑定列表
                context.addExtraHookBinding(callBinding);
            } else {
                context.setHookBinding(callBinding);
            }
        }
        context.setHookFunction(hookArg);
        context.setHookMask(mask);
        context.setHookCount((int) count);
        // 模拟标准 Lua lua_sethook 的 L->oldpc = L->ci->u.l.savedpc：
        // 将 lastHookLine 锚定到 sethook 调用语句所在行，使同行的后续语句被去重
        LuaFrameInfo caller = frameAtLevel(1);
        int callerLine = -1;
        if (caller != null && caller.callNode() != null) {
            SourceSection section = caller.callNode().getEncapsulatingSourceSection();
            if (section != null) {
                callerLine = section.getStartLine();
            }
        }
        context.setLastHookLine(callerLine);
        context.setInHook(false);
        context.setInstructionCount(0);
        return LuaNil.SINGLETON;
    }

    private static void detachHook(LuaContext context) {
        com.oracle.truffle.api.instrumentation.EventBinding<?> binding = context.getHookBinding();
        if (binding != null) {
            binding.dispose();
            context.setHookBinding(null);
        }
        for (com.oracle.truffle.api.instrumentation.EventBinding<?> extra : context.getExtraHookBindings()) {
            extra.dispose();
        }
        context.clearExtraHookBindings();
        context.setHookFunction(null);
        context.setHookMask("");
        context.setHookCount(0);
        context.setInstructionCount(0);
        context.setLastHookLine(-1);
        context.setInHook(false);
    }

    @LuaBuiltin(name = "gethook")
    @TruffleBoundary
    public static Object getHook(Object[] arguments) {
        LuaContext context = context();
        // 标准 Lua：可选第一参数为 thread，此处不支持；忽略
        Object hook = context.getHookFunction();
        TruffleString mask = LuaContext.toTruffleString(context.getHookMask());
        long count = context.getHookCount();
        if (hook == null) {
            // 标准 Lua：无钩子时返回 nil（多值形式的第一项）
            return new LuaMultiValue(new Object[]{LuaNil.SINGLETON, mask, count});
        }
        return new LuaMultiValue(new Object[]{hook, mask, count});
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

    /**
     * 一个 Lua 栈层级的快照。{@code callNode} 是该帧当前执行位置
     * （currentline 的来源，帧每次嵌套调用都会更新）；{@code entryCallNode}
     * 是进入该帧的那次调用（来自调用者帧的 callNode，name/namewhat 的来源）。
     */
    private record LuaFrameInfo(Node callNode, Node entryCallNode, Frame frame, LuaFunction function) {

        private RootNode root() {
            if (callNode != null) {
                return callNode.getRootNode();
            }
            // 函数值查询（非激活帧）：从函数自身的 CallTarget 取 RootNode；
            // 内建函数的 root 是 BuiltinRootNode → 判为 C 函数
            if (function != null && function.getCallTarget() instanceof RootCallTarget rootCallTarget) {
                return rootCallTarget.getRootNode();
            }
            return null;
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
                // callNode 通常是 Truffle 的 DirectCallNode，其封装 section 来自父
                // LuaInvokeNode/LuaMethodCallNode。向上查找最近的 LuaStatementNode
                // 以获取词法器报告的行号（标准 Lua 行号语义），避免 Truffle Source
                // 行号映射对 \n\r 等换行序列的处理与标准 Lua 不一致。
                Node cur = node;
                while (cur != null && !(cur instanceof com.zhhz.truffle.lua.nodes.LuaStatementNode)) {
                    cur = cur.getParent();
                }
                if (cur instanceof com.zhhz.truffle.lua.nodes.LuaStatementNode luaNode) {
                    if (luaNode.hasSourceLine()) {
                        int sl = luaNode.getSourceLine();
                        if (sl > 0) {
                            return sl;
                        }
                    }
                }
                SourceSection section = node.getEncapsulatingSourceSection();
                if (section != null) {
                    return section.getStartLine();
                }
            }
            return -1;
        }

        private LuaTable toInfoTable(LuaContext context, String what, LuaFrameInfo active, boolean asHook) {
            // 标准 Lua：what 中出现非法字符时报错（如 "X"）
            for (int i = 0; i < what.length(); i++) {
                if ("flnSrtuL".indexOf(what.charAt(i)) < 0) {
                    throw LuaException.create("invalid option '" + what.charAt(i) + "'", null);
                }
            }
            LuaTable table = new LuaTable(context.getTableShape());
            for (int i = 0; i < what.length(); i++) {
                switch (what.charAt(i)) {
                    case 'S' -> {
                        boolean lua = isLuaFrame();
                        SourceSection section = sourceSection();
                        // C 函数：source/short_src 为 "[C]"，无定义行
                        String source = "[C]";
                        String shortSrc = "[C]";
                        long lineDefined = -1L;
                        long lastLineDefined = -1L;
                        if (lua && section != null) {
                            // 文件型 Source 视为 "@path"（真实 Lua 的源名格式），
                            // 其余用 Source 的名字（load 传入的 chunk 名或 chunk 文本）
                            String rawName = section.getSource().getPath() != null
                                    ? "@" + section.getSource().getName()
                                    : section.getSource().getName();
                            source = rawName;
                            shortSrc = chunkId(rawName);
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
                        // name/namewhat 来自进入该帧的那次调用（调用者帧的当前位置），
                        // 而非该帧自己的当前位置
                        // 钩子函数被触发执行时，level 1 的 namewhat 为 "hook"
                        String[] nameWhat;
                        if (asHook) {
                            nameWhat = new String[]{"hook", "hook"};
                        } else {
                            Node callSite = entryCallNode != null ? entryCallNode : callNode;
                            nameWhat = resolveNameWhat(callSite);
                        }
                        table.rawset("name", nameWhat[0] != null
                                ? LuaContext.toTruffleString(nameWhat[0]) : LuaNil.SINGLETON);
                        table.rawset("namewhat", LuaContext.toTruffleString(nameWhat[1]));
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
                        // C 函数无 activelines（字段不存在，读取得 nil）
                        if (!isLuaFrame()) {
                            continue;
                        }
                        LuaTable lines = new LuaTable(context.getTableShape());
                        SourceSection section = sourceSection();
                        if (section != null) {
                            // 近似：从函数体首行（linedefined+1）到 end 行。
                            // 真实 Lua 中 `function` 关键字行无活跃代码
                            for (long line = section.getStartLine() + 1; line <= section.getEndLine(); line++) {
                                lines.rawset((Object) line, true);
                            }
                        }
                        table.rawset("activelines", lines);
                    }
                    default -> {
                        // 已在上方校验，不会到达
                    }
                }
            }
            return table;
        }
    }

    /**
     * 返回调用栈中指定层级的帧信息。level 1 = 调用 debug 内建函数的帧；
     * 最内层的非 Lua 帧（interop 分发帧 / debug 内建自身的 BuiltinRootNode 帧）被跳过。
     * 同时捕获 level+1 帧（调用者）的 callNode 作为 entryCallNode——
     * 它是进入 level 帧的那次调用，getinfo 的 name/namewhat 由此解析。
     */
    private static LuaFrameInfo frameAtLevel(long level) {
        if (level <= 0) {
            return null;
        }
        LuaFrameInfo[] result = {null};
        Node[] entryCall = {null};
        boolean[] started = {false};
        long[] current = {0};
        Truffle.getRuntime().iterateFrames(frameInstance -> {
            if (!started[0]) {
                if (!isLuaStackFrame(frameInstance)) {
                    return null; // 最内层的分发/内建帧，跳过
                }
                started[0] = true; // 第一个 Lua 帧 = level 1
            }
            current[0]++;
            if (current[0] == level) {
                result[0] = toFrameInfo(frameInstance);
                return null; // 继续一层取调用者帧的 callNode
            }
            if (current[0] == level + 1) {
                // 调用者是 Lua 帧才有入口调用；主块之上是 polyglot 宿主帧 → 无名
                if (isLuaStackFrame(frameInstance)) {
                    entryCall[0] = frameInstance.getCallNode();
                }
                return Boolean.TRUE; // 非空结果终止遍历
            }
            return null; // 返回 null 才会继续遍历后续帧
        });
        if (result[0] != null && entryCall[0] != null) {
            return new LuaFrameInfo(result[0].callNode(), entryCall[0], result[0].frame(), result[0].function());
        }
        return result[0];
    }

    /**
     * 是否为 Lua 语言的栈帧。以帧自身的 CallTarget 判定：
     * target 为 LuaRootNode → Lua 帧；BuiltinRootNode → 内建（C 函数）帧；
     * 无 target（interop 分发帧）→ 跳过。
     * （不能用 callNode 的 root 判定——callNode 属于调用方的 AST。）
     */
    private static boolean isLuaStackFrame(FrameInstance frameInstance) {
        CallTarget target = frameInstance.getCallTarget();
        if (target instanceof RootCallTarget rootCallTarget) {
            return !(rootCallTarget.getRootNode() instanceof BuiltinRootNode);
        }
        return false;
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
        return new LuaFrameInfo(callNode, null, frame, function);
    }

    private static boolean isBuiltinRoot(FrameInstance frameInstance) {
        Node callNode = frameInstance.getCallNode();
        return callNode != null && callNode.getRootNode() instanceof BuiltinRootNode;
    }

    private static void appendFrames(StringBuilder builder, long startLevel) {
        boolean[] started = {false};
        long[] current = {0};
        LuaContext ctx = context();
        boolean inHook = ctx.isInHook();
        Truffle.getRuntime().iterateFrames(frameInstance -> {
            // 跳过最内层的非 Lua 帧（interop 分发帧 / debug 内建自身的帧）
            if (!started[0]) {
                if (!isLuaStackFrame(frameInstance)) {
                    return null;
                }
                started[0] = true;
                current[0] = 1;
            } else {
                current[0]++;
            }
            if (current[0] < startLevel) {
                return null;
            }
            LuaFrameInfo info = toFrameInfo(frameInstance);
            if (info.isLuaFrame()) {
                SourceSection callSection = info.callNode() != null
                        ? info.callNode().getEncapsulatingSourceSection()
                        : null;
                // 钩子函数被触发执行时，level 1 帧在 traceback 中显示为 "in hook"
                if (inHook && current[0] == 1) {
                    builder.append("\n\t").append(info.shortSource()).append(":")
                            .append(callSection != null ? callSection.getStartLine() : '?')
                            .append(": in hook");
                } else if ("main chunk".equals(info.functionName())) {
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
     * 返回非活跃 Lua 函数的第 n 个（1-based）参数名。
     * 从函数的 CallTarget → RootNode → FrameDescriptor 获取前 n 个带字符串名的槽位。
     */
    private static String getParamName(LuaFunction func, int n) {
        if (n <= 0) {
            return null;
        }
        if (func.getCallTarget() instanceof RootCallTarget rct
                && rct.getRootNode() instanceof LuaRootNode rootNode) {
            int paramCount = rootNode.getParamCount();
            if (paramCount >= 0 && n > paramCount) {
                return null;
            }
            FrameDescriptor fd = rootNode.getFrameDescriptor();
            int count = 0;
            for (int slot = 0; slot < fd.getNumberOfSlots(); slot++) {
                if (fd.getSlotName(slot) instanceof TruffleString ts) {
                    count++;
                    if (count == n) {
                        return ts.toJavaStringUncached();
                    }
                }
            }
        }
        return null;
    }

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

    /**
     * 从调用节点解析 (name, namewhat)：向上找到 LuaInvokeNode/LuaMethodCallNode，
     * 检查被调函数表达式的形态——局部变量读取 → local、upvalue 读取 → upvalue、
     * 全局读取 → global、表字段读取 → field、冒号方法调用 → method。
     */
    private static String[] resolveNameWhat(Node callSite) {
        Node node = callSite;
        while (node != null) {
            if (node instanceof com.zhhz.truffle.lua.nodes.expression.LuaMethodCallNode method) {
                com.zhhz.truffle.lua.nodes.LuaExpressionNode key = method.getDebugMethodKeyNode();
                if (key instanceof com.zhhz.truffle.lua.nodes.expression.LuaStringLiteralNode literal) {
                    TruffleString methodName = literal.getUnresolvedVariableName();
                    return new String[]{
                            methodName != null ? methodName.toJavaStringUncached() : null, "method"};
                }
                return new String[]{null, "method"};
            }
            if (node instanceof com.zhhz.truffle.lua.nodes.expression.LuaInvokeNode invoke) {
                com.zhhz.truffle.lua.nodes.LuaExpressionNode fn = invoke.getDebugFunctionNode();
                if (fn == null) {
                    break;
                }
                TruffleString name = fn.getUnresolvedVariableName();
                String what;
                if (fn instanceof com.zhhz.truffle.lua.nodes.access.LuaReadLocalVariableNode) {
                    what = "local";
                } else if (fn instanceof com.zhhz.truffle.lua.nodes.access.LuaReadUpvalueNode) {
                    what = "upvalue";
                } else if (fn instanceof com.zhhz.truffle.lua.nodes.access.LuaReadGlobalVariableNode) {
                    what = "global";
                } else if (fn instanceof com.zhhz.truffle.lua.nodes.access.LuaReadTableNode) {
                    what = "field";
                } else {
                    break;
                }
                return new String[]{name != null ? name.toJavaStringUncached() : null, what};
            }
            node = node.getParent();
        }
        return new String[]{null, ""};
    }

    /**
     * 复刻 Lua 5.3 的 luaO_chunkid（LUA_IDSIZE=60）：
     * "="名 → 去掉等号，超长右截断到 59；
     * "@名" → 去掉 @，超长则 "..." + 尾部 57 字符；
     * 其余 → [string "首行"]，首行超长截到 45 字符 + "..."，
     *        含换行（即使首行为空）也走截断分支（空首行 → [string "..."]）。
     */
    private static String chunkId(String source) {
        final int idSize = 60;
        if (source.startsWith("=")) {
            String name = source.substring(1);
            return name.length() <= idSize - 1 ? name : name.substring(0, idSize - 1);
        }
        if (source.startsWith("@")) {
            String name = source.substring(1);
            if (name.length() <= idSize - 1) {
                return name;
            }
            return "..." + name.substring(name.length() - (idSize - 4));
        }
        int nl = source.indexOf('\n');
        int budget = idSize - 15; // 前缀9 + 省略号3 + 后缀2 + '\0'
        int length = source.length();
        if (length < budget && nl < 0) {
            return "[string \"" + source + "\"]";
        }
        if (nl >= 0) {
            length = nl;
        }
        if (length > budget) {
            length = budget;
        }
        return "[string \"" + source.substring(0, length) + "...\"]";
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
