
open module org.graalvm.lua.test {
    requires java.logging;
    requires jdk.unsupported;
    requires org.graalvm.polyglot;
    requires org.graalvm.truffle;
    requires org.graalvm.lua;
    requires org.junit.jupiter.api;

    exports com.zhhz.truffle.lua.test;
}
