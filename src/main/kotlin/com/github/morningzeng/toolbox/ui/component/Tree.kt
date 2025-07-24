@file:Suppress("unused")

package com.github.morningzeng.toolbox.ui.component

import com.github.morningzeng.toolbox.model.Children
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.util.ui.tree.TreeUtil
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
class Tree<T : Children<T>> : SimpleTree() {

    val root: DefaultMutableTreeNode = DefaultMutableTreeNode()
    private val treeModelT: DefaultTreeModel = DefaultTreeModel(root)
    private var ts: MutableList<T> = mutableListOf()

    init {
        model = treeModelT
        this.setRootVisible(false)
    }

    fun setNodes(ts: MutableList<T>, allowChildren: (T) -> Boolean) {
        root.removeAllChildren()
        this.ts.clear()
        addNodes(ts, allowChildren)
    }

    fun addNodes(ts: MutableList<T>, allowChildren: (T) -> Boolean) {
        if (!ts.isEmpty()) {
            this.ts.addAll(ts)
            buildNode(ts, root, allowChildren)
        }
        reloadTree(null)
    }

    fun create(t: T, allowsChildren: Boolean): DefaultMutableTreeNode {
        val treeNode = DefaultMutableTreeNode(t, allowsChildren)
        val selectedNode = lastSelectedPathComponent?.let { it as DefaultMutableTreeNode }
        if (selectedNode != null && root != selectedNode) {
            selectedNode.also {
                if (it.allowsChildren) {
                    it.add(treeNode)
                    getSelectedValue()?.also { sv -> sv.addChild(t) }
                    treeModelT.reload(it)
                    expandPath(TreePath(treeNode.path))
                } else {
                    val parentNode = it.parent as DefaultMutableTreeNode
                    parentNode.add(treeNode)
                    getNodeValue(parentNode)?.also { sv -> sv.addChild(t) }
                    treeModelT.reload(parentNode)
                    expandPath(TreePath(parentNode.path))
                    if (root == parentNode) ts.add(t)
                }
            }
        } else {
            root.add(treeNode)
            ts.add(t)
            reloadTree(null)
        }
        TreeUtil.selectNode(this, treeNode)
        return treeNode
    }

    fun clear() {
        ts.clear()
        root.removeAllChildren()
    }

    override fun isSelectionEmpty(): Boolean = getSelectedValue() == null

    fun delete(consumer: (MutableList<DefaultMutableTreeNode>) -> Unit) {
        selectionPaths?.let {
            var next: DefaultMutableTreeNode? = null
            val treeNodes: MutableList<DefaultMutableTreeNode> = mutableListOf()
            for (path in it) {
                val treeNode = path.lastPathComponent as DefaultMutableTreeNode
                if (Objects.isNull(next) || next == treeNode) {
                    val parent = treeNode.getParent() as DefaultMutableTreeNode?
                    if (Objects.isNull(treeNode.getNextSibling().also { next = it })) {
                        next = parent
                    }
                }
                this.treeModelT.removeNodeFromParent(treeNode)
                treeNodes.add(treeNode)
                val t = this.getNodeValue(treeNode)

                if (Objects.isNull(t?.parent)) {
                    this.ts.remove(t)
                }
                t?.parent?.children?.remove(t)
            }
            this.reloadTree(null)
            consumer(treeNodes)
            this.selectionPath = TreePath(this.treeModelT.getPathToRoot(next))
        }
    }

    fun reloadTree(treeNode: TreeNode?) {
        if (treeNode != null) treeModelT.reload(treeNode) else {
            val expandedNodes = getExpandedDescendants(TreePath(treeModelT.root))
            treeModelT.reload()
            expandedNodes?.also {
                while (it.hasMoreElements()) {
                    expandPath(it.nextElement())
                }
            }
        }
    }

    fun childrenCount(): Int {
        return getSelectedValue()?.children?.size ?: ts.size
    }

    fun getSelectedValue(): T? {
        return this.lastSelectedPathComponent?.let { it as DefaultMutableTreeNode }
            ?.let { getNodeValue(it) }
    }

    fun clearSelectionIfClickedOutside() {
        this.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                if (this@Tree.getRowForLocation(50, e!!.getY()) == -1) {
                    this@Tree.clearSelection()
                }
            }
        })
    }

    fun data(): MutableList<T> = ts

    fun cellRenderer(renderer: (T) -> JComponent) {
        this.setCellRenderer { tree: JTree?, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean ->
            value?.let { it as DefaultMutableTreeNode }
                ?.let { getNodeValue(it) }
                ?.let { renderer(it) }
                .also { it?.isOpaque = selected }
        }
    }

    fun buildNode(ts: MutableList<T>, parentNode: DefaultMutableTreeNode, allowChildren: (T) -> Boolean) {
        if (ts.isEmpty()) return
        val parent = getNodeValue(parentNode)
        ts.forEach {
            val allow = allowChildren(it)
            val treeNode = DefaultMutableTreeNode(it, allow)
            parentNode.add(treeNode)
            parent?.also { p -> it.parent = p }
            if (allow) {
                buildNode(it.children, treeNode, allowChildren)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getNodeValue(treeNode: DefaultMutableTreeNode): T? = treeNode.userObject?.let { it as T }

}