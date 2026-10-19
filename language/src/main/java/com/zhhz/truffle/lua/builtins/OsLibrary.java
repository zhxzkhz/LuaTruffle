package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

/** Implementation of Lua 5.3's {@code os} standard library. */
public final class OsLibrary {
    private static final String[] WEEKDAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    private OsLibrary() {
    }

    @LuaBuiltin(name = "clock")
    @TruffleBoundary
    public static double clock() {
        long time = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        return time < 0 ? 0.0 : time / 1.0e9;
    }

    @LuaBuiltin(name = "date")
    @TruffleBoundary
    public static Object date(Object[] arguments) {
        String format = arguments.length > 0 && !isNil(arguments[0]) ? string(arguments[0], 1, "date") : "%c";
        long seconds = arguments.length > 1 && !isNil(arguments[1]) ? integer(arguments, 1, "date") : epochSeconds();
        ZoneId zone = ZoneId.systemDefault();
        if (format.startsWith("!")) {
            zone = ZoneOffset.UTC;
            format = format.substring(1);
        }
        ZonedDateTime value;
        try {
            value = Instant.ofEpochSecond(seconds).atZone(zone);
        } catch (DateTimeException exception) {
            throw LuaException.create("time result cannot be represented", null);
        }
        if ("*t".equals(format)) {
            return dateTable(value);
        }
        return text(formatDate(format, value));
    }

    @LuaBuiltin(name = "difftime")
    public static double difftime(Object[] arguments) {
        return (double) integer(arguments, 0, "difftime") - integer(arguments, 1, "difftime");
    }

    @LuaBuiltin(name = "execute")
    @TruffleBoundary
    public static Object execute(Object[] arguments) {
        if (arguments.length == 0 || isNil(arguments[0])) {
            return true;
        }
        String command = string(arguments[0], 1, "execute");
        command = adaptInterruptKill(command);
        ProcessBuilder builder;
        try {
            if (isWindows()) {
                Path script = Files.createTempFile("lua_cmd_", ".sh");
                Files.writeString(script, command + "\n");
                script.toFile().deleteOnExit();
                builder = new ProcessBuilder("D:\\Program Files\\Git\\bin\\bash.exe",
                        script.toString().replace('\\', '/'));
            } else {
                builder = new ProcessBuilder("/bin/sh", "-c", command);
            }
        } catch (IOException exception) {
            return failure(exception);
        }
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        try {
            int status = builder.start().waitFor();
            return processResult(status);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new LuaMultiValue(new Object[]{LuaNil.SINGLETON, text("interrupted"), 1L});
        } catch (IOException exception) {
            return failure(exception);
        }
    }

