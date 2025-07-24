package com.github.morningzeng.toolbox.ui.dialog

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.enums.DataToBinaryTypeEnum
import com.github.morningzeng.toolbox.model.CryptoSymmetric
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.github.morningzeng.toolbox.utils.ExpandMethodUtils.labelWidth
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout

/**
 * @author Morning Zeng
 * @since 2025-05-20
 */
class PropertySymmetricDialog(
    project: Project,
    okAfter: (ts: MutableList<CryptoSymmetric>) -> Unit = {},
    selectedAfter: (CryptoSymmetric) -> Unit
) : AbstractPropDialog<CryptoSymmetric, PropertySymmetricDialog.RightPanel>(project, okAfter, selectedAfter) {

    init {
        init()
        title = "Symmetric Properties"
    }

    override fun typeReference(): TypeReference<MutableList<CryptoSymmetric>> {
        return object : TypeReference<MutableList<CryptoSymmetric>>() {
        }
    }

    override fun generateBean(
        name: String,
        isGroup: Boolean
    ): CryptoSymmetric = CryptoSymmetric(name, directory = isGroup)

    override fun writeProp(
        prop: CryptoSymmetric,
        rightPanel: RightPanel
    ) {
        rightPanel.apply {
            prop.title = titleTextField.component.text
            prop.key = keyTextField.component.text
            prop.keyType = keyTypeCombo.item
            prop.iv = ivTextField.component.text
            prop.ivType = ivTypeCombo.item
            prop.description = descriptionTextField.component.text
        }
    }

    override fun createRightItemPanel(t: CryptoSymmetric): RightPanel = RightPanel(project, t)

    class RightPanel(
        val project: Project,
        prop: CryptoSymmetric,
    ) : AbstractRightPanel<CryptoSymmetric>(prop) {

        val keyTextField = LabeledComponent.create<JBTextField>(
            JBTextField(prop.key), "Key", BorderLayout.WEST
        ).apply { labelWidth(labelWidth) }
        val keyTypeCombo = ComboBox(DataToBinaryTypeEnum.entries.toTypedArray()).apply {
            selectedItem = prop.keyType
        }
        val ivTextField = LabeledComponent.create<JBTextField>(
            JBTextField(prop.iv), "IV", BorderLayout.WEST
        ).apply { labelWidth(labelWidth) }
        val ivTypeCombo = ComboBox(DataToBinaryTypeEnum.entries.toTypedArray()).apply {
            selectedItem = prop.ivType
        }
        val descriptionTextField = LabeledComponent.create<LanguageTextArea>(
            LanguageTextArea(project, prop.description), "Desc", BorderLayout.WEST
        ).apply { labelWidth(labelWidth) }

        init {
            super.initLayout()
        }


        override fun itemLayout(): (GridBagUtils.GridBagBuilder<AbstractRightPanel<CryptoSymmetric>>) -> Unit {
            return {
                it.row { row ->
                    row.fill(GridBagUtils.GridBagFill.HORIZONTAL)
                        .cell().weightX(1.0).gridWidth(2).add(titleTextField)
                }
                    .row { row ->
                        row.cell().add(keyTextField)
                            .cell().weightX(0.0).add(keyTypeCombo)
                    }
                    .row { row ->
                        row.cell().weightX(1.0).add(ivTextField)
                            .cell().weightX(0.0).add(ivTypeCombo)
                    }
                    .row { row ->
                        row.fill(GridBagUtils.GridBagFill.BOTH)
                            .cell().weightY(1.0).gridWidth(2).add(descriptionTextField)
                    }
            }
        }

    }

}