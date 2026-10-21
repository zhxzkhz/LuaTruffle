package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
@GenerateInline(false)

@NodeInfo(shortName = "pcall")
public abstract class LuaPcallBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    protected Object pcall(Object[] arguments,
                           @CachedLibrary(limit = "3") InteropLibrary interop) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'pcall' (value expected)", this);
        }
        Object[] callArguments = new Object[arguments.length - 1];
        System.arraycopy(arguments, 1, callArguments, 0, callArguments.length);
        LuaContext context = LuaContext.get(this);
        // 标准 Lua 的 luaD_pcall 在受保护调用期间把 errfunc 置 0，
        // 因此 pcall 内部的错误不会触发外层 xpcall 的消息处理函数。
        Object previousHandler = context != null ? context.setErrorHandler(null) : null;
        try {
            Object result = success(interop.execute(arguments[0], callArguments));
            if (context != null) {
                context.clearStackOverflow();
            }
            return result;
        } catch (com.zhhz.truffle.lua.runtime.LuaHandledError handled) {
            // 已由外层的 xpcall 消息处理函数处理，交回给该 xpcall。
            throw handled;
        } catch (Exception exception) {
            if (context != null) {
                context.clearStackOverflow();
            }
            return new LuaMultiValue(new Object[]{false, errorObject(exception)});
        } finally {
            if (context != null) {
                context.restoreErrorHandler(previousHandler);
            }
        }
    }

    static Object success(Object result) {
        if (result instanceof LuaMultiValue values) {
            Object[] returned = values.values();
            Object[] protectedValues = new Object[returned.length + 1];
            protectedValues[0] = true;
            System.arraycopy(returned, 0, protectedValues, 1, returned.length);
            return new LuaMultiValue(protectedValues);
        }
        return new LuaMultiValue(new Object[]{true, result == null ? LuaNil.SINGLETON : result});
    }

    static Object errorObject(Exception exception) {
        Object error;
        if (exception instanceof LuaException luaException) {
            error = luaException.getErrorObject();
        } else {
            String message = LuaException.messageOf(exception);
            error = message == null || message.isBlank() ? LuaException.simpleNameOf(exception) : message;
        }
        // 标准 Lua 的运行时错误对象是字符串；把 Java String 统一转成 Lua 字符串，
        // 否则 pcall 捕获后对错误消息做索引/方法调用会失败。
        return error instanceof String s ? LuaContext.toTruffleString(s) : error;
    }
}
