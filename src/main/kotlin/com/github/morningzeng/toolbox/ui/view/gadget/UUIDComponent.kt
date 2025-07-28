package com.github.morningzeng.toolbox.ui.view.gadget

import com.github.morningzeng.toolbox.Constants
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import java.util.*

/**
 * @author Morning Zeng
 * @since 2025-07-25
 */
class UUIDComponent(project: Project) : JBPanel<JBPanelWithEmptyText>() {

    private val content = object : LanguageTextArea(project, "") {
        override fun defaultRightBarActions(): Array<AnAction> {
            return arrayOf(
                softWrapAction(),
                scrollToEndAction(),
                shortAction(),
                upperCaseAction {
                    render()
                    upperCase = it
                },
                generateAction(),
                clearAllAction()
            )
        }
    }.apply {
        readOnly = true
        setPlaceholder("You can click the Generate button above to get the UUID")
    }

    private var uuid: UUID? = null
    private var short: Boolean = false
    private var upperCase: Boolean = false

    init {
        GridBagUtils.builder(this).fill(GridBagUtils.GridBagFill.BOTH)
            .row { it.cell().weightX(1.0).weightY(1.0).add(content.withRightBar()) }
    }

    fun shortAction(): ToggleAction {
        return object : ToggleAction("Short", "Short UUID", Constants.IconC.DISABLE_MINUS) {
            override fun getActionUpdateThread(): ActionUpdateThread {
                return super.getActionUpdateThread()
            }

            override fun isSelected(e: AnActionEvent): Boolean = short

            override fun setSelected(e: AnActionEvent, state: Boolean) {
                short = state
                render()
            }
        }
    }

    fun generateAction(): AnAction {
        return object : AnAction("Generate", "Generate UUID", Constants.IconC.AUTORENEW) {
            override fun actionPerformed(e: AnActionEvent) {
                uuid = UUID.randomUUID()
                render()
            }
        }
    }

    fun render() {
        uuid?.let {
            var text = it.toString()
            if (short) {
                text = text.replace("-", "")
            }
            if (upperCase) {
                text = text.uppercase()
            }
            content.text = text
        }
    }

}