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
*   **协程**：`coroutine.create/resume/yield/status/wrap/running/isyieldable/close`，
    复刻标准 Lua 的 `nny`（不可让出层数）语义，支持嵌套 resume、`pcall` 内让出与挂起协程的清理。
*   **标准库支持**：
    *   `basic`: `print`, `type`, `tostring`, `tonumber`, `error`, `pcall`, `xpcall`, `assert`, `select`, `load`, `loadfile`, `dofile`, `rawget`, `rawset`, `collectgarbage`, `warn`...
    *   `string`: `len`, `sub`, `upper`, `lower`, `rep`, `format`, `find`, `match`, `gmatch`, `gsub`, `dump`（含 Lua 模式匹配）。
    *   `table`: 构造、`insert`, `remove`, `concat`, `sort`（可让出性对齐参考实现）、`unpack`, `pack`，以及 `pairs`/`ipairs`/`next`。
    *   `math`, `os`, `io`：常用函数与文件读写。
    *   `debug`: `getinfo`, `getlocal`/`setlocal`, `getupvalue`/`setupvalue`, `traceback`, `sethook`/`gethook`（line/call/return/count，含 `tail call` 事件），
      AST 与字节码 VM 两种执行方式都可用；层级与尾调用标记对齐 Lua 5.4 的输出。
*   **两种执行方式**：AST 解释执行（默认）与 Lua 5.3 字节码 VM（`load`/`loadfile` 加载 `string.dump` 结果时自动使用）。
*   **Polyglot 互操作**：
    *   实现了 `InteropLibrary`，允许 Java 直接调用 Lua 函数，或 Lua 访问 Java 对象。
    *   支持 GraalVM Native Image AOT 编译。

## 🧩 兼容性与互操作

*   **语言版本**：以 Lua 5.3 为基线（`_VERSION == "Lua 5.3"`，`string.dump` 与字节码 VM 使用 5.3 指令集），官方 Lua 5.3 测试套件（`all.lua`）通过。
*   **已实现的 Lua 5.4 特性**：
    *   `warn`：默认关闭，`warn("@on")` / `warn("@off")` 切换；启动参数 `-W` 直接开启，消息格式为 `Lua warning: ...`。
    *   `coroutine.close`：关闭挂起协程会执行其 to-be-closed 变量并返回 `true`；`__close` 或协程自身出错时返回 `false, err`；运行中/普通协程按参考实现的错误信息拒绝。
    *   `<const>` 赋值检查与 `<close>` to-be-closed 变量。
    *   `math.random`：采用与 PUC Lua 5.4 相同的 xoshiro256** 算法与种子推导，序列（含 `math.random(0)` 的完整 64 位结果）与参考实现一致。
    *   `debug.getinfo` 的 `S`/`u`/`L` 字段、`debug.setcstacklimit`、`string.gmatch` 的 `init` 参数、`collectgarbage` 的模式切换选项。
    *   弱表：`__mode = "k"` / `"v"` / `"kv"`，可回收对象作为键/值时在 `collectgarbage()` 后移除（数字、字符串、布尔键不受影响）。
    *   `utf8.char` 与 `"\u{...}"` 支持到 `0x7FFFFFFF`（5/6 字节扩展序列），`utf8.len`/`codepoint` 严格解码上限仍为 `0x10FFFF`。
    *   CLI：`-l g=mod` 别名、`-W` 警告开关、无脚本时 `arg[0]` 为解释器路径。
*   **已知差异**：`_VERSION` 仍报告 `"Lua 5.3"`；字节码与 `string.dump` 为 5.3 格式；`__gc` 终结器未实现（JVM 托管 GC 无法把已回收对象传给 `__gc`）；`collectgarbage` 的 `count`/`step`/`setpause` 为基于 JVM GC 的近似值；`package.searchers` 仅提供 Lua 文件与 preload 两种加载器。
*   **Java 互操作**：
    *   宿主对象通过 `InteropLibrary` 暴露字段、方法与数组；Lua 下标从 1 开始映射到宿主 0 基下标，越界读取为 `nil`、越界写入报错。
    *   宿主 `Map` 的键值可像字段一样读写（`config.timeout = 30`）；`obj:method(args)` 按宿主成员方法调用（不额外补 `self`）。
    *   宿主 `null` 统一映射为 Lua `nil`；Lua table 也向宿主暴露数组元素读写（`Value.get/setArrayElement`）。
    *   Lua 函数可作为宿主回调：宿主侧用 `Value.asValue(arg)` 包装后即可 `execute`。

