# Truffle-Lua 🌙


**Truffle-Lua** 是一个基于 Oracle [GraalVM Truffle](https://www.graalvm.org/graalvm-as-a-platform/language-implementation-framework/) 框架构建的高性能 Lua 5.3+ 解释器实现。

通过利用 Graal 编译器的部分求值（Partial Evaluation）和即时编译（JIT）技术，本项目旨在提供接近原生代码的 Lua 执行性能。

## ✨ 核心特性

*   **高性能 JIT 编译**：
    *   利用 **类型特化 (Type Specialization)** 技术，实现整数 (`long`) 和浮点数 (`double`) 的原生快速路径计算，消除装箱开销。
    *   实现 **内联缓存 (Inline Caching)** 和 **分派节点 (Dispatch Node)**，优化动态函数调用。
    *   支持 **栈上替换 (OSR)**，优化长时间运行的循环。
    *   解决递归内联爆炸问题，支持深度递归调用。
*   **完整的 Lua 语言特性**：
    *   **控制流**：`if`, `while`, `repeat-until`, 数值 `for`, 通用 `for-in`。
    *   **函数**：支持闭包 (Closures)、Upvalues、多返回值。
    *   **数据结构**：基于 `DynamicObject` 优化的高性能 Table 实现（混合数组部分与哈希部分）。
    *   **元机制**：完整支持 `setmetatable`/`getmetatable`，以及算术 (`__add` 等)、比较 (`__eq` 等)、访问 (`__index`/`__newindex`) 和调用 (`__call`) 元方法。
*   **协程**：`coroutine.create/resume/yield/status/wrap/running/isyieldable`，
    复刻标准 Lua 的 `nny`（不可让出层数）语义，支持嵌套 resume、`pcall` 内让出与挂起协程的清理。
*   **标准库支持**：
    *   `basic`: `print`, `type`, `tostring`, `tonumber`, `error`, `pcall`, `xpcall`, `assert`, `select`, `load`, `loadfile`, `dofile`, `rawget`, `rawset`, `collectgarbage`...
    *   `string`: `len`, `sub`, `upper`, `lower`, `rep`, `format`, `find`, `match`, `gmatch`, `gsub`, `dump`（含 Lua 模式匹配）。
    *   `table`: 构造、`insert`, `remove`, `concat`, `sort`（可让出性对齐参考实现）、`unpack`, `pack`，以及 `pairs`/`ipairs`/`next`。
    *   `math`, `os`, `io`：常用函数与文件读写。
    *   `debug`: `getinfo`, `getlocal`/`setlocal`, `getupvalue`/`setupvalue`, `traceback`, `sethook`/`gethook`（line/call/return/count，含 `tail call` 事件），
      AST 与字节码 VM 两种执行方式都可用；层级与尾调用标记对齐 Lua 5.4 的输出。
*   **两种执行方式**：AST 解释执行（默认）与 Lua 5.3 字节码 VM（`load`/`loadfile` 加载 `string.dump` 结果时自动使用）。
*   **Polyglot 互操作**：
    *   实现了 `InteropLibrary`，允许 Java 直接调用 Lua 函数，或 Lua 访问 Java 对象。
    *   支持 GraalVM Native Image AOT 编译。

## 🚀 性能基准

在 `fibonacci(40)` 的基准测试中，得益于 JIT 优化，本实现的性能表现优异：

*   **解释模式 (Interpreter)**: ~ 1.12s (JDK 21)
*   **JIT 编译后 (GraalVM)**: ~ 1.2s (JDK 21)
*   **解释模式 (Interpreter)**: ~ 0.7s (JDK 25)
*   **JIT 编译后 (GraalVM)**: ~ 0.32s (JDK 25)
*   **Lua 解释器**: ~ 4.6s
*   **Lua JIT**: ~ 0.96s

*(注：这是基于消除 `LuaNumber` 包装对象、打通原生 `long` 通道后的测试结果)*

## 🛠️ 构建与运行

### 前置要求

*   **JDK**: Java 21+ (推荐使用 **GraalVM for JDK 25** 以获得最佳性能)
*   **Maven**: 3.8+

### 编译

```bash
# 1. 标准构建
mvn package

# 2. 构建 Native Image (AOT 编译)
# 需要安装 native-image 工具: gu install native-image
mvn package -Pnative
```

### 运行 Lua 脚本

** 嵌入式调用 (Java API)**
```java
import org.graalvm.polyglot.*;

public class Main {
    public static void main(String[] args) {
        try (Context context = Context.newBuilder("lua").build()) {
            Value result = context.eval("lua", "return 10 + 20");
            System.out.println(result.asInt()); // 30
        }
    }
}
```

### 运行测试文件需如下参数
```bash
  -ea --add-opens org.graalvm.truffle/com.oracle.truffle.api.impl=ALL-UNNAMED --add-opens org.graalvm.truffle/com.oracle.truffle.polyglot=ALL-UNNAMED --enable-native-access=org.graalvm.truffle -Dpolyglot.engine.WarnInterpreterOnly=false
```

### 测试与回归

```bash
# 单元测试 + 字节码 VM 官方测试子集
mvn -pl language test

# 构建可运行的类路径（生成 standalone/cp.txt，依赖已安装到本地仓库）
mvn -q -pl launcher,standalone -am install -DskipTests
mvn -q -pl standalone package -DskipTests

# 运行官方 all.lua（--portable-tests 跳过依赖本地环境的部分）
java -ea --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow \
  -cp "$(Get-Content standalone/cp.txt)" com.zhhz.truffle.lua.launcher.LuaMain --portable-tests all.lua

# 性能基准
java -ea --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow \
  -cp "$(Get-Content standalone/cp.txt)" com.zhhz.truffle.lua.launcher.LuaMain bench_run.lua
```

`LuaVmOfficialTestSuiteTest` 会用 `string.dump`/`load` 把官方脚本转成字节码后执行，
覆盖 `coroutine.lua`、`attrib.lua`、`sort.lua`、`gc.lua` 等；调试库的已知取舍见
`DebugLibrary` 类注释。

## 📂 项目结构

*   `src/main/java/com/zhhz/truffle/lua/parser`: **前端解析**。基于 ANTLR4 的词法分析器和语法分析器，以及 `AstBuilder` (Visitor) 用于构建 AST。
*   `src/main/java/com/zhhz/truffle/lua/nodes`: **AST 节点**。
    *   `controlflow`: `IfNode`, `WhileNode`, `ForNode`, `BlockNode` 等。
    *   `expression`: 算术运算、逻辑运算、字面量。
    *   `access`: `Read/WriteLocal`, `Read/WriteUpvalue`, `Read/WriteGlobal`。
    *   `builtins`: `print`, `pairs` 等内置函数的实现节点。
*   `src/main/java/com/zhhz/truffle/lua/runtime`: **运行时对象**。
    *   `LuaFunction`: 支持闭包和 Interop 的函数对象。
    *   `LuaTable`: 基于 `DynamicObject` 的高性能表实现。
    *   `LuaContext`: 语言上下文，管理全局作用域和元表注册。

## 🔍 调试与开发

本项目支持使用 **Ideal Graph Visualizer (IGV)** 查看 Graal JIT 的编译图。

**启动参数：**
```bash
  -Dgraal.Dump=:3 -Dgraal.PrintGraph=Network
```

这可以帮助分析节点是否正确内联，以及 `FrameWithoutBoxing` 优化是否生效。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！目前主要待完善的功能包括：
*   `debug.getinfo(thread, ...)` / `getlocal(thread, ...)` 尚未切换到目标协程的栈。
*   `xpcall` 消息处理函数内看不到出错点的 C 帧（Truffle 异常展开时机所致）。
*   更多官方测试脚本（`db.lua`、`errors.lua` 等）的字节码 VM 覆盖。

## 📄 许可证

MIT License