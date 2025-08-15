package com.github.morningzeng.toolbox.utils

import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.intellij.json.json5.Json5Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.fileTypes.PlainTextLanguage

/**
 * @author Morning Zeng
 * @since 2025-08-15
 */
object LanguageUtils {

    fun LanguageTextArea.detectLanguage() {
        this.language = when {
            isJsonContent(this.text) -> Json5Language.INSTANCE
            isXmlContent(this.text) -> XMLLanguage.INSTANCE
            isHtmlContent(this.text) -> HTMLLanguage.INSTANCE
            else -> PlainTextLanguage.INSTANCE
        }
    }

    private fun isJsonContent(content: String): Boolean {
        val trimmed = content.trim()
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"))
    }

    private fun isXmlContent(content: String): Boolean {
        return content.contains("<?xml") ||
                (content.contains("<") && content.contains(">") && content.contains("</"))
    }

    private fun isHtmlContent(content: String): Boolean {
        val htmlTags = arrayOf(
            "<!DOCTYPE", "<html", "<head", "<body", "<div", "<span", "<p>", "<h1", "<h2"
        )
        val lowerContent = content.lowercase()
        return htmlTags.any { lowerContent.contains(it.lowercase()) }
    }


}