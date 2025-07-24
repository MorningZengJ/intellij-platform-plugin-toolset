package com.github.morningzeng.toolbox.ui.dialog

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.model.CryptoHash
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.github.morningzeng.toolbox.utils.ExpandMethodUtils.labelWidth
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout

/**
 * @author Morning Zeng
 * @since 2025-05-22
 */
class PropertyHashDialog(
    project: Project,
    okAfter: (ts: MutableList<CryptoHash>) -> Unit = {},
    selectedAfter: (CryptoHash) -> Unit = {}
) : AbstractPropDialog<CryptoHash, PropertyHashDialog.RightPanel>(project, okAfter, selectedAfter) {

    init {
        init()
        title = "Hash Properties"
    }

    override fun typeReference(): TypeReference<MutableList<CryptoHash>> =
        object : TypeReference<MutableList<CryptoHash>>() {}

    override fun generateBean(
        name: String,
        isGroup: Boolean
    ): CryptoHash = CryptoHash(name, directory = isGroup)

    override fun writeProp(
        prop: CryptoHash,
        rightPanel: RightPanel
    ) {
        rightPanel.apply {
            prop.title = titleTextField.component.text
            prop.key = keyTextField.component.text
            prop.description = descTextArea.component.text
        }
    }

    override fun createRightItemPanel(t: CryptoHash): RightPanel = RightPanel(project, t)

    class RightPanel(
        val project: Project,
        prop: CryptoHash,
    ) : AbstractRightPanel<CryptoHash>(prop) {

        val keyTextField = LabeledComponent.create<JBTextField>(
            JBTextField(prop.key), "Key", BorderLayout.WEST
        ).apply { labelWidth(labelWidth) }
        val descTextArea = LabeledComponent.create<LanguageTextArea>(
            LanguageTextArea(project, prop.description), "Desc", BorderLayout.WEST
        ).apply { labelWidth(labelWidth) }

        init {
            super.initLayout()
        }


        override fun itemLayout(): (GridBagUtils.GridBagBuilder<AbstractRightPanel<CryptoHash>>) -> Unit {
            return { builder ->
                builder.row {
                    it.fill(GridBagUtils.GridBagFill.HORIZONTAL)
                        .cell().weightX(1.0).add(titleTextField)
                }
                    .row { it.cell().add(keyTextField) }
                    .row { it.fill(GridBagUtils.GridBagFill.BOTH).cell().weightY(1.0).add(descTextArea) }
            }
        }


    }

}