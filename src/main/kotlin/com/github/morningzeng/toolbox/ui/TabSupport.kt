package com.github.morningzeng.toolbox.ui

import com.intellij.openapi.wm.ToolWindow
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JTabbedPane

/**
 * @author Morning Zeng
 * @since 2025-05-15
 */
interface TabSupport {

    val title: String

    val icon: Icon?

    val tips: String

    fun component(toolWindow: ToolWindow): JComponent


    fun putTab(tabbedPane: JTabbedPane, toolWindow: ToolWindow) {
        tabbedPane.addTab(this.title, this.icon, this.component(toolWindow), this.tips)
    }
}