package com.github.morningzeng.toolbox.ui.dialog

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.Constants
import com.github.morningzeng.toolbox.model.Children
import com.github.morningzeng.toolbox.ui.ScrollSupport
import com.github.morningzeng.toolbox.ui.action.SingleTextFieldDialogAction
import com.github.morningzeng.toolbox.ui.bar.ActionBar
import com.github.morningzeng.toolbox.ui.component.Tree
import com.github.morningzeng.toolbox.utils.ActionUtils
import com.github.morningzeng.toolbox.utils.ExpandMethodUtils.labelWidth
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.github.morningzeng.toolbox.utils.ScratchFileUtils
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBTextField
import fleet.util.computeIfAbsentShim
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.util.function.Predicate
import javax.swing.Action
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.tree.DefaultMutableTreeNode

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
abstract class AbstractPropDialog<T : Children<T>, P : AbstractPropDialog.AbstractRightPanel<T>>(
    protected val project: Project,
    protected val okAfter: (ts: MutableList<T>) -> Unit = {},
    protected val selectedAfter: (t: T) -> Unit = {}
) : DialogWrapper(project) {

    protected val tree: Tree<T> = Tree()

    private val emptyPanel = JBPanelWithEmptyText()
    private val pane = JBSplitter(false, "prop-dialog-splitter", .3f)
    private val rightPanelMap: MutableMap<T, P> = mutableMapOf()
    private val addActions: AnAction = ActionUtils.drawerActions(
        "Add Item", "New create crypto prop item", Constants.IconC.ADD_DRAWER, *initGroupAction()
    )
    private val actionBar = ActionBar(*barActions())

    init {
        tree.clearSelectionIfClickedOutside()
        val ts = ScratchFileUtils.read(typeReference())?.apply {
            sortWith(Children.comparable())
            forEach { it.children.sortWith(Children.comparable()) }
        } ?: mutableListOf()
        tree.setNodes(ts) { it.directory }
        tree.addTreeSelectionListener {
            tree.lastSelectedPathComponent?.let { it as DefaultMutableTreeNode }
                ?.also {
                    val value = tree.getNodeValue(it)
                    if (!enabledNode().test(value)) {
                        tree.clearSelection()
                        pane.secondComponent = emptyPanel
                        return@also
                    }
                }
            defaultRightPanel()
        }
        tree.cellRenderer {
            JBLabel(it.name(), it.icon(), SwingConstants.LEFT).apply {
                isEnabled = enabledNode().test(it)
            }
        }
        actionBar.layout = BoxLayout(actionBar, BoxLayout.LINE_AXIS)
    }

    open fun barActions(): Array<AnAction> {
        return arrayOf(addActions, deleteAction())
    }

    fun deleteAction(): AnAction {
        return object : AnAction(Constants.IconC.REMOVE_RED) {
            override fun actionPerformed(e: AnActionEvent) = delete()
        }
    }

    fun delete() {
        tree.delete {
            defaultRightPanel()
        }
    }

    open fun enabledNode(): Predicate<T?> = Predicate { true }

    override fun createCenterPanel(): JComponent {
        createLeftPanel()
        defaultRightPanel()
        pane.minimumSize = Dimension(700, 500)
        pane.dividerWidth = 3
        return pane
    }

    override fun doOKAction() {
        super.doOKAction()
        applyProp()
        tree.getSelectedValue()?.let { selectedAfter(it) }
    }

    override fun createActions(): Array<out Action?> {
        return arrayOf(
            object : DialogWrapperAction("Apply") {
                override fun doAction(e: ActionEvent?) {
                    applyProp()
                    tree.getSelectedValue()?.let { selectedAfter(it) }
                }
            },
            *super.createActions()
        )
    }

    private fun applyProp() {
        rightPanelMap.forEach { (t, p) -> writeProp(t, p) }
        tree.lastSelectedPathComponent?.let { it as DefaultMutableTreeNode }
            ?.also { tree.reloadTree(it) }
        ScratchFileUtils.write(tree.data(), typeReference())
        okAfter(tree.data())
        tree.getSelectedValue()?.let { selectedAfter(it) }
    }

    fun initGroupAction(): Array<AnAction> {
        return arrayOf(
            object : SingleTextFieldDialogAction(
                "Group", { name -> this.tree.create(this.generateBean(name, true), true) }, "Add Group"
            ) {
                override fun getActionUpdateThread(): ActionUpdateThread {
                    return super.getActionUpdateThread()
                }

                override fun update(e: AnActionEvent) {
                    e.presentation.isEnabled = tree.isSelectionEmpty
                }
            },
            object : AnAction("KeyPair") {
                override fun actionPerformed(e: AnActionEvent) {
                    val cryptoProp: T = generateBean("", false)
                    tree.create(cryptoProp, true)
                    defaultRightPanel()
                }
            }
        )
    }

    abstract fun typeReference(): TypeReference<MutableList<T>>

    abstract fun generateBean(name: String, isGroup: Boolean): T

    abstract fun writeProp(prop: T, rightPanel: P)

    abstract fun createRightItemPanel(t: T): P

    fun defaultRightPanel() {
        val t = tree.getSelectedValue()
        if (t == null) {
            pane.secondComponent = emptyPanel
            return
        }
        rightPanelMap.computeIfAbsentShim(t) { createRightItemPanel(t) }.also {
            pane.secondComponent = it
        }
    }

    fun createLeftPanel() {
        GridBagUtils.builder()
            .fill(GridBagUtils.GridBagFill.HORIZONTAL)
            .row { it.cell().weightX(1.0).add(actionBar) }
            .row {
                it.fill(GridBagUtils.GridBagFill.BOTH).cell().weightY(1.0).add(
                    ScrollSupport.getInstance(tree).verticalAsNeededScrollPane()
                )
            }
            .build()
            .apply { pane.firstComponent = this }
    }

    abstract class AbstractRightPanel<T : Children<T>>(
        protected val prop: T,
    ) : JBPanel<JBPanelWithEmptyText>() {

        private val emptyPanel = JBPanelWithEmptyText()
        protected val labelWidth = 50
        val titleTextField = LabeledComponent.create<JBTextField>(
            JBTextField(prop.name()), "Name", BorderLayout.WEST
        ).apply { labelWidth(labelWidth) }

        protected fun initLayout() {
            GridBagUtils.builder(this).apply {
                if (prop.directory) {
                    row {
                        it.fill(GridBagUtils.GridBagFill.HORIZONTAL)
                            .cell().weightX(1.0).add(titleTextField)
                    }.row {
                        it.fill(GridBagUtils.GridBagFill.BOTH)
                            .cell().weightX(1.0).weightY(1.0).add(emptyPanel)
                    }
                } else itemLayout()(this)
            }
        }

        protected abstract fun itemLayout(): (GridBagUtils.GridBagBuilder<AbstractRightPanel<T>>) -> Unit
    }

}