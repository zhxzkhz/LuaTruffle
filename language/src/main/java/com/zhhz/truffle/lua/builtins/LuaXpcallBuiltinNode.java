package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

@NodeInfo(shortName = "xpcall")
public abstract class LuaXpcallBuiltinNode extends LuaBuiltinNode {

    @Specialization(limit = "3")
    @CompilerDirectives.TruffleBoundary
    public Object doXpcall(Object[] arguments,
                           @CachedLibrary(limit = "3") InteropLibrary interop) {

        // 1. 检查参数
        if (arguments.length < 2) {
            throw LuaException.create("bad argument #2 to 'xpcall' (function expected, got no value)", this);
        }

        Object function = arguments[0];
        Object messageHandler = arguments[1];

        // 2. 准备参数 (去掉前两个: func, msgh)
        Object[] callArgs = new Object[arguments.length - 2];
        System.arraycopy(arguments, 2, callArgs, 0, callArgs.length);

        try {

            // 3. 【尝试执行】
            Object result = interop.execute(function, callArgs);

            // 4. 【成功路径】

            Object[] newObjects;
            if (result instanceof LuaMultiValue luaMultiValue) {
                if (luaMultiValue.isMultiValue()){
                    var v = (Object[])luaMultiValue.values();
                    newObjects = new Object[v.length + 1];
                    System.arraycopy(v, 0, newObjects, 1, v.length);
                } else {
                    //目前不可能走到这步
                    throw new RuntimeException();
                }
            } else if (result == null || result == LuaNil.SINGLETON){
                return true;
            } else {
                newObjects = new Object[2];
                newObjects[1] = result;
            }
            newObjects[0] = true;
            return new LuaMultiValue(newObjects);

        } catch (LuaException e) {
            // 5. 【错误处理】
            // 捕获到错误后，调用 messageHandler
            try {
                Object errorObject = e.getMessage();
                // 调用 msgh(errorObject)
                Object handlerResult = interop.execute(messageHandler, errorObject);
                // 返回 [false, handlerResult]
                return new LuaMultiValue(new Object[] { false, handlerResult });

            } catch (Exception handlerError) {
                // 如果错误处理器本身也报错了，Lua 的行为是抛出 "error in error handling"
                throw LuaException.create("error in error handling", this);
            }
        } catch (Exception e) {

            // 处理其他 Interop 异常，同样尝试调用 handler
            try {
                Object handlerResult = interop.execute(messageHandler, e.getMessage());
                return new LuaMultiValue(new Object[] { false, handlerResult });
            } catch (Exception handlerError) {
                throw LuaException.create("error in error handling", this);
            }
        }
    }

}