    /**
     * 把 {@code kill -INT <pid>} 改写为 LuaTruffle 包装进程的中断协议。
     * <p>
     * MSYS 无法跨进程投递 SIGINT（且 bash 内建 kill 会绕过 PATH 上的同名脚本），
     * 这里在命令生成层改写：包装进程注册了 /tmp/lua_intr_kill_&lt;pid&gt; 映射
     * （内容：&lt;flag&gt; &lt;jpid&gt;），第 1 次 INT 即 touch 中断标志文件
     * （语言侧协作中断，可被 pcall 捕获）；重复 INT 强杀其 java 子进程
     * （模拟不可中断代码被反复中断后强制终止）。无映射时保持原命令。
     */
    @TruffleBoundary
    private static String adaptInterruptKill(String command) {
        java.util.regex.Matcher matcher = KILL_INT_PATTERN.matcher(command);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String pid = matcher.group(1);
            String map = "/tmp/lua_intr_kill_" + pid;
            String replacement = "{ if read -r flag jpid < \"" + map + "\" 2>/dev/null; then "
                    + "if [ -f \"$flag.fired\" ]; then /usr/bin/kill -9 \"$jpid\" 2>/dev/null; "
                    + "else touch \"$flag\" \"$flag.fired\" 2>/dev/null; fi; "
                    + "else /usr/bin/kill -INT " + pid + " 2>/dev/null; fi; }";
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static final java.util.regex.Pattern KILL_INT_PATTERN =
            java.util.regex.Pattern.compile("\\bkill\\s+-INT\\s+([0-9]+)");

    @LuaBuiltin(name = "exit")
    @TruffleBoundary
    public static Object exit(Object[] arguments) {
        int status = 0;
        if (arguments.length > 0 && !isNil(arguments[0])) {
            Object value = arguments[0];
            if (value instanceof Boolean booleanValue) {
                status = booleanValue ? 0 : 1;
            } else if (value instanceof LuaBoolean booleanValue) {
                status = booleanValue.getValue() ? 0 : 1;
            } else {
                status = (int) integer(arguments, 0, "exit");
            }
        }
        System.exit(status);
        return LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "getenv")
    @TruffleBoundary
    public static Object getenv(Object[] arguments) {
        String value = System.getenv(stringArgument(arguments, 0, "getenv"));
        return value == null ? LuaNil.SINGLETON : text(value);
    }

    @LuaBuiltin(name = "remove")
    @TruffleBoundary
    public static Object remove(Object[] arguments) {
        try {
            Files.delete(LuaContext.get(null).resolveScriptPath(stringArgument(arguments, 0, "remove")));
            return true;
        } catch (IOException | RuntimeException exception) {
            return failure(exception);
        }
    }

    @LuaBuiltin(name = "rename")
    @TruffleBoundary
    public static Object rename(Object[] arguments) {
        try {
            Path from = LuaContext.get(null).resolveScriptPath(stringArgument(arguments, 0, "rename"));
            Path to = LuaContext.get(null).resolveScriptPath(stringArgument(arguments, 1, "rename"));
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException | RuntimeException exception) {
            return failure(exception);
        }
    }

    /** 当前 locale（仅 "C" 可设置成功；Java 无法实现 locale 相关的 strtod/ctype/collate） */
    private static volatile String currentLocale = "C";

    @LuaBuiltin(name = "setlocale")
    @TruffleBoundary
    public static Object setlocale(Object[] arguments) {
        String locale = arguments.length > 0 && !isNil(arguments[0]) ? string(arguments[0], 1, "setlocale") : null;
        String category = arguments.length > 1 && !isNil(arguments[1]) ? string(arguments[1], 2, "setlocale") : "all";
        if (!category.equals("all") && !category.equals("collate") && !category.equals("ctype")
                && !category.equals("monetary") && !category.equals("numeric") && !category.equals("time")) {
            return LuaNil.SINGLETON;
        }
        if (locale == null || locale.isEmpty()) {
            return text(currentLocale);
        }
        if ("C".equals(locale) || "POSIX".equals(locale)) {
            currentLocale = "C";
            return text("C");
        }
        // 标准 Lua 依赖 C 运行时 locale（strtod 小数点、isalpha 字符分类、strcoll
        // 排序），Java 平台无法等价实现；对其他 locale 一律报告不支持。
        // 官方测试套件（literals.lua / strings.lua）对不可用 locale 有优雅降级分支。
        return LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "time")
    @TruffleBoundary
    public static long time(Object[] arguments) {
        if (arguments.length == 0 || isNil(arguments[0])) {
            return epochSeconds();
        }
        if (!(arguments[0] instanceof LuaTable table)) {
            throw argumentError(1, "time", "table expected");
        }
        int year = tableInteger(table, "year", true, 0);
        int month = tableInteger(table, "month", true, 0);
        int day = tableInteger(table, "day", true, 0);
        int hour = tableInteger(table, "hour", false, 12);
        int minute = tableInteger(table, "min", false, 0);
        int second = tableInteger(table, "sec", false, 0);
        try {
            ZonedDateTime value = LocalDateTime.of(year, 1, 1, 0, 0)
                    .plusMonths((long) month - 1)
                    .plusDays((long) day - 1)
                    .plusHours(hour)
                    .plusMinutes(minute)
                    .plusSeconds(second)
                    .atZone(ZoneId.systemDefault());
            fillDateTable(table, value);
            return value.toEpochSecond();
        } catch (DateTimeException exception) {
            throw LuaException.create("field value out of bounds", null);
        }
    }

    @LuaBuiltin(name = "tmpname")
    @TruffleBoundary
    public static TruffleString tmpname() {
        try {
            // Standard Lua's os.tmpname does not add a file extension.
            // Using "" suffix to avoid dots in the path (which break require's module name resolution)
            Path path = Files.createTempFile("lua_", "");
            Files.delete(path);
            return text(isWindows() ? path.toString().replace('\\', '/') : path.toString());
        } catch (IOException exception) {
            throw LuaException.create("unable to generate a unique filename", null);
        }
    }

    private static LuaTable dateTable(ZonedDateTime value) {
        LuaTable table = new LuaTable(LuaContext.get(null).getTableShape());
        fillDateTable(table, value);
        return table;
    }

    private static void fillDateTable(LuaTable table, ZonedDateTime value) {
        table.rawset("year", (long) value.getYear());
        table.rawset("month", (long) value.getMonthValue());
        table.rawset("day", (long) value.getDayOfMonth());
        table.rawset("hour", (long) value.getHour());
        table.rawset("min", (long) value.getMinute());
        table.rawset("sec", (long) value.getSecond());
        table.rawset("wday", (long) value.getDayOfWeek().getValue() % 7 + 1);
        table.rawset("yday", (long) value.getDayOfYear());
        table.rawset("isdst", value.getZone().getRules().isDaylightSavings(value.toInstant()));
    }

    private static String formatDate(String format, ZonedDateTime value) {
        StringBuilder result = new StringBuilder(format.length() + 16);
        for (int i = 0; i < format.length(); i++) {
            char current = format.charAt(i);
            if (current != '%') {
                result.append(current);
                continue;
            }
            if (++i >= format.length()) throw LuaException.create("invalid conversion specifier '%'", null);
            char conversion = format.charAt(i);
            String replacement = switch (conversion) {
                case '%' -> "%";
                case 'a' -> weekday(value).substring(0, 3);
                case 'A' -> weekday(value);
                case 'b', 'h' -> month(value).substring(0, 3);
                case 'B' -> month(value);
                case 'c' -> formatDate("%a %b %e %H:%M:%S %Y", value);
                case 'C' -> twoDigits(Math.floorDiv(value.getYear(), 100));
                case 'd' -> twoDigits(value.getDayOfMonth());
                case 'D' -> formatDate("%m/%d/%y", value);
                case 'e' -> String.format(Locale.ROOT, "%2d", value.getDayOfMonth());
                case 'F' -> formatDate("%Y-%m-%d", value);
                case 'g' -> twoDigits(value.get(WeekFields.ISO.weekBasedYear()) % 100);
                case 'G' -> Integer.toString(value.get(WeekFields.ISO.weekBasedYear()));
                case 'H' -> twoDigits(value.getHour());
                case 'I' -> twoDigits(value.getHour() % 12 == 0 ? 12 : value.getHour() % 12);
                case 'j' -> String.format(Locale.ROOT, "%03d", value.getDayOfYear());
                case 'm' -> twoDigits(value.getMonthValue());
                case 'M' -> twoDigits(value.getMinute());
                case 'n' -> "\n";
                case 'p' -> value.getHour() < 12 ? "AM" : "PM";
                case 'r' -> formatDate("%I:%M:%S %p", value);
                case 'R' -> formatDate("%H:%M", value);
                case 'S' -> twoDigits(value.getSecond());
                case 't' -> "\t";
                case 'T', 'X' -> formatDate("%H:%M:%S", value);
                case 'u' -> Integer.toString(value.getDayOfWeek().getValue());
                case 'U' -> twoDigits(weekNumber(value, DayOfWeek.SUNDAY));
                case 'V' -> twoDigits(value.get(WeekFields.ISO.weekOfWeekBasedYear()));
                case 'w' -> Integer.toString(value.getDayOfWeek().getValue() % 7);
                case 'W' -> twoDigits(weekNumber(value, DayOfWeek.MONDAY));
                case 'x' -> formatDate("%m/%d/%y", value);
                case 'y' -> twoDigits(Math.floorMod(value.getYear(), 100));
                case 'Y' -> Integer.toString(value.getYear());
                case 'z' -> value.format(DateTimeFormatter.ofPattern("xx", Locale.ROOT));
                case 'Z' -> value.getZone().getDisplayName(TextStyle.SHORT, Locale.getDefault());
                default -> throw LuaException.create("invalid conversion specifier '%" + conversion + "'", null);
            };
            result.append(replacement);
        }
        return result.toString();
    }

    private static int weekNumber(ZonedDateTime value, DayOfWeek firstDay) {
        return value.get(WeekFields.of(firstDay, 7).weekOfYear()) - 1;
    }

    private static int tableInteger(LuaTable table, String field, boolean required, int defaultValue) {
        Object value = table.rawget(field);
        if (isNil(value)) {
            if (required) throw LuaException.create("field '" + field + "' missing in date table", null);
            return defaultValue;
        }
        long number = integerValue(value, "field '" + field + "' is not an integer");
        if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
            throw LuaException.create("field '" + field + "' is out of bounds", null);
        }
        return (int) number;
    }

