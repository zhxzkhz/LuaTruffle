package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

@NodeInfo(shortName = "load")
public abstract class LoadBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    public Object doLoad(Object[] arguments,
                         @CachedLibrary(limit = "3") InteropLibrary interop) {

        if (arguments.length == 0) {
            // load() 至少需要一个参数
            return new LuaMultiValue(new Object[]{ null, toTruffleString("bad argument #1 to 'load' (string or function expected)") });
        }

        Object sourceProvider = arguments[0];
        String chunkName = getChunkName(arguments);

        // 获取当前的 Env
        Env env = getContext().getEnv();
        // 创建一个 Truffle Source 对象


        try {
            String sourceText = getSourceText(sourceProvider, interop);

            if (sourceText == null) {
                // 如果 getSourceText 返回 null，说明读取器已经读完，但内容为空
                // 编译空字符串会得到一个有效的、什么都不做的函数
                sourceText = "";
            }

            Source source = Source.newBuilder(LuaLanguage.ID, sourceText, chunkName).build();
            // 【核心】委托给 Truffle 框架进行解析
            // parseInternal 会返回一个 CallTarget
            var callTarget = env.parseInternal(source);
            // 将 CallTarget 包装成我们的 LuaFunction
            LuaFunction compiledFunction = new LuaFunction(getContext().getFunctionShape(), callTarget);

            // 【成功】返回函数
            return compiledFunction;

        } catch (LuaException e) {
            // 【失败】如果解析过程中抛出任何异常（语法错误等）
            // Truffle 会将其包装起来。我们捕获它并返回 nil 和错误信息。

            // 我们需要从异常中提取一个对用户友好的消息
            return new LuaMultiValue(new Object[]{LuaNil.SINGLETON, getErrorMessage(e) });
        } catch (Exception e){
            return new LuaMultiValue(new Object[]{LuaNil.SINGLETON, e.getMessage() });
        }
    }

    /**
     * 辅助方法：从 `load` 或 `loadstring` 的参数中获取源代码。
     */
    @TruffleBoundary
    private String getSourceText(Object sourceProvider, InteropLibrary interop) {
        // --- 情况 A: loadstring(string) ---
        if (sourceProvider instanceof TruffleString) {
            return ((TruffleString) sourceProvider).toJavaStringUncached();
        }

        // --- 情况 B: load(function) ---

        if (interop.isExecutable(sourceProvider)) {
            StringBuilder sb = new StringBuilder();
            while (true) {
                try {
                    // 反复调用读取器函数，不带任何参数
                    Object chunk = interop.execute(sourceProvider);

                    // 检查读取器是否返回了 nil 或空字符串，表示结束
                    if (chunk == null || interop.isNull(chunk)) {
                        break;
                    }

                    if (interop.isString(chunk) && interop.asString(chunk).isEmpty()) {
                        break;
                    }

                    // 将返回的块拼接到源代码中
                    if (interop.isString(chunk)) {

                        sb.append(interop.asString(chunk));
                    } else {
                        // 如果读取器返回了非字符串，是错误的
                        // load 会中止并返回一个错误，但为了简化，我们这里直接中断
                        break;
                    }
                } catch (LuaException e){
                    throw e;
                } catch (Exception e) {
                    // 如果读取器本身报错，load 也会失败
                    throw LuaException.create("error in reader function\n" + e.getMessage(),this);
                }
            }
            return sb.toString();
        }

        // 如果第一个参数既不是 string 也不是 function，报错
        throw LuaException.create("bad argument #1 to 'load' (string or function expected)",this);
    }

    // --- 其他辅助方法 ---

    @TruffleBoundary
    private String getChunkName(Object[] arguments) {
        if (arguments.length > 1 && arguments[1] instanceof TruffleString) {
            return ((TruffleString) arguments[1]).toJavaStringUncached();
        }
        return "=(load)"; // 默认的 chunk 名
    }

    @TruffleBoundary
    private String getErrorMessage(LuaException e) {
        // Truffle 异常通常有很好的消息
        // PolyglotException 会提供更丰富的上下文
        StringBuilder sb = new StringBuilder();

        // 1. 尝试直接获取位置（适用于非内置函数的错误）
        SourceSection location = e.getLocation().getSourceSection();


        // 3. 格式化输出
        if (location != null) {
            sb.append(location.getSource().getName())
                    .append(":")
                    .append(location.getStartLine())
                    .append(": ");
        } else {
            sb.append("unknown location: ");
        }

        sb.append(e.getMessage());
        // 对于其他异常，返回类名
        return sb.toString();
    }

    private TruffleString toTruffleString(String s) {
        return TruffleString.fromJavaStringUncached(s, TruffleString.Encoding.UTF_8);
    }
}
