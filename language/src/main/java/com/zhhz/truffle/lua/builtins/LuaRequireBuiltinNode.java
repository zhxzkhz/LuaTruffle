package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

/**
 * 实现 Lua 的 `require(modname)` 函数。
 * <p>
 * 流程：
 * 1. 检查 package.loaded[modname]。
 * 2. 遍历 package.searchers。
 * 3. 调用 searcher(modname) -> 返回 loader 和 data。
 * 4. 调用 loader(modname, data) -> 返回 module result。
 * 5. 存入 package.loaded 并返回。
 */
@NodeInfo(shortName = "require")
public abstract class LuaRequireBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doRequire(Object[] args,
                            @CachedLibrary(limit = "3") InteropLibrary interop) {
        if (args.length == 0) {
            throw LuaException.create("bad argument #1 to 'require' (string expected, got no value)", this);
        }

        Object nameObj = args[0];
        TruffleString modName;
        if (nameObj instanceof TruffleString) {
            modName = (TruffleString) nameObj;
        } else if (nameObj instanceof String) {
            modName = toTruffleString((String) nameObj);
        } else {
            throw LuaException.create("bad argument #1 to 'require' (string expected)", this);
        }

        // 1. 检查 package.loaded (快速路径)
        Object loadedValue = checkLoaded(modName);
        if (loadedValue != null) {
            return loadedValue;
        }

        // 2. 执行搜索和加载逻辑 (慢路径，放入 Boundary)
        return loadModuleBoundary(modName, interop);
    }

    /**
     * 检查模块是否已加载。
     */
    @CompilerDirectives.TruffleBoundary
    private Object checkLoaded(TruffleString modName) {
        LuaTable globals = getContext().getGlobals();
        Object pkg = globals.rawget("package");
        if (pkg instanceof LuaTable) {
            Object loaded = ((LuaTable) pkg).rawget("loaded");
            if (loaded instanceof LuaTable) {
                Object value = ((LuaTable) loaded).rawget(modName);
                // 如果值存在且不为 nil (null 或 LuaNil)，则直接返回
                if (value != null && value != LuaNil.SINGLETON) {
                    return value;
                }
            }
        }
        return null; // 未加载
    }

    /**
     * 核心加载逻辑：遍历 searchers，调用 loader，缓存结果。
     */
    @CompilerDirectives.TruffleBoundary
    private Object loadModuleBoundary(TruffleString modName, InteropLibrary interop) {
        LuaContext context = getContext();
        LuaTable globals = context.getGlobals();

        // 获取 package.searchers
        LuaTable pkg = (LuaTable) globals.rawget("package");
        if (pkg == null) {
            throw LuaException.create("'package' must be a table", this);
        }

        Object searchersObj = pkg.rawget("searchers");
        if (!(searchersObj instanceof LuaTable searchers)) {
            throw LuaException.create("'package.searchers' must be a table", this);
        }

        StringBuilder errorMsg = new StringBuilder();
        Object loader = null;
        Object loaderData = null;

        // 3. 遍历 searchers (Lua 数组索引从 1 开始)
        // 我们假设 searchers 是一个连续数组，遇到 nil 停止，或者遍历到一个合理的上限
        for (int i = 1; ; i++) {
            Object searcher = searchers.arrayRawGet(i); // 使用快速数组读取
            if (searcher == null || searcher == LuaNil.SINGLETON) {
                // 没有更多的 searcher 了
                break;
            }

            try {
                // 调用 searcher(modName)
                Object searchResult = interop.execute(searcher, modName);

                // searcher 返回规范：
                // - 成功: 返回 loader函数 (以及可选的 loaderData)
                // - 失败: 返回 错误字符串 或 nil

                // 处理多返回值 (Object[])
                if (searchResult instanceof Object[] results) {
                    if (results.length > 0) {
                        Object first = results[0];
                        if (interop.isExecutable(first)) {
                            loader = first;
                            loaderData = results.length > 1 ? results[1] : null;
                            break; // 找到了！
                        }
                    }
                } else if (interop.isExecutable(searchResult)) {
                    // 单返回值且是函数
                    loader = searchResult;
                    // data 就是 nil
                    break; // 找到了！
                } else if (searchResult instanceof String || searchResult instanceof TruffleString) {
                    // 失败，返回了错误信息
                    errorMsg.append(searchResult);
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw LuaException.create("error calling package.searchers[" + i + "]: " + e.getMessage(), this);
            }
        }

        // 4. 如果没找到 loader，抛出错误
        if (loader == null) {
            throw LuaException.create("module '" + modName + "' not found:" + errorMsg, this);
        }

        // 5. 调用 loader(modName, loaderData)
        try {
            // 参数：模块名，以及 searcher 返回的额外数据（例如文件路径）
            Object[] loaderArgs = new Object[]{ modName, loaderData };

            Object result = interop.execute(loader, loaderArgs);

            // 处理 loader 的返回值
            // 如果 loader 返回了 Object[]，取第一个；否则取本身
            Object moduleValue;
            if (result instanceof Object[] && ((Object[]) result).length > 0) {
                moduleValue = ((Object[]) result)[0];
            } else {
                moduleValue = result;
            }

            // 6. 缓存结果到 package.loaded
            LuaTable loaded = (LuaTable) pkg.rawget("loaded");

            // 如果 moduleValue 不是 nil，使用它
            if (moduleValue != null && moduleValue != LuaNil.SINGLETON) {
                loaded.rawset(modName, moduleValue);
                return moduleValue;
            } else {
                // 如果 loader 没有返回值 (返回 nil)，
                // Lua 检查 package.loaded[modName] 是否被 loader 手动设置了
                Object existing = loaded.rawget(modName);
                if (existing != null && existing != LuaNil.SINGLETON) {
                    return existing;
                } else {
                    // 如果都没设置，默认设为 true
                    loaded.rawset(modName, true);
                    return true;
                }
            }

        } catch (Exception e) {
            // 如果在加载过程中出错，清理 loaded 表（虽然标准Lua不一定清理，但为了安全）
            // loaded.rawset(modName, null); 
            throw LuaException.create("error loading module '" + modName + "' from file '" + loaderData + "':\n" + e.getMessage(), this);
        }
    }
    /*
    @Specialization
    public Object doRequire(Object[] arguments,
                         @CachedLibrary(limit = "3") InteropLibrary interop,
                         @Bind LuaContext context) {
        TruffleString symbolName = (TruffleString) arguments[0];
        try {
            return context.getEnv().lookupHostSymbol(interop.asString(symbolName));
        } catch (UnsupportedMessageException e) {
            throw LuaException.create("module '" + symbolName + "' not found", this);
        } catch (SecurityException e) {
            throw LuaException.create("No polyglot access allowed.", this);
        }

    }
    */
}
