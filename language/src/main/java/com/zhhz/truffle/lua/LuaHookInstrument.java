package com.zhhz.truffle.lua;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;

/**
 * 内嵌 instrument：唯一职责是把引擎的 {@link Instrumenter} 经语言服务
 * （{@link LuaContext#lookupHookInstrumenter()}）暴露给 debug.sethook。
 * <p>
 * Truffle 的 Instrumenter 只能通过 {@link TruffleInstrument.Env#getInstrumenter()}
 * 获得；语言代码（Env.lookup(Class)）可以查到已注册 instrument 的服务对象，
 * 因此在 onCreate 时把自身注册为服务。
 */
@TruffleInstrument.Registration(id = LuaHookInstrument.ID, name = "lua-hook", services = LuaHookInstrument.class)
public final class LuaHookInstrument extends TruffleInstrument {

    public static final String ID = "lua-hook";

    private Instrumenter instrumenter;
    private TruffleLanguage.Env languageEnv;

    public Instrumenter getInstrumenterHandle() {
        return instrumenter;
    }

    @Override
    protected void onCreate(Env env) {
        this.instrumenter = env.getInstrumenter();
        // 语言侧通过 Env.lookup(LuaHookInstrument.class) 拿到本服务
        env.registerService(this);
    }

    public TruffleLanguage.Env getLanguageEnv() {
        return languageEnv;
    }

    public void setLanguageEnv(TruffleLanguage.Env env) {
        this.languageEnv = env;
    }
}
