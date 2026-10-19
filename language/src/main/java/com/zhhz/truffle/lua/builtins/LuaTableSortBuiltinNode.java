package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.isTruthy;
import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;
@GenerateInline(false)

@NodeInfo(shortName = "table.sort")
public abstract class LuaTableSortBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @CompilerDirectives.TruffleBoundary
    protected LuaMultiValue doSort(Object[] arguments,
                                   @CachedLibrary(limit = "3") InteropLibrary interop,
                                   @com.oracle.truffle.api.dsl.Cached(value = "create()", neverDefault = true) LuaMetatableNode metatableNode) {
        LuaTable table = TableLibrary.ensureTable(arguments, 0, "sort");
        Object comparator = arguments.length >= 2 ? arguments[1] : LuaNil.SINGLETON;
        if (comparator != LuaNil.SINGLETON && !interop.isExecutable(comparator)) {
            throw LuaException.create("bad argument #2 to 'sort' (function expected, got value)", this);
        }

        boolean rawGet = !TableLibrary.hasMeta(table, LuaMetatables.__INDEX);
        boolean rawSet = !TableLibrary.hasMeta(table, LuaMetatables.__NEWINDEX);
        int length = (int) TableLibrary.lengthOf(table);
        // 使用 1 起始下标，与标准 Lua 的 auxsort 完全一致
        Object[] values = new Object[length + 1];
        for (int index = 1; index <= length; index++) {
            values[index] = TableLibrary.tableGet(table, index, rawGet);
        }

        auxsort(values, 1, length, comparator, interop, metatableNode);
        for (int index = 1; index <= length; index++) {
            TableLibrary.tableSet(table, index, values[index], rawSet);
        }
        return new LuaMultiValue(new Object[0]);
    }

    /**
     * 复刻标准 Lua {@code ltablib.c} 的 {@code auxsort}。该实现对不一致的
     * 比较器会在分区循环越界时抛出 "invalid order function for sorting"。
     */
    private void auxsort(Object[] a, int l, int u, Object comparator, InteropLibrary interop,
                         LuaMetatableNode metatableNode) {
        while (l < u) { // 尾递归优化：较小的区间递归，较大的区间循环处理
            // 排列 a[l]、a[(l+u)/2]、a[u]
            if (lessThan(a[u], a[l], comparator, interop, metatableNode)) {
                swap(a, l, u);
            }
            if (u - l == 1) {
                break; // 只有两个元素
            }
            int i = (l + u) / 2;
            if (lessThan(a[i], a[l], comparator, interop, metatableNode)) {
                swap(a, i, l);
            } else if (lessThan(a[u], a[i], comparator, interop, metatableNode)) {
                swap(a, i, u);
            }
            if (u - l == 2) {
                break; // 只有三个元素
            }
            Object pivot = a[i]; // 枢轴
            swap(a, i, u - 1);
            // 不变式：a[l..i] <= P <= a[j..u]
            i = l;
            int j = u - 1;
            for (;;) {
                // 递增 i 直到 a[i] >= P
                while (true) {
                    i++;
                    if (!lessThan(a[i], pivot, comparator, interop, metatableNode)) {
                        break;
                    }
                    if (i >= u) {
                        throw LuaException.create("invalid order function for sorting", this);
                    }
                }
                // 递减 j 直到 a[j] <= P
                while (true) {
                    j--;
                    if (!lessThan(pivot, a[j], comparator, interop, metatableNode)) {
                        break;
                    }
                    if (j <= l) {
                        throw LuaException.create("invalid order function for sorting", this);
                    }
                }
                if (j < i) {
                    break;
                }
                swap(a, i, j);
            }
            swap(a, u - 1, i); // 把枢轴放回 a[i]
            // 调整使较小的部分位于 [j..i]，较大的部分位于 [l..u]
            if (i - l < u - i) {
                j = l;
                i = i - 1;
                l = i + 2;
            } else {
                j = i + 1;
                i = u;
                u = j - 2;
            }
            auxsort(a, j, i, comparator, interop, metatableNode); // 递归较小的区间
        }
    }

    private static void swap(Object[] a, int i, int j) {
        Object tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private boolean lessThan(Object left, Object right, Object comparator, InteropLibrary interop,
                             LuaMetatableNode metatableNode) {
        Object result;
        if (comparator != LuaNil.SINGLETON) {
            // 标准 Lua：C 函数经 lua_call/luaD_callnoyield 调用 Lua 比较器，故比较器内不可 yield。
            com.zhhz.truffle.lua.runtime.LuaContext context = com.zhhz.truffle.lua.runtime.LuaContext.get(this);
            context.enterNonYieldable();
            try {
                result = interop.execute(comparator, left, right);
            } catch (LuaException exception) {
                throw exception;
            } catch (Exception exception) {
                throw LuaException.create("error in table.sort comparator\n" + exception.getMessage(), this);
            } finally {
                context.exitNonYieldable();
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
        throw LuaException.create(com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.orderErrorMessage(left, right), this);
    }
}
