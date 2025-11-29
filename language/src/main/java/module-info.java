module org.graalvm.lua {
  requires java.base;
  requires java.logging;
  requires jdk.unsupported;
  requires org.antlr.antlr4.runtime;
  requires org.graalvm.polyglot;
  requires org.graalvm.truffle;
  requires java.management;
    exports com.zhhz.truffle.lua;
  provides  com.oracle.truffle.api.provider.TruffleLanguageProvider with
          LuaLanguageProvider;
}
