package com.github.morningzeng.toolbox.ui.view.coding

import com.github.morningzeng.toolbox.Constants
import com.github.morningzeng.toolbox.ui.action.HistoryAction
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import javax.swing.JButton

/**
 * @author Morning Zeng
 * @since 2025-07-22
 */
abstract class AbstractCodingComponent(
    protected val project: Project
) : JBPanel<JBPanelWithEmptyText>() {

    private val encHistoryAction: HistoryAction<String> = HistoryAction()
    private val decHistoryAction: HistoryAction<String> = HistoryAction()
    val encodeArea: LanguageTextArea = textArea(encHistoryAction)
    val decodeArea: LanguageTextArea = textArea(decHistoryAction)
    private val encodeBtn: JButton = JButton("Encode", Constants.IconC.DOUBLE_ANGLES_DOWN)
    private val decodeBtn: JButton = JButton("Decode", Constants.IconC.DOUBLE_ANGLES_UP)

    init {
        initLayout()
        initEvent()
    }

    fun initLayout() {
        GridBagUtils.builder(this)
            .fill(GridBagUtils.GridBagFill.BOTH)
            .row { it.cell().weightX(1.0).weightY(1.0).add(decodeArea.withRightBar()) }
            .row {
                val panel = GridBagUtils.builder().fill(GridBagUtils.GridBagFill.HORIZONTAL)
                    .row { cit ->
                        cit.cell().add(encodeBtn)
                            .cell().add(decodeBtn)
                    }
                    .build()
                it.fill(GridBagUtils.GridBagFill.HORIZONTAL).cell().weightY(0.0).add(panel)
            }
            .fill(GridBagUtils.GridBagFill.BOTH)
            .row { it.cell().weightY(1.0).add(encodeArea.withRightBar()) }
    }

    fun initEvent() {
        encodeBtn.addActionListener {
            try {
                encodeArea.text = encode()
                encHistoryAction.addItem(textConvert()(""))
                decHistoryAction.addItem(textConvert()(""))
            } catch (e: Exception) {
                Messages.showErrorDialog(project, e.message, "Encode Error")
            }
        }
        decodeBtn.addActionListener {
            try {
                decodeArea.text = decode()
                encHistoryAction.addItem(textConvert()(""))
                decHistoryAction.addItem(textConvert()(""))
            } catch (e: Exception) {
                Messages.showErrorDialog(project, e.message, "Decode Error")
            }
        }
    }

    fun textConvert(): (String) -> String {
        return { "${decodeArea.text} - ${encodeArea.text}" }
    }

    fun tConvert(): (String) -> String? {
        return {
            it.split(" - ").apply {
                decodeArea.text = this[0]
                encodeArea.text = this[1]
            }
            null
        }
    }

    abstract fun encode(): String

    abstract fun decode(): String

    private fun textArea(action: HistoryAction<String>): LanguageTextArea {
        return object : LanguageTextArea(project, "") {
            override fun defaultRightBarActions(): Array<AnAction> {
                return arrayOf(
                    softWrapAction(),
                    scrollToEndAction(),
                    action.also {
                        it.setTextArea(this)
                        it.textAreaEvent(textConvert(), tConvert())
                    },
                    clearAllAction()
                )
            }
        }
    }

}