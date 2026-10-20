package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TableLibraryTest extends AbstractLuaTest {

    private Context context;

    @BeforeEach
    public void setUp() {
        context = newContextBuilder("lua").build();
    }

    @AfterEach
    public void tearDown() {
        context.close();
    }

    private Value eval(String code) {
        return context.eval("lua", code);
    }

    @Test
    public void testLibraryRegistration() {
        assertTrue(eval("""
                return type(table) == 'table'
                    and type(table.concat) == 'function'
                    and type(table.insert) == 'function'
                    and type(table.move) == 'function'
                    and type(table.pack) == 'function'
                    and type(table.remove) == 'function'
                    and type(table.sort) == 'function'
                    and type(table.unpack) == 'function'
                """).asBoolean());
    }

    @Test
    public void testConcatDefaultsRangesAndNumbers() {
        assertEquals("1,two,3.5", eval("return table.concat({1, 'two', 3.5}, ',')").asString());
        assertEquals("b-c", eval("return table.concat({'a', 'b', 'c', 'd'}, '-', 2, 3)").asString());
        assertEquals("", eval("return table.concat({'a'}, nil, 2, 1)").asString());

        assertFalse(eval("""
                local ok = pcall(function() table.concat({1, nil, 3}, ',') end)
                return ok
                """).asBoolean());
    }

    @Test
    public void testInsertAndRemove() {
        assertTrue(eval("""
                local values = {1, 3}
                local insertReturns = select('#', table.insert(values, 2, 2))
                table.insert(values, 4)
                local removed = table.remove(values, 2)
                local last = table.remove(values)
                return insertReturns == 0
                    and removed == 2
                    and last == 4
                    and #values == 2
                    and values[1] == 1
                    and values[2] == 3
                """).asBoolean());

        assertTrue(eval("""
                local empty = {[0] = 'zero'}
                local atZero = table.remove(empty)
                local atOne = table.remove({}, 1)
                local afterEnd = table.remove({1, 2}, 3)
                return atZero == 'zero' and empty[0] == nil and atOne == nil and afterEnd == nil
                """).asBoolean());
        assertFalse(eval("local ok = pcall(function() table.insert({}, 3, 1) end); return ok").asBoolean());
        assertFalse(eval("local ok = pcall(function() table.remove({1}, 3) end); return ok").asBoolean());
    }

    @Test
    public void testPackAndUnpackPreserveArity() {
        assertTrue(eval("""
                local packed = table.pack(1, nil, 3)
                local a, b, c = table.unpack(packed, 1, packed.n)
                return packed.n == 3
                    and packed[1] == 1
                    and packed[2] == nil
                    and packed[3] == 3
                    and select('#', table.unpack(packed, 1, packed.n)) == 3
                    and a == 1 and b == nil and c == 3
                    and select('#', table.unpack({}, 1, 0)) == 0
                """).asBoolean());
    }

    @Test
    public void testMoveHandlesOverlapDestinationAndNegativeTargetIndices() {
        assertTrue(eval("""
                local right = {1, 2, 3, 4, 5}
                local returned = table.move(right, 1, 3, 2)

                local left = {1, 2, 3, 4, 5}
                table.move(left, 2, 4, 1)

                local source = {'a', 'b', 'c'}
                local destination = {}
                local destinationReturned = table.move(source, 1, 3, 5, destination)

                local negativeDestination = {}
                table.move({'x', 'y'}, 1, 2, -1, negativeDestination)

                -- 5.3.0 要求起始位置为正
                local negativeFromOk = pcall(table.move, {}, -1, 1, 1)

                return returned == right
                    and right[1] == 1 and right[2] == 1 and right[3] == 2 and right[4] == 3 and right[5] == 5
                    and left[1] == 2 and left[2] == 3 and left[3] == 4 and left[4] == 4 and left[5] == 5
                    and destinationReturned == destination
                    and destination[5] == 'a' and destination[6] == 'b' and destination[7] == 'c'
                    and negativeDestination[-1] == 'x' and negativeDestination[0] == 'y'
                    and not negativeFromOk
                """).asBoolean());
    }

    @Test
    public void testIntegerKeysAreCanonicalAndSparseKeysDoNotGrowArray() {
        assertTrue(eval("""
                local values = {}
                rawset(values, 1, 'one')
                rawset(values, 1000000, 'sparse')

                local moved = {}
                table.move({'far'}, 1, 1, 1000000, moved)

                return values[1] == 'one'
                    and values[1.0] == 'one'
                    and rawget(values, 1.0) == 'one'
                    and values[1000000] == 'sparse'
                    and values[1000000.0] == 'sparse'
                    and rawget(values, 1000000.0) == 'sparse'
                    and #values == 1
                    and moved[1000000] == 'far'
                    and #moved == 0
                """).asBoolean());
    }

    @Test
    public void testSortDefaultAndCustomComparator() {
        assertTrue(eval("""
                local numbers = {5, 1, 4, 2, 3}
                local sortReturns = select('#', table.sort(numbers))

                local strings = {'c', 'a', 'b'}
                table.sort(strings)

                local descending = {1, 4, 2, 3}
                table.sort(descending, function(a, b) return a > b end)

                return sortReturns == 0
                    and table.concat(numbers, ',') == '1,2,3,4,5'
                    and table.concat(strings, ',') == 'a,b,c'
                    and table.concat(descending, ',') == '4,3,2,1'
                """).asBoolean());
    }

    @Test
    public void testSortUsesLessThanMetamethod() {
        assertTrue(eval("""
                local mt = { __lt = function(a, b) return a.value < b.value end }
                local values = {
                    setmetatable({value = 3}, mt),
                    setmetatable({value = 1}, mt),
                    setmetatable({value = 2}, mt)
                }
                table.sort(values)
                return values[1].value == 1 and values[2].value == 2 and values[3].value == 3
                """).asBoolean());
    }

    @Test
    public void testSortAndArgumentErrorsAreCatchable() {
        assertTrue(eval("""
                local invalidComparator = pcall(function() table.sort({1, 2}, 42) end)
                local incomparableValues = pcall(function() table.sort({{}, {}}) end)
                local comparatorError = pcall(function()
                    table.sort({2, 1}, function() error('sort failure') end)
                end)
                return invalidComparator == false
                    and incomparableValues == false
                    and comparatorError == false
                """).asBoolean());
    }
}
