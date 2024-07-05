package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.model.Children;
import com.google.common.collect.Lists;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-07-05
 */
public final class Tree<T extends Children<T>> extends SimpleTree {
    @Getter
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    private final DefaultTreeModel treeModel = new DefaultTreeModel(this.root);
    private final List<T> ts = Lists.newArrayList();

    {
        this.setModel(this.treeModel);
        this.setRootVisible(false);
    }

    public void setNodes(final Collection<T> ts) {
        this.root.removeAllChildren();
        this.ts.clear();
        this.addNodes(ts);
    }

    public void addNodes(final Collection<T> ts) {
        this.ts.addAll(ts);
        this.builderNode(ts, this.root);
    }

    public DefaultMutableTreeNode create(final T t, final boolean allowsChildren) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(t, allowsChildren);

        Optional.ofNullable(this.getLastSelectedPathComponent())
                .map(DefaultMutableTreeNode.class::cast)
                .ifPresentOrElse(n -> {
                    if (n.getAllowsChildren()) {
                        n.add(node);
                        final T selectedValue = this.getSelectedValue();
                        Optional.ofNullable(selectedValue).map(Children::getChildren).ifPresent(ts -> {
                            ts.add(t);
                            t.setParent(selectedValue);
                        });
                        this.treeModel.reload(n);
                        this.expandPath(new TreePath(node.getPath()));
                    } else {
                        final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) n.getParent();
                        parent.add(node);
                        final T pt = this.getNodeValue(parent);
                        Optional.ofNullable(pt).map(Children::getChildren).ifPresent(ts -> {
                            ts.add(t);
                            t.setParent(pt);
                        });
                        this.treeModel.reload(parent);
                        this.expandPath(new TreePath(parent.getPath()));
                    }
                }, () -> {
                    this.root.add(node);
                    this.ts.add(t);
                    this.reloadTree(null);
                });
        TreeUtil.selectNode(this, node);
        return node;
    }

    public void cleanUp(final Consumer<List<T>> flatConsumer) {
        this.ts.clear();
        final List<T> flatTree = this.flatTree(this.ts);
        flatConsumer.accept(flatTree);
    }

    public void delete(final Consumer<List<DefaultMutableTreeNode>> consumer) {
        Optional.ofNullable(this.getSelectionPaths())
                .ifPresent(treePaths -> {
                    final List<DefaultMutableTreeNode> treeNodes = Lists.newArrayList();
                    for (final TreePath path : treePaths) {
                        final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                        this.treeModel.removeNodeFromParent(treeNode);
                        treeNodes.add(treeNode);
                        final T t = this.getNodeValue(treeNode);
                        Optional.ofNullable(t.getParent()).ifPresent(parent -> parent.getChildren().remove(t));
                    }
                    this.reloadTree(null);
                    consumer.accept(treeNodes);
                });
    }

    public void reloadTree(final TreeNode treeNode) {
        Optional.ofNullable(treeNode)
                .ifPresentOrElse(tn -> this.treeModel.reload(treeNode), () -> {
                    final Enumeration<TreePath> expandedPaths = this.getExpandedDescendants(new TreePath(this.treeModel.getRoot()));
                    this.treeModel.reload();
                    Optional.ofNullable(expandedPaths)
                            .ifPresent(treePathEnumeration -> {
                                while (treePathEnumeration.hasMoreElements()) {
                                    this.expandPath(treePathEnumeration.nextElement());
                                }
                            });
                });
    }

    public int childrenCount() {
        return Optional.ofNullable(this.getSelectedValue())
                .map(Children::getChildren)
                .map(List::size)
                .orElse(this.ts.size());
    }

    public T getSelectedValue() {
        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
        if (Objects.isNull(selectedNode)) {
            return null;
        }
        //noinspection unchecked
        return (T) selectedNode.getUserObject();
    }

    void builderNode(final Collection<T> ts, final DefaultMutableTreeNode parent) {
        if (Objects.isNull(ts) || ts.isEmpty()) {
            return;
        }
        final T pt = this.getNodeValue(parent);
        for (final T t : ts) {
            final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(t);
            parent.add(treeNode);
            Optional.ofNullable(pt).ifPresent(t::setParent);
            final List<T> children = t.getChildren();
            this.builderNode(children, treeNode);
        }
    }

    List<T> flatTree(final Collection<T> tree) {
        return tree.stream()
                .<T>mapMulti((t, consumer) -> {
                    consumer.accept(t);
                    final List<T> children = t.getChildren();
                    if (!CollectionUtils.isEmpty(children)) {
                        flatTree(children).forEach(consumer);
                    }
                })
                .toList();
    }

    private T getNodeValue(final DefaultMutableTreeNode treeNode) {
        //noinspection unchecked
        return (T) treeNode.getUserObject();
    }

}
