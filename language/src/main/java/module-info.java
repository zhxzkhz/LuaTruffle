module org.graalvm.lua {
  requires java.base;
  requires java.logging;
  requires jdk.unsupported;
  requires org.antlr.antlr4.runtime;
  requires org.graalvm.polyglot;
  requires org.graalvm.truffle;
  requires java.management;
    exports com.zhhz.truffle.lua;
    // launcher 的中断协议经反射调用 LuaInterrupt.request()（--interrupt-file 协作中断）
    exports com.zhhz.truffle.lua.runtime;
    // Lua 5.3 预编译 chunk 的模型与 dump/undump（string.dump / load 二进制块使用）
    exports com.zhhz.truffle.lua.bytecode;
  provides  com.oracle.truffle.api.provider.TruffleLanguageProvider with
          com.zhhz.truffle.lua.LuaLanguageProvider;
  provides com.oracle.truffle.api.instrumentation.provider.TruffleInstrumentProvider with
          com.zhhz.truffle.lua.LuaHookInstrumentProvider;
}
