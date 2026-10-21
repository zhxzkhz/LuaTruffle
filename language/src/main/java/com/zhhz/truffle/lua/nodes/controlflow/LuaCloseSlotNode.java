package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

public final class LuaCloseSlotNode extends LuaStatementNode {

    private final int slotIndex;
    private final TruffleString variableName;
    @Child private InteropLibrary interop;

    public LuaCloseSlotNode(int slotIndex, TruffleString variableName) {
        this.slotIndex = slotIndex;
        this.variableName = variableName;
        this.interop = InteropLibrary.getFactory().createDispatched(3);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        executeClose(frame, LuaNil.SINGLETON);
    }

    public void executeClose(VirtualFrame frame, Object errorObject) {
        Object value = readSlot(frame);
        if (value == LuaNil.SINGLETON) {
            return;
        }

        Object closeFunction = findCloseMetamethod(value);
        if (!(closeFunction instanceof LuaFunction)) {
            throw nonClosableError();
        }

        try {
            interop.execute(closeFunction, value, errorObject == null ? LuaNil.SINGLETON : errorObject);
            frame.getFrameDescriptor().setSlotKind(slotIndex, FrameSlotKind.Object);
            frame.setObject(slotIndex, LuaNil.SINGLETON);
        } catch (LuaException e) {
            throw e;
        } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
            throw closeError(e);
        }
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private RuntimeException nonClosableError() {
        return LuaException.create("variable '" + variableName.toJavaStringUncached() + "' got a non-closable value", this);
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private RuntimeException closeError(Exception cause) {
        return LuaException.create("error closing variable '" + variableName.toJavaStringUncached() + "': "
                + LuaException.messageOf(cause), this);
    }

    private Object readSlot(VirtualFrame frame) {
        FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(slotIndex);
        return switch (kind) {
            case Long -> frame.getLong(slotIndex);
            case Double -> frame.getDouble(slotIndex);
            case Boolean -> frame.getBoolean(slotIndex);
            case Object -> {
                Object value = frame.getObject(slotIndex);
                yield value == null ? LuaNil.SINGLETON : value;
            }
            default -> LuaNil.SINGLETON;
        };
    }

    @TruffleBoundary
    private Object findCloseMetamethod(Object value) {
        LuaTable metatable = LuaContext.get(this).getEffectiveMetatable(value);
        if (metatable == null) {
            return null;
        }
        return DynamicObjectLibrary.getUncached().getOrDefault(metatable, LuaMetatables.__CLOSE, null);
    }
}
