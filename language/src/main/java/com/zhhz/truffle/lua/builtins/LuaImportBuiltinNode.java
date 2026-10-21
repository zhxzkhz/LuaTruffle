package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNodeGen;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 内置函数 {@code import(name)}：导入 Java 类（宿主符号）。
 * <p>
 * 查找顺序：
 * <ol>
 *   <li>宿主符号：{@code Env.lookupHostSymbol(name)}，按全限定名导入 Java 类，
 *       如 {@code import('java.lang.String')}（需宿主访问权限，启动器以 allowAllAccess 运行）；</li>
 *   <li>polyglot bindings：其他语言通过 {@code context.getBindings().putMember} 导出的符号（保持旧行为）。</li>
 * </ol>
 * <p>
 * 全局注入：导入的符号会以简单名注入全局环境（不覆盖已有全局）——单类导入
 * {@code import('com.badlogic.gdx.Gdx')} 之后可直接用 {@code Gdx}；通配符导入
 * {@code import('com.badlogic.gdx.*')} 注入包内全部顶层类。调用本身仍返回导入的
 * 符号，{@code local x = import(...)} 的写法同样有效。
 * <p>
 * 调用约定：Java 对象统一用 {@code .} 调用——成员读取返回的宿主函数已绑定接收者
 * （{@code sb.append('x')} 即 Java 的 {@code sb.append("x")}），链式调用与取出函数
 * 后再调用均可；{@code :} 保持标准 Lua 语义（前置 self），不适用于宿主方法。
 * 类对象本身可像函数一样调用（即构造）：{@code local sb = StringBuilder('hi')}。
 * <p>
 * 优化：导入结果按符号名缓存在 {@link LuaContext} 中。宿主符号查找与 interop 成员读取
 * 都是 {@code @TruffleBoundary} 慢路径操作，重复导入同一符号（如循环中的 {@code import}）
 * 直接命中缓存，零查找开销。
 * <p>
 * 导入失败（非 Java 类且无 polyglot 符号）抛出 Lua 错误并给出符号名，
 * 避免静默返回 nil 导致后续 nil 索引错误难以定位。
 */
@NodeInfo(shortName = "import")
public abstract class LuaImportBuiltinNode extends LuaBuiltinNode {

    @Child private LuaCoerceToStringNode toStringNode = LuaCoerceToStringNodeGen.create();

    @Specialization
    public Object importSymbol(Object[] arguments,
                               @Cached TruffleString.ToJavaStringNode toJavaStringNode,
                               @CachedLibrary(limit = "3") InteropLibrary arrays,
                               @Bind LuaContext context) {
        if (arguments.length == 0) {
            throw LuaException.create("bad argument #1 to 'import' (string expected, got no value)", this);
        }
        String name = toJavaStringNode.execute(toStringNode.execute(arguments[0]));

        Object cached = context.getImportedSymbol(name);
        if (cached != null) {
            return cached;
        }
        Object symbol = importSymbolBoundary(name, context, arrays);
        cacheImportedSymbolBoundary(context, name, symbol);
        return symbol;
    }

    @CompilerDirectives.TruffleBoundary
    private static void cacheImportedSymbolBoundary(LuaContext context, String name, Object symbol) {
        context.cacheImportedSymbol(name, symbol);
    }

    /** 查找顺序：通配符包导入；宿主符号（Java 类）优先，其次 polyglot bindings；均未命中则抛错。 */
    @CompilerDirectives.TruffleBoundary
    private Object importSymbolBoundary(String name, LuaContext context, InteropLibrary arrays) {
        if (name.endsWith(".*")) {
            return importPackageBoundary(name, context);
        }
        if (context.getEnv().isHostLookupAllowed()) {
            try {
                Object symbol = context.getEnv().lookupHostSymbol(name);
                bindSimpleName(name, symbol, context);
                return symbol;
            } catch (SecurityException e) {
                throw LuaException.create("No polyglot access allowed.", this);
            } catch (RuntimeException e) {
                // 宿主符号不存在（如类名拼写错误），继续尝试 polyglot bindings
            }
        }
        try {
            Object symbol = arrays.readMember(context.getPolyglotBindings(), name);
            bindSimpleName(name, symbol, context);
            return symbol;
        } catch (UnsupportedMessageException | UnknownIdentifierException e) {
            throw LuaException.create("cannot import '" + name + "': not a host class or polyglot symbol", this);
        } catch (SecurityException e) {
            throw LuaException.create("No polyglot access allowed.", this);
        }
    }

