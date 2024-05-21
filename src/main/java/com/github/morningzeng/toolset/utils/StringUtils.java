package com.github.morningzeng.toolset.utils;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public class StringUtils {

    public static String maskSensitive(final String str) {
        return str.replaceAll("(?<=.{3}).+(?=.{4})", "****");
    }

}
