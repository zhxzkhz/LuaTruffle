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
@GenerateInline(false)

@NodeInfo(shortName = "xpcall")
public abstract class LuaXpcallBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    protected Object xpcall(Object[] arguments,
                            @CachedLibrary(limit = "3") InteropLibrary interop) {
        if (arguments.length < 2) {
            throw LuaException.create("bad argument #2 to 'xpcall' (function expected, got no value)", this);
        }
        Object[] callArguments = new Object[arguments.length - 2];
        System.arraycopy(arguments, 2, callArguments, 0, callArguments.length);
        LuaContext context = LuaContext.get(this);
        Object previousHandler = context != null ? context.setErrorHandler(arguments[1]) : null;
        try {
            Object result = LuaPcallBuiltinNode.success(interop.execute(arguments[0], callArguments));
            if (context != null) {
                context.clearStackOverflow();
            }
            return result;
        } catch (com.zhhz.truffle.lua.runtime.LuaHandledError handled) {
            // 栈溢出等错误已在抛出点调用过 handler，直接返回其结果。
            return new LuaMultiValue(new Object[]{false, LuaMultiValue.firstValue(handled.getHandled())});
        } catch (Exception exception) {
            try {
                // 参考实现经 luaD_callnoyield 调用消息处理函数：nny>0，不可 yield。
                if (context != null) {
                    context.enterNonYieldable();
                }
                Object handled;
                try {
                    handled = interop.execute(arguments[1], LuaPcallBuiltinNode.errorObject(exception));
                } finally {
                    if (context != null) {
                        context.exitNonYieldable();
                    }
                }
                if (context != null) {
                    context.clearStackOverflow();
                }
                return new LuaMultiValue(new Object[]{false, LuaMultiValue.firstValue(handled)});
            } catch (Exception handlerError) {
                if (context != null) {
                    context.clearStackOverflow();
                }
                // 标准 Lua：消息处理函数自身出错时产生 LUA_ERRERR，
                // xpcall 返回 false 与错误消息，而不是向外抛出。
                return new LuaMultiValue(new Object[]{false, LuaContext.toTruffleString("error in error handling")});
            }
        } finally {
            if (context != null) {
                context.restoreErrorHandler(previousHandler);
            }
        }
    }
}