    /**
     * 把导入的符号以其简单名（最后一段限定名）注入全局环境，与通配符导入的全局注入
     * 行为保持一致：{@code import('com.badlogic.gdx.Gdx')} 之后可直接写 {@code Gdx.app}。
     * 已存在的同名全局不会被覆盖。调用本身仍返回符号本身，{@code local x = import(...)}
     * 的写法同样有效。
     */
    private void bindSimpleName(String name, Object symbol, LuaContext context) {
        String simple = name.substring(name.lastIndexOf('.') + 1);
        if (simple.isEmpty()) {
            return;
        }
        LuaTable globals = context.getGlobals();
        TruffleString key = LuaContext.toTruffleString(simple);
        Object existing = globals.rawget(key);
        if (existing == null || existing == LuaNil.SINGLETON) {
            globals.rawset(key, symbol);
        }
    }

    /**
     * 通配符导入 {@code import('包名.*')}：扫描 classpath 上该包的顶层类，把每个类
     * 直接注入全局环境（简单类名 → 宿主类），Java 风格，之后可直接使用类名。
     * 已存在的同名全局不会被覆盖；跳过无法加载的类（可选依赖缺失、非 public 等）；
     * 包不存在或没有任何可解析类时报错，避免静默无效果难以定位。
     * 返回 nil（导入通过全局变量生效，本身无返回值）。
     */
    private Object importPackageBoundary(String name, LuaContext context) {
        String pkg = name.substring(0, name.length() - 2);
        if (!isValidPackageName(pkg)) {
            throw LuaException.create("cannot import '" + name + "': invalid package name", this);
        }
        Env env = context.getEnv();
        if (!env.isHostLookupAllowed()) {
            throw LuaException.create("No polyglot access allowed.", this);
        }
        ClassLoader loader = findClassLoader();

        LuaTable globals = context.getGlobals();
        int count = 0;
        for (String simpleName : LuaHostPackageScanner.scanPackage(pkg, loader)) {
            TruffleString key = LuaContext.toTruffleString(simpleName);
            Object existing = globals.rawget(key);
            if (existing != null && existing != LuaNil.SINGLETON) {
                continue; // 不覆盖已有的同名全局
            }
            try {
                // false：不触发静态初始化，与单类导入保持一致（首次访问成员时才初始化）
                Class<?> clazz = Class.forName(pkg + "." + simpleName, false, loader);
                globals.rawset(key, env.asHostSymbol(clazz));
                count++;
            } catch (ClassNotFoundException | LinkageError | RuntimeException ignored) {
                // 该类无法加载（可选依赖缺失、非可见等），跳过
            }
        }
        if (count == 0) {
            throw LuaException.create("cannot import '" + name + "': package not found on classpath or empty", this);
        }
        return LuaNil.SINGLETON;
    }

    /**
     * 包扫描用的 ClassLoader：宿主嵌入场景优先取调用方设置的上下文 ClassLoader
     * （即包含应用类如 libgdx 的那个），否则回退到语言自身被加载的 ClassLoader，
     * 最后回退到系统 ClassLoader。
     */
    private static ClassLoader findClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = LuaImportBuiltinNode.class.getClassLoader();
        }
        return loader != null ? loader : ClassLoader.getSystemClassLoader();
    }

    /** 包名格式校验：非空、不以点开头/结尾、无空段、不含 '.*' 以外的通配符。 */
    private static boolean isValidPackageName(String pkg) {
        if (pkg.isEmpty() || pkg.startsWith(".") || pkg.endsWith(".") || pkg.contains("*")) {
            return false;
        }
        for (String segment : pkg.split("\\.", -1)) {
            if (segment.isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
