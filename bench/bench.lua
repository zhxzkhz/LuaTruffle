-- Stable CPU-time benchmark used by scripts/bench-baseline.ps1.
-- Cases are kept in the 0.1-0.7s range so the 15.6ms Windows thread-CPU clock
-- granularity stays a small fraction of each sample.
-- Usage: lua bench.lua [iterations]
-- Prints one "BENCH <case> <seconds>" line per iteration plus a SINK line.
local iterations = tonumber(arg and arg[1]) or 1
local sink = 0

local function fib(n)
  if n < 2 then return n end
  return fib(n - 1) + fib(n - 2)
end

local function loopCase()
  local s = 0
  for i = 1, 100000000 do s = s + i end
  return s
end

local function concatCase()
  local parts = {}
  for i = 1, 1000000 do parts[i] = tostring(i) end
  return #table.concat(parts)
end

local function run(name, measure)
  for _ = 1, iterations do
    local start = os.clock()
    sink = sink + measure()
    print(string.format("BENCH %s %.6f", name, os.clock() - start))
  end
end

-- Warm up once so the measured iterations run optimized code.
sink = sink + fib(35) + loopCase() + concatCase()

run("fib35", function() return fib(35) end)
run("loop100m", loopCase)
run("concat1m", concatCase)

print("SINK", sink)
