package com.zhhz.truffle.lua.builtins.pack;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.builtins.LoadBuiltinNode;
import com.zhhz.truffle.lua.builtins.LoadBuiltinNodeGen;
import com.zhhz.truffle.lua.builtins.LuaBuiltinNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaTable;

import java.io.IOException;

/**
 * 实现 Lua 的路径搜索器 (package.searchers[2])。
 * 负责利用 package.path 查找 Lua 文件并编译。
 */
@NodeInfo(shortName = "searcher_luapath")
public abstract class LuaPathSearcherNode extends LuaBuiltinNode {

    // 复用 SearchPath 节点来处理路径模板匹配逻辑
    @Child private SearchPathBuiltinNode searchPathNode = SearchPathBuiltinNodeGen.create();

    @Specialization
    public Object doSearch(Object[] args) {
        if (args.length == 0) {
            return toTruffleString("no module name provided");
        }

        Object nameObj = args[0];

        // 1. 获取全局的 package.path
        String path = getPackagePath();
        if (path == null) {
            return toTruffleString("no package.path");
        }

        // 2. 调用 package.searchpath(name, path)
        //    注意：searchPathNode 的 execute 方法参数可能需要适配，这里假设通用 execute
        //    它返回：成功 -> TruffleString(filePath); 失败 -> Object[]{nil, errorMsg}
        Object searchResult = searchPathNode.execute(new Object[]{nameObj, toTruffleString(path)});

        // 3. 处理搜索结果
        if (searchResult instanceof TruffleString || searchResult instanceof String) {
            // --- 情况 A: 找到了文件 ---
            String filePath = searchResult.toString();

            // 4. 加载并编译文件
            return loadFile(filePath, nameObj.toString());

        } else if (searchResult instanceof Object[]) {
            // --- 情况 B: 没找到，searchpath 返回了 [nil, errorMsg] ---
            Object[] results = (Object[]) searchResult;
            if (results.length > 1 && results[1] != null) {
                return results[1]; // 返回错误信息字符串
            }
        }

        return toTruffleString("\n\tno file found (searchpath returned unexpected result)");
    }

    /**
     * 从全局表获取 package.path
     */
    @CompilerDirectives.TruffleBoundary
    private String getPackagePath() {
        LuaContext context = getContext();
        LuaTable globals = context.getGlobals();

        Object pkg = globals.rawget("package");
        if (pkg instanceof LuaTable) {
            Object path = ((LuaTable) pkg).rawget("path");
            if (path != null) {
                return path.toString();
            }
        }
        return null;
    }

    /**
     * 读取文件并解析为函数。
     * 这是一个慢路径操作，必须在 Boundary 中进行。
     */
    @CompilerDirectives.TruffleBoundary
    private Object loadFile(String filePath, String moduleName) {
        TruffleLanguage.Env env = getContext().getEnv();
        TruffleFile file = env.getPublicTruffleFile(filePath);

        try {
            // 1. 构建 Source
            //    chunkName 通常以 @ 开头表示文件名
            Source source = Source.newBuilder(LuaLanguage.ID, file)
                    .name("@" + filePath)
                    .build();

            // 2. 解析 (编译)
            CallTarget callTarget = env.parseInternal(source);

            // 3. 包装为 LuaFunction
            LuaFunction loader = new LuaFunction(getContext().getFunctionShape(),callTarget);

            // 4. 成功！返回 (loader, filePath)
            //    Lua 标准规定 searcher 返回两个值：加载器函数和数据(通常是文件名)
            return new Object[]{ loader, toTruffleString(filePath) };

        } catch (SecurityException e) {
            return toTruffleString("\n\terror loading module '" + moduleName + "' from file '" + filePath + "':\n\tpermission denied");
        } catch (IOException e) {
            return toTruffleString("\n\terror loading module '" + moduleName + "' from file '" + filePath + "':\n\t" + e.getMessage());
        } catch (Exception e) {
            // 语法错误等
            return toTruffleString("\n\terror loading module '" + moduleName + "' from file '" + filePath + "':\n\t" + e.getMessage());
        }
    }

    private static TruffleString toTruffleString(String s) {
        return TruffleString.fromJavaStringUncached(s, TruffleString.Encoding.UTF_8);
    }
}