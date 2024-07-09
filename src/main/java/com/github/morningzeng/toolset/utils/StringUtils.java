package com.github.morningzeng.toolset.utils;

import com.intellij.openapi.util.text.StringUtil;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public class StringUtils {

    public static String maskSensitive(final String str) {
        if (StringUtil.isEmpty(str)) {
            return str;
        }
        return str.replaceAll("(?<=.{3}).+(?=.{4})", "****");
    }

}
