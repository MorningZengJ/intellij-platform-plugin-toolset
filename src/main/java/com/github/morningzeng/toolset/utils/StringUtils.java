package com.github.morningzeng.toolset.utils;

import com.intellij.openapi.util.text.StringUtil;

import java.util.List;

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

    public static boolean isPublicKey(final String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException();
        }
        final List<String> lines = str.lines().toList();
        String firstLine = lines.get(0);
        String lastLine = lines.get(lines.size() - 1);

        if (firstLine.matches("-----BEGIN (.+?)?PUBLIC KEY-----") && lastLine.matches("-----END (.+?)?PUBLIC KEY-----")) {
            return true;
        }
        if (firstLine.matches("-----BEGIN (.+?)?PRIVATE KEY-----") && lastLine.matches("-----END (.+?)?PRIVATE KEY-----")) {
            return false;
        }
        throw new IllegalArgumentException();
    }

}
