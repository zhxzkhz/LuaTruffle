package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 从 Lua 侧访问宿主对象/数组/函数，以及宿主回调 Lua 的约定。 */
public class LuaHostInteropTest extends AbstractLuaTest {

    public static class Point {
        public final int x = 3;
        public final int y = 4;

        public int sum() {
            return x + y;
        }

        public int plus(int value) {
            return x + value;
        }
    }

    public static class Failing {
        public void explode() {
            throw new IllegalStateException("host failure");
        }
    }

    private Context context;

    @BeforeEach
    public void setUp() {
        this.context = newContextBuilder("lua").build();
    }

    @AfterEach
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    private Value eval(String code) {
        return context.eval("lua", code);
    }

    @Test
    void readsHostFieldsAndCallsMethods() {
        context.getBindings("lua").putMember("point", new Point());
        assertEquals(3, eval("return point.x").asInt());
        assertEquals(7, eval("return point:sum()").asInt());
        assertEquals(10, eval("return point:plus(7)").asInt());
    }

    @Test
    void readsHostArraysAndLists() {
        context.getBindings("lua").putMember("arr", new int[]{10, 20, 30});
        assertEquals(10, eval("return arr[1]").asInt());
        assertTrue(eval("return arr[0] == nil").asBoolean());
        assertEquals(3, eval("return #arr").asInt());

        context.getBindings("lua").putMember("list", List.of("a", "b"));
        assertEquals("a", eval("return list[1]").asString());
        assertEquals(2, eval("return #list").asInt());
    }

    @Test
    void callsHostFunctions() {
        context.getBindings("lua").putMember("twice", (java.util.function.LongUnaryOperator) v -> v * 2);
        assertEquals(42, eval("return twice(21)").asInt());

        List<String> seen = new ArrayList<>();
        context.getBindings("lua").putMember("record", (java.util.function.Consumer<String>) seen::add);
        eval("record('hello')");
        assertEquals(List.of("hello"), seen);
    }

    @Test
    void hostFunctionErrorsBecomeLuaErrors() {
        context.getBindings("lua").putMember("failing", new Failing());
        PolyglotException error = assertThrows(PolyglotException.class,
                () -> eval("failing:explode()"));
        assertTrue(error.getMessage().contains("host failure"), error.getMessage());
    }

    @Test
    void luaFunctionsCanBePassedToHost() {
        context.getBindings("lua").putMember("apply", (Function<Object, Object>) f -> Value.asValue(f).execute(21).asInt());
        assertEquals(42, eval("return apply(function (v) return v * 2 end)").asInt());
    }

    @Test
    void hostSuppliersAreCallable() {
        context.getBindings("lua").putMember("answer", (Supplier<Integer>) () -> 42);
        assertEquals(42, eval("return answer()").asInt());
    }

    @Test
    void writesHostArrayElements() {
        int[] arr = {10, 20, 30};
        context.getBindings("lua").putMember("arr", arr);
        eval("arr[2] = 42");
        assertEquals(42, arr[1]);

        PolyglotException error = assertThrows(PolyglotException.class, () -> eval("arr[4] = 1"));
        assertTrue(error.getMessage().contains("array element"), error.getMessage());
    }

    @Test
    void readsAndWritesHostMapMembers() {
        Map<String, Object> config = new HashMap<>();
        config.put("name", "before");
        context.getBindings("lua").putMember("config", config);
        assertEquals("before", eval("return config.name").asString());
        eval("config.name = 'after'");
        assertEquals("after", config.get("name"));
    }

    @Test
    void hostCanReadAndWriteLuaTableArrays() {
        Value table = eval("return {10, 20}");
        assertEquals(10, table.getArrayElement(0).asInt());
        table.setArrayElement(1, 99);
        assertEquals(99, table.getArrayElement(1).asInt());
        assertEquals(2, table.getArraySize());
    }

    @Test
    void hostNullBecomesNil() {
        context.getBindings("lua").putMember("nothing", null);
        assertTrue(eval("return nothing == nil").asBoolean());
    }
}
