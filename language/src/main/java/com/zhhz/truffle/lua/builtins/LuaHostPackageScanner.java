package com.zhhz.truffle.lua.builtins;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 扫描 classpath 上指定 Java 包内的顶层类（不递归子包），供 {@code import('包名.*')} 使用。
 * <p>
 * 支持三种 classpath 条目：目录（file）、jar/zip（jar、zip 协议）以及 JDK 模块镜像
 * （jrt 协议，即 {@code import('java.util.*')} 导入 JDK 类的场景）。
 * 内部类（名称含 {@code $}）与 {@code module-info}/{@code package-info} 会被排除；
 * 无法读取的 classpath 条目按不存在处理，由调用方对空结果报错。
 */
final class LuaHostPackageScanner {

    private LuaHostPackageScanner() {
    }

    /** 返回包内顶层类的简单类名（不含包名），按名称排序保证结果稳定。 */
    static List<String> scanPackage(String packageName, ClassLoader loader) {
        String packagePath = packageName.replace('.', '/');
        TreeSet<String> classes = new TreeSet<>();
        try {
            Enumeration<URL> resources = loader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                switch (url.getProtocol()) {
                    case "file" -> scanDirectory(url, classes);
                    case "jar", "zip" -> scanJar(url, packagePath, classes);
                    case "jrt" -> scanJrt(url, classes);
                    default -> {
                    }
                }
            }
        } catch (IOException | URISyntaxException ignored) {
            // 条目不可读时按找不到处理
        }
        if (classes.isEmpty()) {
            // ClassLoader.getResources 不返回 JDK 模块镜像（jrt）中的资源，
            // 这里直接遍历 jrt 文件系统的所有模块查找该包（如 java.util.*）
            scanJrtModules(packagePath, classes);
        }
        return new ArrayList<>(classes);
    }

    private static void scanDirectory(URL url, TreeSet<String> classes) throws URISyntaxException {
        File[] files = new File(url.toURI()).listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                addClassName(file.getName(), classes);
            }
        }
    }

    private static void scanJar(URL url, String packagePath, TreeSet<String> classes) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        try (JarFile jar = connection.getJarFile()) {
            String prefix = packagePath + "/";
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName();
                if (!entryName.startsWith(prefix)) {
                    continue;
                }
                String simple = entryName.substring(prefix.length());
                if (simple.indexOf('/') >= 0) {
                    continue; // 子包内容
                }
                addClassName(simple, classes);
            }
        }
    }

    private static void scanJrt(URL url, TreeSet<String> classes) throws IOException {
        // jrt URL 形如 jrt:/java.base/java/util，对应 jrt 文件系统中的 /modules/java.base/java/util
        Path dir = FileSystems.getFileSystem(URI.create("jrt:/")).getPath("/modules" + url.getPath());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    addClassName(entry.getFileName().toString(), classes);
                }
            }
        } catch (NoSuchFileException ignored) {
        }
    }

    /** 在 jrt 文件系统的每个模块下查找 {@code packagePath} 对应的包目录。 */
    private static void scanJrtModules(String packagePath, TreeSet<String> classes) {
        try {
            Path modules = FileSystems.getFileSystem(URI.create("jrt:/")).getPath("/modules");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(modules)) {
                for (Path module : stream) {
                    Path packageDir = module.resolve(packagePath);
                    if (!Files.isDirectory(packageDir)) {
                        continue;
                    }
                    try (DirectoryStream<Path> files = Files.newDirectoryStream(packageDir)) {
                        for (Path entry : files) {
                            if (Files.isRegularFile(entry)) {
                                addClassName(entry.getFileName().toString(), classes);
                            }
                        }
                    }
                }
            }
        } catch (IOException ignored) {
            // 非 JDK9+ 镜像或 jrt 不可用时按找不到处理
        }
    }

    private static void addClassName(String fileName, TreeSet<String> classes) {
        if (!fileName.endsWith(".class")) {
            return;
        }
        String simple = fileName.substring(0, fileName.length() - ".class".length());
        if (simple.isEmpty() || simple.indexOf('$') >= 0
                || simple.equals("module-info") || simple.equals("package-info")) {
            return;
        }
        classes.add(simple);
    }
}
