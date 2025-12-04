package com.zhhz.truffle.lua.builtins.pack;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.builtins.LuaBuiltinNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

@NodeInfo(shortName = "package.searchpath")
public abstract class SearchPathBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @CompilerDirectives.TruffleBoundary
    public Object doSearch(Object[] args) {
        // 1. 参数检查
        if (args.length < 2) {
            throw LuaException.create("bad argument #1 to 'searchpath' (string expected)", this);
        }

        String name = ensureString(args, 0);
        String path = ensureString(args, 1);

        // 可选参数 sep (默认 ".")
        String sep = (args.length > 2) ? ensureString(args, 2) : ".";

        // 可选参数 rep (默认系统分隔符)
        String rep = (args.length > 3) ? ensureString(args, 3) : getContext().getEnv().getFileNameSeparator();

        // 2. 替换模块名中的分隔符 (foo.bar -> foo/bar)
        // 注意：正则表达式中 . 需要转义，但 String.replace 不需要
        String fileName = name.replace(sep, rep);

        StringBuilder errorMsg = new StringBuilder();
        TruffleLanguage.Env env = getContext().getEnv();

        // 3. 遍历路径模板
        // Lua 路径以分号分隔
        String[] templates = path.split(";");

        for (String template : templates) {
            if (template.isEmpty()) continue;

            // 将 ? 替换为文件名
            String actualPath = template.replace("?", fileName);

            // 使用 TruffleFile 检查文件
            TruffleFile file = env.getPublicTruffleFile(actualPath);

            if (file.exists() && file.isRegularFile()) {
                // 【成功】返回绝对路径
                // 返回 TruffleString 以保持一致性
                return toTruffleString(file.getAbsoluteFile().getPath());
            } else {
                // 记录错误信息，模拟 Lua 的标准输出格式
                errorMsg.append("\n\tno file '").append(actualPath).append("'");
            }
        }

        // 【失败】返回 nil 和错误消息
        return new Object[]{ LuaNil.SINGLETON, toTruffleString(errorMsg.toString()) };
    }

    // 辅助：确保参数是字符串
    private String ensureString(Object[] args, int index) {
        if (index < args.length) {
            Object arg = args[index];
            if (arg instanceof String) return (String) arg;
            if (arg instanceof TruffleString) return ((TruffleString) arg).toJavaStringUncached();
        }
        return null; // 或者抛出异常，视情况而定
    }

}