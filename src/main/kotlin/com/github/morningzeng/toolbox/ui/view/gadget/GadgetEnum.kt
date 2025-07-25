package com.github.morningzeng.toolbox.ui.view.gadget

import com.github.morningzeng.toolbox.ui.TabSupport
import com.intellij.openapi.wm.ToolWindow
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author Morning Zeng
 * @since 2025-07-23
 */
enum class GadgetEnum(
    override val title: String,
    override val icon: Icon? = null,
    override val tips: String,
) : TabSupport {

    TIMESTAMP("Timestamp", null, "Date & Time") {
        override fun component(toolWindow: ToolWindow): JComponent = TimestampComponent(toolWindow.project)
    },
    UUID("UUID", null, "UUID") {
        override fun component(toolWindow: ToolWindow): JComponent = UUIDComponent(toolWindow.project)
    },
    ;

}