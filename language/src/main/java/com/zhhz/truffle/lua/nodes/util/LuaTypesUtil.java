package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaValue;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

/**
 * 包含了与 Lua 类型系统相关的静态辅助方法的工具类。
 */
public final class LuaTypesUtil {

    // 私有构造函数，防止实例化
    private LuaTypesUtil() {}

    // --- 【优化】预先创建并缓存所有可能的返回值 ---
    public static final TruffleString T_NIL = toTruffleString("nil");
    public static final TruffleString T_NUMBER = toTruffleString("number");
    public static final TruffleString T_STRING = toTruffleString("string");
    public static final TruffleString T_BOOLEAN = toTruffleString("boolean");
    public static final TruffleString T_TABLE = toTruffleString("table");
    public static final TruffleString T_FUNCTION = toTruffleString("function");
    public static final TruffleString T_USERDATA = toTruffleString("userdata");
    public static final TruffleString T_THREAD = toTruffleString("thread");

    /**
     * 把宿主 interop 值转换成 Lua 值：宿主的 null 映射为 Lua nil。
     */
    public static Object normalizeHostValue(Object value) {
        if (value == null) {
            return LuaNil.SINGLETON;
        }
        if (value instanceof LuaValue) {
            return value;
        }
        if (com.oracle.truffle.api.interop.InteropLibrary.getUncached().isNull(value)) {
            return LuaNil.SINGLETON;
        }
        return value;
    }
    /**
     * 【核心】实现 Lua 的“真值”判断规则。
     * 在 Lua 中，只有 false 和 nil 被认为是假 (falsy)。
     *
     * @param value 任何 Lua 值 (作为 Object)。
     * @return {@code true} 如果值是 truthy，{@code false} 如果值是 falsy。
     */
    public static boolean isTruthy(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof LuaBoolean luaBoolean) return luaBoolean.asBoolean();
        return value != null && value != LuaNil.SINGLETON;
    }

    public static String getTypeName(Object value) {
        if (value == null || value == LuaNil.SINGLETON) {
            return "nil";
        }
        if (value instanceof LuaBoolean || value instanceof Boolean) {
            return "boolean";
        }
        if (value instanceof Long || value instanceof Double || value instanceof Integer
                || value instanceof Float || value instanceof Short || value instanceof Byte) {
            return "number";
        }
        if (value instanceof TruffleString || value instanceof String) {
            return "string";
        }
        if (value instanceof LuaValue luaValue) {
            return luaValue.getTypeName();
        }
        return "userdata";
    }

    /**
     * 比较运算的类型错误消息，与标准 Lua 的 {@code luaG_ordererror} 一致：
     * 同类型为 {@code attempt to compare two <type> values}，
     * 否则为 {@code attempt to compare <t1> with <t2>}。
     */
    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    public static String orderErrorMessage(Object first, Object second) {
        String t1 = getTypeName(first);
        String t2 = getTypeName(second);
        if (t1.equals(t2)) {
            return "attempt to compare two " + t1 + " values";
        }
        return "attempt to compare " + t1 + " with " + t2;
    }

    /**
     * 参数检查错误消息里 "got X" 所用的类型名，等价于 lauxlib 的 {@code typeerror}：
     * 优先使用元表的 {@code __name} 字段；light userdata 特判为 {@code light userdata}；
     * 否则退回基本类型名。
     */
    public static String getArgumentTypeName(Object value) {
        if (value == null || value == LuaNil.SINGLETON) {
            return "nil";
        }
        if (value instanceof com.zhhz.truffle.lua.runtime.LuaLightUserdata) {
            return "light userdata";
        }
        com.zhhz.truffle.lua.runtime.LuaContext context =
                com.zhhz.truffle.lua.runtime.LuaContext.get(null);
        if (context != null) {
            com.zhhz.truffle.lua.runtime.LuaTable metatable = context.getEffectiveMetatable(value);
            if (metatable != null) {
                Object name = metatable.rawget(com.zhhz.truffle.lua.runtime.LuaMetatables.__NAME,
                        com.oracle.truffle.api.object.DynamicObjectLibrary.getUncached());
                if (name instanceof TruffleString ts) {
                    return ts.toJavaStringUncached();
                }
            }
        }
        return getTypeName(value);
    }

    /**
     * 等价于标准 Lua 的 {@code pushglobalfuncname}：在 {@code package.loaded} 里按
     * 「模块名」和「模块名.字段」两层查找给定函数，返回其限定名（如 {@code "io.write"}、
     * {@code "collectgarbage"}）。找不到返回 null。
     */
    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    public static String globalFuncName(Object function) {
        if (function == null) {
            return null;
        }
        com.zhhz.truffle.lua.runtime.LuaContext context =
                com.zhhz.truffle.lua.runtime.LuaContext.get(null);
        if (context == null) {
            return null;
        }
        com.oracle.truffle.api.object.DynamicObjectLibrary lib =
                com.oracle.truffle.api.object.DynamicObjectLibrary.getUncached();
        com.zhhz.truffle.lua.runtime.LuaTable globals = context.getGlobals();
        Object pkg = globals.rawget(T_GLOBAL_PACKAGE, lib);
        if (!(pkg instanceof com.zhhz.truffle.lua.runtime.LuaTable packageTable)) {
            return null;
        }
        Object loadedObj = packageTable.rawget(T_LOADED, lib);
        if (!(loadedObj instanceof com.zhhz.truffle.lua.runtime.LuaTable loaded)) {
            return null;
        }
        // 第 1 层：loaded[modName] == function
        // 第 2 层：loaded[modName][key] == function
        Object modName = LuaNil.SINGLETON;
        while (true) {
            modName = loaded.nextKey(modName, lib);
            if (modName == null || modName == LuaNil.SINGLETON) {
                break;
            }
            Object module = loaded.rawget(modName, lib);
            if (module == function) {
                return stripG(toJava(modName));
            }
            if (module instanceof com.zhhz.truffle.lua.runtime.LuaTable moduleTable) {
                Object key = LuaNil.SINGLETON;
                while (true) {
                    key = moduleTable.nextKey(key, lib);
                    if (key == null || key == LuaNil.SINGLETON) {
                        break;
                    }
                    if (moduleTable.rawget(key, lib) == function) {
                        return stripG(toJava(modName) + "." + toJava(key));
                    }
                }
            }
        }
        return null;
    }

    private static final TruffleString T_GLOBAL_PACKAGE = toTruffleString("package");
    private static final TruffleString T_LOADED = toTruffleString("loaded");

    private static String toJava(Object key) {
        if (key instanceof TruffleString ts) {
            return ts.toJavaStringUncached();
        }
        return String.valueOf(key);
    }

    private static String stripG(String name) {
        return name.startsWith("_G.") ? name.substring(3) : name;
    }

    /**
     * 当前正在执行的 Lua 函数对象（最近的、arguments[0] 为 LuaFunction 的帧）。
     * 用于在内建函数中实现 {@code pushglobalfuncname} 风格的错误信息。
     */
    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    public static Object currentFunction() {
        Object[] found = {null};
        try {
            com.oracle.truffle.api.Truffle.getRuntime().iterateFrames(frameInstance -> {
                Object[] args = frameInstance.getFrame(
                        com.oracle.truffle.api.frame.FrameInstance.FrameAccess.READ_ONLY)
                        .getArguments();
                if (args.length > 0 && args[0] instanceof com.zhhz.truffle.lua.runtime.LuaFunction) {
                    found[0] = args[0];
                    return Boolean.TRUE;
                }
                return null;
            });
        } catch (RuntimeException ignored) {
            return null;
        }
        return found[0];
    }

    /**
     * 复刻标准 Lua 的 {@code getfuncname/getobjname}：从调用点解析出 (name, namewhat)。
     * namewhat ∈ {"method","field","global","local","upvalue",""}。
     */
    public static String[] callSiteNameWhat(com.oracle.truffle.api.nodes.Node callSite) {
        com.oracle.truffle.api.nodes.Node node = callSite;
        while (node != null) {
            if (node instanceof com.zhhz.truffle.lua.nodes.expression.LuaMethodCallNode method) {
                com.zhhz.truffle.lua.nodes.LuaExpressionNode key = method.getDebugMethodKeyNode();
                if (key instanceof com.zhhz.truffle.lua.nodes.expression.LuaStringLiteralNode literal) {
                    TruffleString methodName = literal.getUnresolvedVariableName();
                    return new String[]{methodName != null ? methodName.toJavaStringUncached() : null, "method"};
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
     * 复刻标准 Lua 的 {@code luaL_argerror}：生成 "bad argument #N to 'name' (msg)"。
     * <ul>
     *   <li>方法调用（{@code obj:name()}）时把参数号减 1（不计 self），减到 0 则生成
     *       {@code calling 'name' on bad self (msg)}；</li>
     *   <li>调用点能解析出名字时用它（field/global/local/upvalue/method）；</li>
     *   <li>否则退回到 {@code pushglobalfuncname} 式的限定名，再退回 {@code function}。</li>
     * </ul>
     */
    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    public static RuntimeException argumentError(String function, int arg, String message) {
        return com.zhhz.truffle.lua.LuaException.create(argumentErrorMessage(function, arg, message), null);
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    public static String argumentErrorMessage(String function, int arg, String message) {
        String[] nameWhat = callSiteNameWhat(activeCallSite());
        String name = nameWhat[0];
        if ("method".equals(nameWhat[1])) {
            if (name == null) {
                name = function;
            }
            int adjusted = arg - 1;
            if (adjusted == 0) {
                return "calling '" + name + "' on bad self (" + message + ")";
            }
            return "bad argument #" + adjusted + " to '" + name + "' (" + message + ")";
        }
        if (name == null) {
            String found = globalFuncName(currentFunction());
            name = found != null ? found : function;
        }
        return "bad argument #" + arg + " to '" + name + "' (" + message + ")";
    }

    /** 当前正在执行的内建函数所对应的调用点（由 LuaInvokeNode/LuaMethodCallNode 记录）。 */
    public static com.oracle.truffle.api.nodes.Node activeCallSite() {
        com.zhhz.truffle.lua.runtime.LuaContext context =
                com.zhhz.truffle.lua.runtime.LuaContext.get(null);
        return context != null ? context.getActiveCallSite() : null;
    }


    // 你还可以把其他类似的工具方法放在这里，例如：
    // public static String getTypeName(Object value) { ... }
    // public static long coerceToLong(Object value) { ... }
}