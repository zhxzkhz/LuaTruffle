---@class ClassDef
--- 核心面向对象基类函数
---@param classname string 类名
---@param super table|nil 父类
function class(classname, super)
    -- 1. 创建类表
    local cls = {}

    -- 2. 保存元数据
    cls.__cname = classname
    cls.super = super
    
    -- 3. 设置索引：实例查找方法时，去 cls 里找
    cls.__index = cls
    
    -- 【可选优化】保护元表不被外部滥用修改。
    -- 但在热更开发期，建议先注释掉，方便 dump table 调试。
    -- cls.__metatable = classname
    
    -- 4. 实现继承：如果在 cls 里找不到，去 super 里找
    if super then
        setmetatable(cls, { __index = super })
    end

    -- ================= 优化点 1：将类方法定义在 new 外部 =================
    -- 这些方法属于类原型，所有实例共享，不能每次 new 时都重新定义一遍！

    --- 获取实例的哈希码 (内存地址标识)
    function cls:hashCode()
        return self.__hashcode
    end

    --- 定义默认的 __tostring 元方法
    function cls:__tostring()
        -- 优化点 2：支持继承链上的自定义 tostring 方法
        -- 如果当前实例或父类实现了自定义的 :tostring() 方法，则优先调用
        if type(self.tostring) == "function" then
            return self:tostring()
        end
        -- 默认输出: 类名:内存地址
        return self.__cname .. ":" .. self.__hashcode
    end
    -- =====================================================================

    -- 5. 构造入口
    function cls:new(...)
        -- 每次 new 创建一个全新的空表作为实例
        local instance = {}
        
        -- 优化点 3：在设置元表前获取原生内存地址，避免触发 __tostring 死循环
        -- Lua 原生 tostring(table) 格式通常为 "table: 0x12345678"
        local rawStr = tostring(instance)
        -- 提取冒号后面的地址部分，兼容性更强，比硬截取 sub(-10) 更稳健
        local hash = rawStr:sub(-10)
        instance.__hashcode = hash
        instance.__cname = classname
        -- 设置元表，将实例与类绑定
        -- 注意：这里用 self 代替 cls，支持更深层次的继承链
        -- setmetatable(instance, cls)
        setmetatable(instance, self)
        -- 调用构造函数
        if instance.ctor then
            instance:ctor(...)
        end

        return instance
    end

    return cls
end


---@class ClassDef
--- 核心面向对象基类函数 (修正版)
---@param classname string 类名
---@param super table|nil 父类
function class1(classname, super)
    local cls = {}
    cls.__cname = classname
    cls.super = super
    
    -- 核心修复：cls 既是类表，也作为实例的 __index 源
    cls.__index = cls

    -- 实现继承
    if super then
        -- 如果有父类，子类在找不到方法时，去父类找
        setmetatable(cls, { __index = super })
    end

    -- 仅在基类或未定义 new 的情况下定义 new
    -- 这样子类可以直接继承父类的 new，除非子类想特殊重写
    if not cls.new then
        ---@return table
        function cls:new(...)
            -- 这里的 self 在 Strike_Red:new() 调用时，指的就是 Strike_Red 类表
            local instance = {}
            
            -- 提取哈希码
            local rawStr = tostring(instance)
            instance.__hashcode = rawStr:sub(-10)
            instance.__cname = classname

            -- 将实例的元表指向调用者(子类类表)
            setmetatable(instance, self)
            
            -- 执行构造函数
            if self.ctor then
                self.ctor(instance, ...)
            end

            return instance
        end
    end

    -- 统一定义 hashCode 和 tostring
    function cls:hashCode()
        return self.__hashcode
    end

    function cls:__tostring()
        if type(self.tostring) == "function" then
            return self:tostring()
        end
        return (self.__cname or "Unknown") .. ":" .. (self.__hashcode or "0x0")
    end

    return cls
end


---@class Enum
---@field new Enum
_enum = class("_enum")

---@param index integer
---@param name string
---@return Enum
function _enum:ctor(index,name)
  self.index = index
  self.n = name
  return self
end

---@return string
function _enum:name()
  return self.n
end

---@return integer
function _enum:ordinal()
  return self.index
end

---@return string
function _enum:tostring()
  return self.n
end


---@class Texture

---@class Batch
local _Batch = {}


---@param texture Texture
---@param x number
---@param y number
---@param width number
---@param height number
---@param ... any
function _Batch.draw(texture,x,y,width,height,...)
  
end

---@param i number
function _Batch.setColor(i) end

function xlua_dump(o)
  local t = {}
  local _t = {}
  local _n = {}
  local space, deep = string.rep(' ', 2), 0
  local function _ToString(o, _k)
    if type(o) == ('number') then
      table.insert(t, o)
     elseif type(o) == ('string') then
      table.insert(t, string.format('%q', o))
     elseif type(o) == ('table') then
      local mt = getmetatable(o)
      if mt and (mt.__tostring or type(mt) == "string") then
        table.insert(t, tostring(o))
       else
        deep = deep + 2
        table.insert(t, '{')

        for k, v in pairs(o) do
          if v == _G then
            table.insert(t, string.format('\r\n%s%s\t=%s ,', string.rep(space, deep - 1), k, "_G"))
           elseif v ~= package.loaded then
            if tonumber(k) then
              k = string.format('[%s]', k)
             else
              k = string.format('[\"%s\"]', k)
            end
            table.insert(t, string.format('\r\n%s%s\t= ', string.rep(space, deep - 1), k))
            if v == nil then
              table.insert(t, string.format('%s ,',"nil"))
             else --if type(v) == ('table') then
              _ToString(v, _k)
            end
          end
        end
        table.insert(t, string.format('\r\n%s}', string.rep(space, deep - 1)))
        deep = deep - 2
      end
     else
      table.insert(t, tostring(o))
    end
    table.insert(t, " ,")
    return t
  end

  t = _ToString(o, '')
  return table.concat(t)
end