## 🚀 性能基准

在 `fibonacci(40)` 的基准测试中，得益于 JIT 优化，本实现的性能表现优异：

*   **解释模式 (Interpreter)**: ~ 1.12s (JDK 21)
*   **JIT 编译后 (GraalVM)**: ~ 1.2s (JDK 21)
*   **解释模式 (Interpreter)**: ~ 0.7s (JDK 25)
*   **JIT 编译后 (GraalVM)**: ~ 0.32s (JDK 25)
*   **Lua 解释器**: ~ 4.6s
*   **Lua JIT**: ~ 0.96s

*(注：这是基于消除 `LuaNumber` 包装对象、打通原生 `long` 通道后的测试结果)*

> 上表是早期实现的实测结果，当前实现尚未达到该水平：本机复测 `fib(40)` 为
> native ~2.1s、JVM(JIT) ~2.7s（解释模式 ~140s）。已为热路径上的钩子/线程簿记
> 增加快速路径，native/JVM 相对上一版（~7.6s/~7.7s）分别提升约 3.6x/2.8x，
> 但距早期数据仍有差距，性能对齐/优化仍在进行中。
> 回归基线使用 `fib(35)` 等较短用例，见“测试与回归”。

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
# 产物: standalone/target/luanative(.exe)
# 冒烟测试：标准库安装、位运算、尾调用、协程、debug 钩子与错误处理
./standalone/target/luanative standalone/native-smoke.lua
# 官方测试套件（需先准备 all.lua 等测试文件）
./standalone/target/luanative --portable-tests all.lua
```

> Native Image 构建使用 `standalone/pom.xml` 中的 `-H:MaxRuntimeCompileMethods=250`；
> 标准库通过反射安装，相关元数据位于
> `language/src/main/resources/META-INF/native-image/org.graalvm.truffle/truffle-lua/`
> （`reflect-config.json` / `reachability-metadata.json`）。新增 `@LuaBuiltin` 库类时需同步登记。

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
# 依赖版本变化后必须重新生成，否则 Truffle 与 GraalVM 的 libgraal 版本不匹配会报
# UnsatisfiedLinkError: ...LibGraalScope.attachThreadTo
mvn -q -pl standalone dependency:build-classpath "-Dmdep.outputFile=$((Get-Location).Path)\standalone\cp.txt"

# 运行官方 all.lua（--portable-tests 跳过依赖本地环境的部分）
java -ea --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow \
  -cp "$(Get-Content standalone/cp.txt)" com.zhhz.truffle.lua.launcher.LuaMain --portable-tests all.lua

# 性能基准（CPU 时间；参数为每个用例的测量轮数）
./standalone/target/luanative bench/bench.lua 3
java -ea --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow \
  -cp "$(Get-Content standalone/cp.txt)" com.zhhz.truffle.lua.launcher.LuaMain bench/bench.lua 3

# 与基线比较：native 与 JVM 各取最快样本，超过容差（默认 25%）时退出码为 1
powershell -File scripts/bench-baseline.ps1
# 机器或实现变化后重新记录基线（写入 bench/baseline.json）
powershell -File scripts/bench-baseline.ps1 -Update
```

基线检查使用 `os.clock()`（当前线程 CPU 时间）排除 JIT 编译线程与 GC 线程的干扰，
每个用例取多轮最小值以减少噪声；可用 `-Runs`、`-Tolerance`、`-SkipNative`、`-SkipJvm`
调整。脚本依赖已构建的 `standalone/target/luanative(.exe)` 与 `launcher` jar。

基线用例为 `fib(35)`、`loop100m`、`concat1m`：Windows 上 `os.clock()` 的粒度约为
15.6ms，用例太短会让噪声占比过高。基线只用于同一口径下的相对回归，与上文
`fib(40)` 性能表的数值不可直接比较。

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
*   跨线程调试只支持字节码 VM 协程；AST 协程的挂起栈无法读取（Truffle 没有跨线程栈枚举 API）。
*   `debug.traceback` 对“由 C 函数调用”的 Lua 帧显示函数自身名字，参考实现显示 `<源文件:定义行>`。
*   更多官方测试脚本（`errors.lua`、`db.lua` 等）的字节码 VM 覆盖。

## 📄 许可证

MIT License