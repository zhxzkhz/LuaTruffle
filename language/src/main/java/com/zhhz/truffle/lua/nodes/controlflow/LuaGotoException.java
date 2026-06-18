package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.strings.TruffleString;

import java.util.Arrays;

public final class LuaGotoException extends ControlFlowException {

    private final TruffleString label;
    private final int[] targetBlockPath;

    public LuaGotoException(TruffleString label, int[] targetBlockPath) {
        this.label = label;
        this.targetBlockPath = targetBlockPath;
    }

    public TruffleString getLabel() {
        return label;
    }

    public boolean targetsBlock(int[] blockPath) {
        if (targetBlockPath == null) {
            return true;
        }
        return Arrays.equals(targetBlockPath, blockPath);
    }
}
