package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.access.LuaReadTableNode;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaValue;


@NodeInfo(shortName = "invoke")
public final class LuaInvokeNode extends LuaExpressionNode {

    @Child private LuaExpressionNode functionNode;
    @Children private final LuaExpressionNode[] argumentNodes;
    @Child private InteropLibrary library;

    public LuaInvokeNode(LuaExpressionNode functionNode, LuaExpressionNode[] argumentNodes) {
        this.functionNode = functionNode;
        if (functionNode == null){
            throw new RuntimeException("functionNode 为空");
        }
        this.argumentNodes = argumentNodes;
        this.library = InteropLibrary.getFactory().createDispatched(3);
    }

    // unpack 辅助方法
    private Object unpackResult(Object result) {
        if (result instanceof Object[] multi) { // 【修改】
            return multi.length > 0 ? multi[0] : null;
        }
        return result;
    }

    @Override
    @ExplodeLoop
    public Object executeGeneric(VirtualFrame frame) {

        // --- 1. 获取并解包要被调用的函数值 ---
        Object rawFunctionValue = functionNode.executeGeneric(frame);

        Object functionValue = unpackResult(rawFunctionValue);

        // --- 2. 检查可调用性 ---
        if (!(functionValue instanceof LuaFunction luaFunction)) { // 简化了 if 语句
            String message = getMessage(functionValue);
            throw LuaException.create(message, this);
        }

        // 2. 【参数处理优化】
        int argCount = this.argumentNodes.length;

        // 优化：无参调用直接处理
        if (argCount == 0) {
            try {
                return execReturn(luaFunction,null);
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                throw LuaException.undefinedFunction(this, e);
            }
        }

        // 步骤 A: 计算前 N-1 个参数 (这些参数长度固定为 1)
        // 我们先用一个临时数组存起来。JIT 编译器通常能将这个小数组虚拟化(scalarize)，
        // 甚至直接在寄存器中传递，不会产生真实的堆分配。
        final Object[] prefixArgs = new Object[argCount - 1];
        for (int i = 0; i < argCount - 1; i++) {
            // 在这里，我们仍然需要处理参数本身是多返回值的情况
            Object rawArg = this.argumentNodes[i].executeGeneric(frame);
            if (rawArg instanceof LuaMultiValue luaMultiValue) {
                //如果是多值则获取第一个
                if (luaMultiValue.isMultiValue()){
                    prefixArgs[i] = ((Object[]) luaMultiValue.values())[0];
                } else {
                    prefixArgs[i] = luaMultiValue.values();
                }
            } else  if (rawArg instanceof Object[] || rawArg == null) {
                throw new RuntimeException("不能返回数组祸空");
            } else {
                prefixArgs[i] = rawArg;
            }
        }
        // 步骤 B: 计算最后一个参数 (可能返回多值)
        Object lastResult = this.argumentNodes[argCount - 1].executeGeneric(frame);

        // 步骤 C: 根据最后一个参数的结果，创建最终数组
        Object[] finalArguments;

        if (lastResult instanceof LuaMultiValue luaMultiValue) {
            if (luaMultiValue.isMultiValue()){
                Object[] values = (Object[]) luaMultiValue.values();
                // --- 情况 1: 最后一个参数返回了多值 ---
                // 计算总长度
                int totalLength = (argCount - 1) + values.length;
                finalArguments = new Object[totalLength];

                // 只有这里发生了真正的数组复制
                System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
                System.arraycopy(values, 0, finalArguments, prefixArgs.length, values.length);

            } else {
                // --- 情况 2: 最后一个参数返回单值 (最常见的情况) ---
                finalArguments = new Object[argCount];
                System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
                finalArguments[argCount - 1] = luaMultiValue.values();
            }
        } else  if (lastResult instanceof Object[] || lastResult == null) {
            throw new RuntimeException("不能返回数组祸空");
        }  else {
            finalArguments = new Object[argCount];
            System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
            finalArguments[argCount - 1] = lastResult;
        }

        // --- 4. 【关键修正】直接将【纯净的用户参数数组】传递给 execute 消息 ---
        //    不再手动创建 frameArguments 或添加 parentFrame。
        //    这个职责现在完全交给了 LuaFunction.Execute 消息处理器。

        try {
            return execReturn(luaFunction, finalArguments);
        } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
            // Execute was not successful.
            throw LuaException.undefinedFunction(this, e);
        }
    }

    private Object execReturn(LuaFunction functionValue,Object[] finalArguments) throws UnsupportedMessageException, UnsupportedTypeException, ArityException {
        Object result;
        if (finalArguments == null){
            result = library.execute(functionValue);
        } else {
            result = library.execute(functionValue, finalArguments);
        }

        if (result instanceof LuaMultiValue luaMultiValue){
            if (luaMultiValue.isMultiValue()){
                return luaMultiValue;
            } else {
                return luaMultiValue.values();
            }
        }
        return result;
    }

    @TruffleBoundary
    private String getMessage(Object functionValue) {
        String typeName = (functionValue instanceof LuaValue)
                ? ((LuaValue) functionValue).getTypeName()
                : "java object";

        String message = String.format("attempt to call a %s value", typeName);

        // b. 【关键】向子节点“询问”它试图解析的名字
        TruffleString varName = this.functionNode.getUnresolvedVariableName();
        if (varName != null) {
            // c. 如果子节点提供了名字，将其附加到错误信息中
            //    还需要判断这个名字是全局还是局部
            if (this.functionNode instanceof LuaReadTableNode){
                message += String.format(" (field '%s')", varName.toJavaStringUncached());
            } else {
                message += String.format(" (global '%s')", varName.toJavaStringUncached());
            }
        }
        return message;
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.CallTag.class) {
            return true;
        }
        return super.hasTag(tag);
    }


}
