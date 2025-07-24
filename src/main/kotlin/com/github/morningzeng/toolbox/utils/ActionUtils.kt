package com.github.morningzeng.toolbox.utils

import com.intellij.openapi.actionSystem.*
import javax.swing.Icon

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
object ActionUtils {

    fun drawerActions(text: String, description: String, icon: Icon, vararg actions: AnAction): AnAction {
        val group = DefaultActionGroup(text, description, icon).apply {
            addAll(*actions)
            isPopup = true
        }
        return object : AnAction(text, description, icon) {
            override fun actionPerformed(e: AnActionEvent) {
                val popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group)
                e.inputEvent?.component?.let { popupMenu.component.show(it, it.width, it.height) }
            }
        }
    }
}