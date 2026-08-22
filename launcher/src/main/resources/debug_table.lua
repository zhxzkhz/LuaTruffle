local t = {}
local f = function() return 1 end
print(type(t))
print(type(f))
t.value = f
print(type(t.value))
print(t.value == f)
function t:method()
  return 7
end
print(type(t.method))
print(t:method())
