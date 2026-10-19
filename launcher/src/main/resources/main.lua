-- ==========================================================
-- 微型测试框架
-- ==========================================================
local pass_count = 0
local fail_count = 0

local function assertEqual(actual, expected, test_name)
    if actual == expected then
        pass_count = pass_count + 1
        print("[PASS] " .. test_name)
    else
        fail_count = fail_count + 1
        print("[FAIL] " .. test_name .. " | Expected: " .. tostring(expected) .. ", Got: " .. tostring(actual))
    end
end

local function assertTrue(condition, test_name)
    assertEqual(condition, true, test_name)
end

-- ==========================================================
-- 1. 基础数据类型与运算符测试
-- ==========================================================
local function test_basics()
    print("--- Testing Basics & Operators ---")
    
    -- 算术运算
    assertEqual(1 + 2 * 3, 7, "Basic Arithmetic (+, *)")
    assertEqual(10 / 2 - 1, 4, "Basic Arithmetic (/, -)")
    assertEqual(10 % 3, 1, "Modulo operator (%)")
    assertEqual(2 ^ 3, 8, "Power operator (^)")
    
    -- 关系运算
    assertTrue(5 > 3, "Greater than (>)")
    assertTrue(3 <= 3, "Less or equal (<=)")
    assertTrue(1 ~= 2, "Not equal (~=)")
    
    -- 逻辑运算
    assertTrue(true and true, "Logical AND")
    assertTrue(false or true, "Logical OR")
    assertTrue(not false, "Logical NOT")
    assertEqual(nil or 10, 10, "Logical OR short-circuit")
    assertEqual(10 and 20, 20, "Logical AND short-circuit")

    -- 字符串
    assertEqual("Hello" .. " " .. "World", "Hello World", "String concatenation (..)")
    assertEqual(#"Lua", 3, "String length operator (#)")
end

-- ==========================================================
-- 2. 控制流测试 (if, while, for)
-- ==========================================================
local function test_control_flow()
    print("--- Testing Control Flow ---")
    
    -- If / Elseif / Else
    local a = 10
    local res = 0
    if a < 5 then res = 1
    elseif a == 10 then res = 2
    else res = 3 end
    assertEqual(res, 2, "If-elseif-else statement")

    -- While loop
    local count = 0
    while count < 5 do
        count = count + 1
    end
    assertEqual(count, 5, "While loop")

    -- Numeric For loop
    local sum = 0
    for i = 1, 10, 2 do
        sum = sum + i
    end
    assertEqual(sum, 25, "Numeric for loop with step")

    -- Repeat Until loop
    local x = 0
    repeat
        x = x + 1
    until x == 3
    assertEqual(x, 3, "Repeat-until loop")
end

-- ==========================================================
-- 3. 函数与闭包 (Lexical Scoping)
-- ==========================================================
local function test_functions()
    print("--- Testing Functions & Closures ---")
    
    -- 基础函数与多返回值
    local function multi_ret(a, b)
        return a + b, a - b
    end
    local sum, diff = multi_ret(10, 5)
    assertEqual(sum, 15, "Multiple return values (1)")
    assertEqual(diff, 5, "Multiple return values (2)")

    -- 变长参数 (...)
    local function vararg_sum(...)
        local s = 0
        local args = {...}
        for i = 1, #args do
            s = s + args[i]
        end
        return s
    end
    assertEqual(vararg_sum(1, 2, 3, 4), 10, "Varargs (...)")

    -- 闭包与 Upvalues (极容易写错的解释器特性)
    local function make_counter()
        local count = 0
        return function()
            count = count + 1
            return count
        end
    end
    local c1 = make_counter()
    local c2 = make_counter()
    assertEqual(c1(), 1, "Closure upvalue (c1 step 1)")
    assertEqual(c1(), 2, "Closure upvalue (c1 step 2)")
    assertEqual(c2(), 1, "Closure upvalue (c2 independent)")
end

-- ==========================================================
-- 4. 表 (Tables) 测试
-- ==========================================================
local function test_tables()
    print("--- Testing Tables ---")
    
    -- 数组与哈希
    local t = {10, 20, 30, key = "value"}
    assertEqual(t[1], 10, "Table array access")
    assertEqual(t.key, "value", "Table hash access (. syntax)")
    assertEqual(#t, 3, "Table length operator (#)")

    -- 方法调用 (: 语法糖)
    local obj = { val = 100 }
    function obj:add(n)
        self.val = self.val + n
    end
    obj:add(50)
    assertEqual(obj.val, 150, "Table method call (: syntax)")

    -- 泛型 for 循环 (需确保你的解释器实现了 pairs/ipairs，如果没有可以注释掉)
    if type(ipairs) == "function" then
        local sum = 0
        for i, v in ipairs({1, 2, 3}) do
            sum = sum + v
        end
        assertEqual(sum, 6, "Generic for loop (ipairs)")
    end
end

-- ==========================================================
-- 5. 元表 (Metatables) 测试
-- ==========================================================
local function test_metatables()
    print("--- Testing Metatables ---")
    
    -- __index
    local t1 = {}
    setmetatable(t1, { __index = function(t, k) return k .. "_generated" end })
    assertEqual(t1.hello, "hello_generated", "Metatable __index (function)")

    local fallback = { x = 99 }
    local t2 = setmetatable({}, { __index = fallback })
    assertEqual(t2.x, 99, "Metatable __index (table)")

    -- __newindex
    local history = {}
    local t3 = setmetatable({}, {
        __newindex = function(t, k, v)
            history[k] = v
        end
    })
    t3.new_key = "new_val"
    assertEqual(t3.new_key, nil, "Metatable __newindex (table empty)")
    assertEqual(history.new_key, "new_val", "Metatable __newindex (history updated)")

    -- 运算符重载 (__add)
    local vec1 = {x = 1, y = 2}
    local vec2 = {x = 3, y = 4}
    setmetatable(vec1, {
        __add = function(a, b)
            return {x = a.x + b.x, y = a.y + b.y}
        end
    })
    local vec3 = vec1 + vec2
    assertEqual(vec3.x, 4, "Metatable __add (operator overloading)")
end

-- ==========================================================
-- 6. 异常处理测试 (pcall)
-- ==========================================================
local function test_errors()
    print("--- Testing Error Handling (pcall) ---")
    
    if type(pcall) == "function" then
        local status, err = pcall(function()
            error("Custom error")
        end)
        assertEqual(status, false, "pcall catches error")
        
        local status_ok, res = pcall(function() return 42 end)
        assertEqual(status_ok, true, "pcall success status")
        assertEqual(res, 42, "pcall success result")
    else
        print("[SKIP] pcall not implemented")
    end
end

-- ==========================================================
-- 7. 协程 (Coroutines) 测试 (进阶)
-- ==========================================================
local function test_coroutines()
    print("--- Testing Coroutines ---")
    
    if type(coroutine) == "table" and type(coroutine.create) == "function" then
        local co = coroutine.create(function(a, b)
            coroutine.yield(a + b)
            return a - b
        end)
        
        local status, res1 = coroutine.resume(co, 10, 5)
        assertEqual(status, true, "Coroutine first resume status")
        assertEqual(res1, 15, "Coroutine yield value")
        
        local status2, res2 = coroutine.resume(co)
        assertEqual(status2, true, "Coroutine second resume status")
        assertEqual(res2, 5, "Coroutine return value")
        assertEqual(coroutine.status(co), "dead", "Coroutine status is dead")
    else
        print("[SKIP] Coroutine library not implemented")
    end
end


-- ==========================================================
-- 运行所有测试
-- ==========================================================
print("========================================")
print("      Starting Lua Interpreter Test     ")
print("========================================")

test_basics()
test_control_flow()
test_functions()
test_tables()

-- 后面这几个属于进阶特性，如果你的解释器抛出异常导致进程退出，
-- 你可以先注释掉它们，一步一步调试。
test_metatables()
test_errors()
--test_coroutines()

print("========================================")
print("TEST RESULTS:")
print("Passed: " .. pass_count)
print("Failed: " .. fail_count)
print("Total:  " .. (pass_count + fail_count))

if fail_count == 0 then
    print(">>> SUCCESS! ALL TESTS PASSED! <<<")
else
    print(">>> SOME TESTS FAILED. CHECK LOGS. <<<")
end



require("class")

AA = class("AA")

function AA:ctor()
    self.listeners = {}
    return self
end

function AA:aa()
    self.listeners["aa"] = {}
    print("self.listeners[\"aa\"]",self.listeners["aa"])
end

AA:new():aa()
print("结束")