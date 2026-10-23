-- Native Image 冒烟测试：库安装、位运算、尾调用、协程、钩子与错误处理。
-- 用法: ./standalone/target/luanative standalone/native-smoke.lua

local checks = 0
local function check(name, condition)
    if not condition then
        error("native smoke failed: " .. name, 0)
    end
    checks = checks + 1
end

-- 标准库通过反射安装；缺失时会是 nil 或空表。
local libraries = {"math", "string", "table", "os", "io", "utf8", "json", "bit32", "debug", "coroutine"}
for _, name in ipairs(libraries) do
    local library = _G[name]
    check("library " .. name, type(library) == "table" and next(library) ~= nil)
end

-- 64 位整数、浮点与二进制库。
check("math.min", math.min(3, 5) == 3 and math.type(math.min(3, 5)) == "integer")
check("math.max", math.max(3, 5.5) == 5.5)
check("bit32.lrotate", bit32.lrotate(0x12345678, 4) == 0x23456781)
check("bit32.rrotate", bit32.rrotate(0x12345678, 4) == 0x81234567)
check("string.format", string.format("%d %s", 42, "ok") == "42 ok")

-- 尾调用不应消耗 Java 栈。
local function tail(n, acc)
    if n == 0 then return acc end
    return tail(n - 1, acc + 1)
end
check("tail call", tail(200000, 0) == 200000)

-- 协程。
local co = coroutine.create(function(a)
    local b = coroutine.yield(a + 1)
    return b * 2
end)
local ok, value = coroutine.resume(co, 41)
check("coroutine yield", ok and value == 42)
ok, value = coroutine.resume(co, 21)
check("coroutine return", ok and value == 42)

-- debug 行钩子。
local hookLines = 0
debug.sethook(function() hookLines = hookLines + 1 end, "l")
local sum = 0
for i = 1, 10 do sum = sum + i end
debug.sethook()
check("debug hook", hookLines > 0 and sum == 55)

-- 错误处理。
local ok2, message = pcall(function() error("boom") end)
check("pcall", not ok2 and tostring(message):find("boom", 1, true) ~= nil)

-- Lua 5.4 特性：warn（默认关闭，可 @on 开启）与 coroutine.close。
check("warn and coroutine.close", type(warn) == "function" and type(coroutine.close) == "function")
local co = coroutine.create(function()
    local handle = setmetatable({}, {__close = function() end})
    coroutine.yield()
end)
check("coroutine resume", coroutine.resume(co))
check("coroutine close", coroutine.close(co) == true and coroutine.status(co) == "dead")

print("native smoke OK: " .. checks .. " checks")
