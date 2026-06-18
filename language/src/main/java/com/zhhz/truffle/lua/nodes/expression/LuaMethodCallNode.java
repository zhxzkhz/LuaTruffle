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
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaValue;

@NodeInfo(shortName = "method_call")
public final class LuaMethodCallNode extends LuaExpressionNode {

    @Child private LuaExpressionNode receiverNode;
    @Child private LuaExpressionNode methodKeyNode;
    @Children private final LuaExpressionNode[] argumentNodes;
    @Child private InteropLibrary library;

    public LuaMethodCallNode(LuaExpressionNode receiverNode, LuaExpressionNode methodKeyNode, LuaExpressionNode[] argumentNodes) {
        this.receiverNode = receiverNode;
        this.methodKeyNode = methodKeyNode;
        this.argumentNodes = argumentNodes;
        this.library = InteropLibrary.getFactory().createDispatched(3);
    }

    private Object unpackResult(Object result) {
        if (result instanceof Object[] multi) {
            return multi.length > 0 ? multi[0] : null;
        }
        return result;
    }

    @Override
    @ExplodeLoop
    public Object executeGeneric(VirtualFrame frame) {
        Object receiverValue = receiverNode.executeGeneric(frame);

        Object methodKey = methodKeyNode.executeGeneric(frame);
        Object functionValue = unpackResult(LuaReadTableNode.readUncached(this, receiverValue, methodKey));

        if (!library.isExecutable(functionValue)) {
            String message = getMessage(functionValue);
            throw LuaException.create(message, this);
        }

        int explicitArgCount = this.argumentNodes.length;
        int totalArgCount = explicitArgCount + 1;

        if (explicitArgCount == 0) {
            try {
                return execReturn(functionValue, new Object[]{receiverValue});
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                throw LuaException.undefinedFunction(this, e);
            }
        }

        final Object[] prefixArgs = new Object[explicitArgCount - 1 + 1];
        prefixArgs[0] = receiverValue;
        for (int i = 0; i < explicitArgCount - 1; i++) {
            Object rawArg = this.argumentNodes[i].executeGeneric(frame);
            if (rawArg instanceof LuaMultiValue luaMultiValue) {
                if (luaMultiValue.isMultiValue()) {
                    prefixArgs[i + 1] = ((Object[]) luaMultiValue.values())[0];
                } else {
                    prefixArgs[i + 1] = luaMultiValue.values();
                }
            } else if (rawArg instanceof Object[] || rawArg == null) {
                throw LuaException.create("unexpected multi-return or nil in method call argument", this);
            } else {
                prefixArgs[i + 1] = rawArg;
            }
        }

        Object lastResult = this.argumentNodes[explicitArgCount - 1].executeGeneric(frame);
        Object[] finalArguments;

        if (lastResult instanceof LuaMultiValue luaMultiValue) {
            if (luaMultiValue.isMultiValue()) {
                Object[] values = (Object[]) luaMultiValue.values();
                int totalLength = prefixArgs.length + values.length;
                finalArguments = new Object[totalLength];
                System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
                System.arraycopy(values, 0, finalArguments, prefixArgs.length, values.length);
            } else {
                finalArguments = new Object[prefixArgs.length + 1];
                System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
                finalArguments[prefixArgs.length] = luaMultiValue.values();
            }
        } else if (lastResult instanceof Object[] || lastResult == null) {
            throw LuaException.create("unexpected multi-return or nil in method call argument", this);
        } else {
            finalArguments = new Object[prefixArgs.length + 1];
            System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
            finalArguments[prefixArgs.length] = lastResult;
        }

        try {
            return execReturn(functionValue, finalArguments);
        } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
            throw LuaException.undefinedFunction(this, e);
        }
    }

    private Object execReturn(Object functionValue, Object[] finalArguments) throws UnsupportedMessageException, UnsupportedTypeException, ArityException {
        Object result = library.execute(functionValue, finalArguments);
        if (result instanceof LuaMultiValue luaMultiValue) {
            if (luaMultiValue.isMultiValue()) {
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
        return String.format("attempt to call a %s value", typeName);
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.CallTag.class) {
            return true;
        }
        return super.hasTag(tag);
    }
}
