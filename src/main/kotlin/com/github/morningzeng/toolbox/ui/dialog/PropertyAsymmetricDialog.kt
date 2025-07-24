package com.github.morningzeng.toolbox.ui.dialog

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.Constants
import com.github.morningzeng.toolbox.enums.CryptoAsymmetricEnum
import com.github.morningzeng.toolbox.model.CryptoAsymmetric
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.github.morningzeng.toolbox.utils.GridBagUtils.GridBagFill
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.tree.TreeUtil
import java.awt.BorderLayout
import java.util.function.Predicate
import javax.swing.ListCellRenderer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

/**
 * @author Morning Zeng
 * @since 2025-05-23
 */

class PropertyAsymmetricDialog(
    val crypto: CryptoAsymmetricEnum,
    project: Project,
    okAfter: (ts: MutableList<CryptoAsymmetric>) -> Unit,
    selectedAfter: (CryptoAsymmetric) -> Unit
) : AbstractPropDialog<CryptoAsymmetric, PropertyAsymmetricDialog.RightPanel>(project, okAfter, selectedAfter) {

    init {
        init()
        title = "Asymmetric Properties"
    }

    override fun barActions(): Array<AnAction> {
        return arrayOf(*super.barActions(), generatorBtn())
    }

    override fun enabledNode(): Predicate<CryptoAsymmetric?> = Predicate { it?.crypto == crypto }

    override fun typeReference(): TypeReference<MutableList<CryptoAsymmetric>> {
        return object : TypeReference<MutableList<CryptoAsymmetric>>() {}
    }

    override fun generateBean(
        name: String,
        isGroup: Boolean
    ): CryptoAsymmetric {
        return CryptoAsymmetric(name, crypto, isGroup)
    }

    override fun writeProp(
        prop: CryptoAsymmetric,
        rightPanel: RightPanel
    ) {
        rightPanel.apply {
            prop.title = titleTextField.component.text
            prop.key = keyTextArea.component.text
            prop.isPublicKey = keyTypeCombo.item == CryptoAsymmetricEnum.Type.PUBLIC_KEY
            prop.description = descTextArea.component.text
        }
    }

    override fun createRightItemPanel(t: CryptoAsymmetric): RightPanel = RightPanel(project, t)

    fun generatorBtn(): AnAction {
        return object : AnAction("Generate", "Generate key pair", Constants.IconC.GENERATE) {
            override fun actionPerformed(e: AnActionEvent) {
                val group = "${crypto.name} (Generate)"
                var groupNode = tree.root.children().toList()
                    .filter { it is DefaultMutableTreeNode }
                    .map { it as DefaultMutableTreeNode }
                    .firstOrNull { group == tree.getNodeValue(it)?.title }
                if (groupNode == null) {
                    tree.clearSelection()
                    groupNode = tree.create(generateBean(group, true), true)
                }
                groupNode.apply {
                    TreeUtil.selectNode(tree, this)
                    val pair = crypto.genKey()
                    val description = "Plugin generates ${crypto.name}"
                    generateBean("PublicKey", false).let {
                        it.isPublicKey = true
                        it.key = pair.first
                        it.crypto = crypto
                        it.description = description
                        tree.create(it, false)
                    }
                    tree.selectionPath = TreePath(this)
                    generateBean("PrivateKey", false).let {
                        it.isPublicKey = false
                        it.key = pair.second
                        it.crypto = crypto
                        it.description = description
                        tree.create(it, false)
                    }
                }
            }
        }
    }

    class RightPanel(
        project: Project,
        prop: CryptoAsymmetric
    ) : AbstractRightPanel<CryptoAsymmetric>(prop) {

        val keyTypeCombo: ComboBox<CryptoAsymmetricEnum.Type> =
            ComboBox(CryptoAsymmetricEnum.Type.entries.toTypedArray()).apply {
                selectedItem =
                    if (prop.isPublicKey) CryptoAsymmetricEnum.Type.PUBLIC_KEY else CryptoAsymmetricEnum.Type.PRIVATE_KEY
                this.renderer = ListCellRenderer { list, value, index, isSelected, cellHasFocus ->
                    JBLabel(value.alias)
                }
            }
        val keyTextArea = LabeledComponent.create<LanguageTextArea>(
            LanguageTextArea(project, prop.key), "Key", BorderLayout.WEST
        )
        val descTextArea = LabeledComponent.create<LanguageTextArea>(
            LanguageTextArea(project, prop.description), "Desc", BorderLayout.WEST
        )

        init {
            initLayout()
        }

        override fun itemLayout(): (GridBagUtils.GridBagBuilder<AbstractRightPanel<CryptoAsymmetric>>) -> Unit {
            return { builder ->
                builder.row {
                    it.fill(GridBagFill.HORIZONTAL)
                        .cell().weightX(1.0).add(titleTextField)
                        .cell().weightX(0.0).add(keyTypeCombo)
                }
                    .row { it.fill(GridBagFill.BOTH).cell().weightX(1.0).weightY(1.0).gridWidth(2).add(keyTextArea) }
                    .row { it.cell().gridWidth(2).add(descTextArea) }
            }
        }
    }
}