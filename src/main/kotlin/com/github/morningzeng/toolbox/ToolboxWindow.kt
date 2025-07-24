package com.github.morningzeng.toolbox

import com.github.morningzeng.toolbox.ui.TabEnum
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.content.AlertIcon
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener

/**
 * @author Morning Zeng
 * @since 2025-05-15
 */
class ToolboxWindow : ToolWindowFactory {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val instance = ContentFactory.getInstance()
        val placeholderPanel = JBPanelWithEmptyText()
        TabEnum.entries.filter { it.load }.forEach {
            try {
                val content = instance.createContent(
                    it.component(toolWindow), it.title, false
                )
                content.icon = it.icon
                content.alertIcon = AlertIcon(it.icon)
                content.popupIcon = it.icon

                toolWindow.contentManager.addContent(content)
            } catch (e: Exception) {
                Notification(
                    "remind-notify",
                    "Toolbox initialize ${it.title} failed",
                    e.message ?: "",
                    NotificationType.ERROR
                ).notify(project)
                e.printStackTrace()
            }
        }

        val map = TabEnum.entries.associateBy { it.title }
        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun selectionChanged(event: ContentManagerEvent) {
                val content = event.content
                if (content != placeholderPanel) return
                try {
                    val selectedTab = map.getValue(content.tabName)
                    selectedTab.component(toolWindow)
                } catch (e: Exception) {
                    Notification(
                        "remind-notify",
                        "Toolbox initialize ${content.tabName} failed",
                        e.message ?: "",
                        NotificationType.ERROR
                    ).notify(project)
                    e.printStackTrace()
                }
            }
        })
    }
}