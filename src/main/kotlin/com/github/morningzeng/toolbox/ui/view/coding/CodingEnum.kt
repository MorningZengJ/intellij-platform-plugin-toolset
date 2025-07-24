package com.github.morningzeng.toolbox.ui.view.coding

import com.github.morningzeng.toolbox.ui.TabSupport
import com.intellij.openapi.wm.ToolWindow
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author Morning Zeng
 * @since 2025-07-22
 */
enum class CodingEnum(
    override val title: String,
    override val icon: Icon? = null,
    override val tips: String
) : TabSupport {

    BASE64("Base64", null, "Base64 Encode & Decode") {
        override fun component(toolWindow: ToolWindow): JComponent = Base64Component(toolWindow.project)
    },
    URL("URL", null, "URL Encode & Decode") {
        override fun component(toolWindow: ToolWindow): JComponent = URLComponent(toolWindow.project)
    },

    ;

}