    private static Object processResult(int status) {
        return new LuaMultiValue(new Object[]{status == 0 ? true : LuaNil.SINGLETON, text("exit"), (long) status});
    }

    private static LuaMultiValue failure(Exception exception) {
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        return new LuaMultiValue(new Object[]{LuaNil.SINGLETON, text(message), 1L});
    }

    private static String stringArgument(Object[] arguments, int index, String function) {
        if (index >= arguments.length) throw argumentError(index + 1, function, "string expected, got no value");
        return string(arguments[index], index + 1, function);
    }

    private static String string(Object value, int argument, String function) {
        if (value instanceof TruffleString string) return string.toJavaStringUncached();
        if (value instanceof String string) return string;
        throw argumentError(argument, function, "string expected");
    }

    private static long integer(Object[] arguments, int index, String function) {
        if (index >= arguments.length) throw argumentError(index + 1, function, "number expected, got no value");
        return integerValue(arguments[index], "bad argument #" + (index + 1) + " to '" + function + "' (integer expected)");
    }

    private static long integerValue(Object value, String error) {
        if (value instanceof Long number) return number;
        if (value instanceof Double number && Double.isFinite(number) && number == Math.rint(number)
                && number >= -0x1.0p63 && number < 0x1.0p63) return (long) number.doubleValue();
        throw LuaException.create(error, null);
    }

    private static RuntimeException argumentError(int index, String function, String message) {
        return LuaException.create("bad argument #" + index + " to '" + function + "' (" + message + ")", null);
    }

    private static boolean isNil(Object value) {
        return value == null || value == LuaNil.SINGLETON;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static long epochSeconds() {
        return Instant.now().getEpochSecond();
    }

    private static String weekday(ZonedDateTime value) {
        return WEEKDAYS[value.getDayOfWeek().getValue() % 7];
    }

    private static String month(ZonedDateTime value) {
        return MONTHS[value.getMonthValue() - 1];
    }

    private static String twoDigits(int value) {
        return String.format(Locale.ROOT, "%02d", value);
    }

    private static TruffleString text(String value) {
        return LuaContext.toTruffleString(value);
    }
}
