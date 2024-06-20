package com.github.morningzeng.toolset.utils;

import com.intellij.json.json5.Json5Language;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.PlainTextLanguage;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Morning Zeng
 * @since 2024-06-20
 */
public final class LanguageUtils {

    public static Language tryResolve(String content) {
        content = content.trim();
        final List<String> list = content.lines().toList();
        if (list.isEmpty()) {
            return PlainTextLanguage.INSTANCE;
        }
        final String neckLine = list.get(0);
        final String shadow = list.get(list.size() - 1);
        if (neckLine.startsWith("<html>") && shadow.endsWith("</html>")) {
            return HTMLLanguage.INSTANCE;
        }
        if ((neckLine.startsWith("{") && shadow.endsWith("}"))
                || (neckLine.startsWith("[") && shadow.endsWith("]"))) {
            return Json5Language.INSTANCE;
        }
        final Pattern startXml = Pattern.compile("^(?<=<).+?(?=>)");
        final Pattern endXml = Pattern.compile("(?<=</).+?(?=>)$");
        final Matcher startXmlMatcher = startXml.matcher(neckLine);
        final Matcher endXmlMatcher = endXml.matcher(shadow);
        if (startXmlMatcher.find() && endXmlMatcher.find()) {
            if (Objects.equals(startXmlMatcher.group(), endXmlMatcher.group())) {
                return XMLLanguage.INSTANCE;
            }
        }
        return PlainTextLanguage.INSTANCE;
    }

}
