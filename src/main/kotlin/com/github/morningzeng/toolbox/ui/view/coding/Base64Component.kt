package com.github.morningzeng.toolbox.ui.view.coding

import com.intellij.openapi.project.Project
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author Morning Zeng
 * @since 2025-07-22
 */
class Base64Component(
    project: Project
) : AbstractCodingComponent(project) {
    override fun encode(): String {
        return Base64.getEncoder().encodeToString(decodeArea.text.toByteArray(StandardCharsets.UTF_8))
    }

    override fun decode(): String {
        return String(Base64.getDecoder().decode(encodeArea.text), StandardCharsets.UTF_8)
    }
}