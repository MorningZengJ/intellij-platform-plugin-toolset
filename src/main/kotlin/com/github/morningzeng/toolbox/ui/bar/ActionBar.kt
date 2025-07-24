package com.github.morningzeng.toolbox.ui.bar

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.SwingUtilities

/**
 * @author Morning Zeng
 * @since 2025-05-20
 */
class ActionBar(
    vararg val actions: AnAction,
    val horizontal: Boolean = true,
) : JBPanel<JBPanelWithEmptyText>() {

    init {
        if (horizontal) {
            maximumSize = Dimension(Int.MAX_VALUE, 20)
            border = JBUI.Borders.customLineBottom(JBColor.GRAY)
        } else {
            Dimension(30, preferredSize.height).apply {
                preferredSize = this
                minimumSize = this
            }
        }
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        ApplicationManager.getApplication().invokeAndWait {
            ActionManager.getInstance().createActionToolbar(
                ActionPlaces.POPUP,
                DefaultActionGroup(*actions),
                horizontal
            ).also {
                it.targetComponent = this
                SwingUtilities.invokeLater { add(it.component) }
            }
        }
    }

    override fun setSize(width: Int, height: Int) {
        Dimension(width, height).apply {
            maximumSize = this
            minimumSize = this
            preferredSize = this
        }
    }
}