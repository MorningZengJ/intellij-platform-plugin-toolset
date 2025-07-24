package com.github.morningzeng.toolbox.ui.action

import com.github.morningzeng.toolbox.ui.dialog.SingleTextFieldDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import javax.swing.Icon

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
open class SingleTextFieldDialogAction(
    val title: String,
    val clickOk: (text: String) -> Unit,
    text: String? = null,
    icon: Icon? = null,
    description: String? = null,
) : AnAction(text, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        SingleTextFieldDialog(title, "Name", clickOk).showAndGet()
    }
}