package com.ajaxjs.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于 Spring {@link PathMatchingResourcePatternResolver}实现资源扫描
 */
public class ResourceScanner {
    private static final String CLASS_SUFFIX = ".class";

    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private static Set<URI> getResources(String rootDirPath, String suffix) {
        if (null == rootDirPath)
            throw new NullPointerException("rootDirPath must not be null");

        String locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + rootDirPath;

        if (null != suffix)
            locationPattern += "/**/*" + suffix;

        Resource[] resources;

        try {
            resources = resolver.getResources(locationPattern);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return Stream.of(resources).map(input -> {
            try {
                return input.getURI();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).collect(Collectors.toSet());
    }

    /**
     * 扫描指定包名下的所有类
     *
     * @param packageName 包名
     */
    public static Set<Class<?>> getClasses(String packageName) {
        return getClasses(packageName, null);
    }

    /**
     * 扫描指定包名下的所有类
     *
     * @param packageName 包名
     * @param predicate   条件过滤器
     */
    public static Set<Class<?>> getClasses(String packageName, Predicate<Class<?>> predicate) {
        if (null == packageName)
            throw new NullPointerException("package name must not be null");

        String path = packageName.replace('.', '/');
        Set<URI> rootDirs = getResources(path, null), urls = getResources(path, CLASS_SUFFIX);
        Set<String> rootPrefixes = rootDirs.stream().map(input -> {
            String str = input.toString();
            return str.substring(0, str.length() - path.length());
        }).collect(Collectors.toSet());

        int csLength = CLASS_SUFFIX.length();
        ClassLoader classLoader = getDefaultClassLoader();
        Set<Class<?>> result = new HashSet<>();

        urls.forEach(_url -> {
            String url = _url.toString();

            for (String prefix : rootPrefixes) {
                if (url.startsWith(prefix)) {
                    try {
                        String p = url.substring(prefix.length(), url.length() - csLength);
                        String className = p.replace('/', '.');
                        Class<?> clz = Class.forName(className, false, classLoader);

                        if (predicate == null || predicate.test(clz))
                            result.add(clz);
                    } catch (Throwable e) {
                        System.err.printf("%s:%s\n", e.getClass().getSimpleName(), e.getMessage());
                    }
                }
            }
        });

        return result;
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;

        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }

        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ResourceScanner.class.getClassLoader();

            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }

        return cl;
    }
}

