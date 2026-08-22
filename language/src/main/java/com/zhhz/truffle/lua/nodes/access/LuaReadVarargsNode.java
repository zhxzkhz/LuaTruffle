package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

@NodeInfo(shortName = "...", description = "Reads variable arguments")
public final class LuaReadVarargsNode extends LuaExpressionNode {

    private final int fixedParamCount;

    public LuaReadVarargsNode(int fixedParamCount) {
        this.fixedParamCount = fixedParamCount;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object[] args = getFrameArguments(frame);
        int startIndex = 1 + fixedParamCount;
        return startIndex >= args.length ? LuaNil.SINGLETON : args[startIndex];
    }

    @Override
    public Object executeMultiValue(VirtualFrame frame) {
        Object[] args = getFrameArguments(frame);
        int startIndex = 1 + fixedParamCount;
        int varargsLength = args.length - startIndex;
        if (varargsLength <= 0) {
            return new LuaMultiValue(new Object[0]);
        }
        Object[] varargs = new Object[varargsLength];
        System.arraycopy(args, startIndex, varargs, 0, varargsLength);
        return new LuaMultiValue(varargs);
    }

    /**
     * Get frame arguments, falling back to the 'arg' table when the frame has
     * no varargs (e.g., for the main chunk). This allows -e statements that
     * modify arg[1] to be visible via ... in the main chunk.
     */
    private Object[] getFrameArguments(VirtualFrame frame) {
        Object[] args = frame.getArguments();
        int startIndex = 1 + fixedParamCount;
        if (startIndex >= args.length) {
            return getVarargsFromArgTable(args);
        }
        return args;
    }

    @TruffleBoundary
    private Object[] getVarargsFromArgTable(Object[] args) {
        LuaContext context = LuaContext.get(null);
        LuaTable globals = context.getGlobals();
        Object argObj = globals.rawget(context.toTruffleString("arg"));
        if (argObj == null || argObj == LuaNil.SINGLETON) {
            return args; // no arg table, no varargs
        }
        if (!(argObj instanceof LuaTable)) {
            throw com.zhhz.truffle.lua.LuaException.create("'arg' is not a table", null);
        }
        LuaTable argTable = (LuaTable) argObj;
        java.util.List<Object> varargList = new java.util.ArrayList<>();
        long idx = 1;
        while (true) {
            Object val = argTable.rawget(idx);
            if (val == null || val == LuaNil.SINGLETON) break;
            varargList.add(val);
            idx++;
        }
        if (!varargList.isEmpty()) {
            Object[] combined = new Object[1 + varargList.size()];
            combined[0] = args.length > 0 ? args[0] : null;
            for (int i = 0; i < varargList.size(); i++) {
                combined[1 + i] = varargList.get(i);
            }
            return combined;
        }
        return args;
    }
}
