package com.zhhz.truffle.lua.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 宿主 interop 契约测试。测试在启用断言的 surefire 下运行，
 * 因此可以覆盖 Truffle 的协议前置/后置条件校验。
 */
public class LuaInteropTest extends AbstractLuaTest {

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
    public void testGlobalScopeMembers() {
        Value bindings = context.getBindings("lua");
        bindings.putMember("hostNumber", 3);
        assertEquals(3, eval("return math.min(hostNumber, 5)").asInt());

        eval("guestValue = 'guest'");
        assertEquals("guest", bindings.getMember("guestValue").asString());
        assertTrue(bindings.hasMember("guestValue"));
        assertTrue(bindings.getMemberKeys().contains("hostNumber"));

        // 数字键属于数组协议，不能出现在成员键列表中（interop 只允许字符串成员）。
        eval("_G[1] = 'numeric'; _G[2.5] = 'float'");
        Set<String> keys = bindings.getMemberKeys();
        assertFalse(keys.contains("1"));
        assertFalse(keys.contains("2.5"));
    }

    @Test
    public void testTableMembersAndArrayElements() {
        Value table = eval("return {name = 'x', 10, 20, [2.5] = 'f'}");
        assertTrue(table.hasMembers());
        assertTrue(table.hasArrayElements());
        assertEquals(2, table.getArraySize());
        assertEquals(10, table.getArrayElement(0).asInt());
        assertEquals("x", table.getMember("name").asString());

        Set<String> members = table.getMemberKeys();
        assertTrue(members.contains("name"));
        assertFalse(members.contains("1"));
        assertFalse(members.contains("2.5"));

        table.putMember("extra", 5);
        assertEquals(5, table.getMember("extra").asInt());
        table.putMember("name", "y");
        assertEquals("y", table.getMember("name").asString());
    }

    @Test
    public void testFunctionIsExecutable() {
        Value function = eval("return function(a, b) return a + b end");
        assertTrue(function.canExecute());
        assertEquals(5, function.execute(2, 3).asInt());
    }

    @Test
    public void testFileHandleRoundTrip() throws IOException {
        Path file = Files.createTempFile("lua-interop", ".txt");
        try {
            Files.writeString(file, "abc");
            String path = file.toString().replace("\\", "\\\\");
            Value handle = eval("return io.open('" + path + "', 'r')");
            context.getBindings("lua").putMember("handle", handle);

            assertEquals("userdata", eval("return type(handle)").asString());
            assertEquals("file", eval("return io.type(handle)").asString());
            assertTrue(handle.hasMembers());
            assertTrue(handle.getMemberKeys().contains("read"));
            assertTrue(handle.getMemberKeys().contains("close"));
            assertTrue(handle.getMember("read").canExecute());
            assertEquals("abc", eval("return handle:read('a')").asString());
            assertTrue(eval("return handle:close()").asBoolean());
            assertEquals("closed file", eval("return io.type(handle)").asString());

            // 关闭后 Windows 上底层句柄应已释放，Lua 的 os.remove 才能成功。
            assertTrue(eval("return os.remove('" + path + "') == true").asBoolean());
        } finally {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // 文件可能已被 Lua 的 os.remove 删除
            }
        }
    }
}
