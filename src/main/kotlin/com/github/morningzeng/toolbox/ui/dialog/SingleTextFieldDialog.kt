package com.github.morningzeng.toolbox.ui.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBTextField
import javax.swing.BoxLayout
import javax.swing.JComponent

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
class SingleTextFieldDialog(
    text: String,
    private val label: String,
    private val okAction: (text: String) -> Unit
) : DialogWrapper(true) {

    private val textField: JBTextField = JBTextField()

    init {
        init()
        title = text
    }

    override fun createCenterPanel(): JComponent {
        return JBPanel<JBPanelWithEmptyText>().apply {
            layout = BoxLayout(this, BoxLayout.LINE_AXIS)
            add(JBLabel(label))
            add(textField)
        }
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return textField
    }

    override fun doOKAction() {
        okAction.invoke(textField.text)
        super.doOKAction()
    }
}