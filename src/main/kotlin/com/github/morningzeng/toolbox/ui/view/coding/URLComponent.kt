package com.github.morningzeng.toolbox.ui.view.coding

import com.intellij.openapi.project.Project
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * @author Morning Zeng
 * @since 2025-07-22
 */
class URLComponent(
    project: Project
) : AbstractCodingComponent(project) {
    override fun encode(): String {
        return URLEncoder.encode(decodeArea.text, StandardCharsets.UTF_8)
    }

    override fun decode(): String {
        return URLDecoder.decode(encodeArea.text, StandardCharsets.UTF_8)
    }
}