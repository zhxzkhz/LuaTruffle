package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.isTruthy;
import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

@NodeInfo(shortName = "table.sort")
public abstract class LuaTableSortBuiltinNode extends LuaBuiltinNode {

    @Specialization(limit = "3")
    @CompilerDirectives.TruffleBoundary
    protected LuaMultiValue doSort(Object[] arguments,
                                   @CachedLibrary(limit = "3") InteropLibrary interop,
                                   @com.oracle.truffle.api.dsl.Cached("create()") LuaMetatableNode metatableNode) {
        LuaTable table = TableLibrary.ensureTable(arguments, 0, "sort");
        if (arguments.length > 2) {
            throw LuaException.create("wrong number of arguments to 'sort'", this);
        }
        Object comparator = arguments.length == 2 ? arguments[1] : LuaNil.SINGLETON;
        if (comparator != LuaNil.SINGLETON && !interop.isExecutable(comparator)) {
            throw LuaException.create("bad argument #2 to 'sort' (function expected, got value)", this);
        }

        int length = table.getArraySize();
        Object[] values = new Object[length];
        for (int index = 0; index < length; index++) {
            values[index] = table.arrayRawGet(index + 1);
        }

        heapSort(values, comparator, interop, metatableNode);
        for (int index = 0; index < values.length; index++) {
            table.arrayRawSet(index + 1, values[index]);
        }
        return new LuaMultiValue(new Object[0]);
    }

    private void heapSort(Object[] values, Object comparator, InteropLibrary interop,
                          LuaMetatableNode metatableNode) {
        for (int root = values.length / 2 - 1; root >= 0; root--) {
            siftDown(values, root, values.length, comparator, interop, metatableNode);
        }
        for (int end = values.length - 1; end > 0; end--) {
            Object first = values[0];
            values[0] = values[end];
            values[end] = first;
            siftDown(values, 0, end, comparator, interop, metatableNode);
        }
    }

    private void siftDown(Object[] values, int root, int end, Object comparator,
                          InteropLibrary interop, LuaMetatableNode metatableNode) {
        while (true) {
            int child = root * 2 + 1;
            if (child >= end) {
                return;
            }
            if (child + 1 < end && lessThan(values[child], values[child + 1], comparator, interop, metatableNode)) {
                child++;
            }
            if (!lessThan(values[root], values[child], comparator, interop, metatableNode)) {
                return;
            }
            Object value = values[root];
            values[root] = values[child];
            values[child] = value;
            root = child;
        }
    }

    private boolean lessThan(Object left, Object right, Object comparator, InteropLibrary interop,
                             LuaMetatableNode metatableNode) {
        Object result;
        if (comparator != LuaNil.SINGLETON) {
            try {
                result = interop.execute(comparator, left, right);
            } catch (LuaException exception) {
                throw exception;
            } catch (Exception exception) {
                throw LuaException.create("error in table.sort comparator\n" + exception.getMessage(), this);
            }
        } else {
            result = defaultLessThan(left, right, metatableNode);
        }
        return isTruthy(LuaMultiValue.firstValue(result));
    }

    private Object defaultLessThan(Object left, Object right, LuaMetatableNode metatableNode) {
        if (left instanceof Long leftValue && right instanceof Long rightValue) {
            return leftValue < rightValue;
        }
        if (left instanceof Double leftValue && right instanceof Double rightValue) {
            return leftValue < rightValue;
        }
        if (left instanceof Long leftValue && right instanceof Double rightValue) {
            return leftValue < rightValue;
        }
        if (left instanceof Double leftValue && right instanceof Long rightValue) {
            return leftValue < rightValue;
        }
        if (left instanceof LuaNumber leftValue && right instanceof LuaNumber rightValue) {
            return leftValue.getValue() < rightValue.getValue();
        }
        if (left instanceof TruffleString leftValue && right instanceof TruffleString rightValue) {
            return TruffleString.CompareCharsUTF16Node.getUncached().execute(leftValue, rightValue) < 0;
        }
        if (left instanceof LuaString leftValue && right instanceof LuaString rightValue) {
            return TruffleString.CompareCharsUTF16Node.getUncached().execute(leftValue.getValue(), rightValue.getValue()) < 0;
        }

        Object result = metatableNode.execute(left, right, toTruffleString("__lt"));
        if (result != null) {
            return LuaMultiValue.firstValue(result);
        }
        throw LuaException.typeError(this, "<", left, right);
    }
}
