package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;

@NodeInfo(shortName = "pcall")
public abstract class LuaPcallBuiltinNode extends LuaBuiltinNode {

    @Specialization(limit = "3")
    @CompilerDirectives.TruffleBoundary
    public Object doPcall(Object[] arguments,
                          @CachedLibrary(limit = "3") InteropLibrary interop) {
        // 1. 检查参数
        if (arguments.length < 1) {
            throw LuaException.create("bad argument #1 to 'pcall' (value expected)", this);
        }

        Object function = arguments[0];


        // 2. 准备参数 (去掉第一个 func 参数)
        Object[] callArgs = new Object[arguments.length - 1];
        System.arraycopy(arguments, 1, callArgs, 0, callArgs.length);

        try {

            // 3. 【核心】尝试执行函数
            Object result = interop.execute(function, callArgs);

            // 4. 【成功路径】返回 [true, result...]
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
            } else if (result == null){
                return true;
            } else {
                newObjects = new Object[2];
                newObjects[1] = result;
            }
            newObjects[0] = true;
            return new LuaMultiValue(newObjects);

        } catch (Exception e) {
            // 处理其他可能的 Interop 异常 (如 ArityException, UnsupportedTypeException)
            // 也可以视为运行时错误

            var ovj = new Object[] { false, e.getMessage() };

            return new LuaMultiValue(ovj);
        }
    }


}