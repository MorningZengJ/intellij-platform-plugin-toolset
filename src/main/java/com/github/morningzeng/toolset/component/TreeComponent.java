package com.github.morningzeng.toolset.component;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.Getter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-05-15
 */
public final class TreeComponent extends SimpleTree {
    @Getter
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    private final DefaultTreeModel treeModel = new DefaultTreeModel(this.root);

    {
        this.setModel(this.treeModel);
        this.setRootVisible(false);
    }

    @SafeVarargs
    public final <T> void setNodes(final Collection<T> data, final Function<T, ?>... functions) {
        if (Objects.isNull(data) || data.isEmpty()) {
            return;
        }
        if (Objects.isNull(functions) || functions.length == 0) {
            data.forEach(t -> {
                final DefaultMutableTreeNode node = new DefaultMutableTreeNode(t);
                this.root.add(node);
                node.setAllowsChildren(false);
            });
            return;
        }

        final Map<Integer, Map<Object, DefaultMutableTreeNode>> deepMap = Maps.newHashMap();
        Function<T, ?> parent = null;
        for (int deep = 1; deep < functions.length + 1; deep++) {
            final Function<T, ?> function = functions[deep - 1];
            for (final T datum : data) {
                if (Objects.isNull(function)) {
                    continue;
                }
                final Object o = function.apply(datum);
                if (o instanceof Collection<?> os) {
                    for (final Object object : os) {
                        buildNode(functions, datum, deepMap, deep, object, parent);
                    }
                    continue;
                }
                buildNode(functions, datum, deepMap, deep, o, parent);
            }
            parent = function;
        }
        this.reloadTree();
    }

    public Set<DefaultMutableTreeNode> leafNodes() {
        final Enumeration<TreeNode> enumeration = root.breadthFirstEnumeration();
        return Sets.newHashSet(enumeration.asIterator()).stream()
                .filter(Predicate.not(TreeNode::getAllowsChildren))
                .map(treeNode -> (DefaultMutableTreeNode) treeNode)
                .collect(Collectors.toUnmodifiableSet());
    }

    public DefaultMutableTreeNode createNodeOnSelectNode(final Object o, final boolean allowsChildren) {
        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
        if (Objects.isNull(selectedNode)) {
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(o, allowsChildren);
            this.root.add(node);
            this.reloadTree();
            TreeUtil.selectNode(this, node);
            return node;
        }
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(o, allowsChildren);
        selectedNode.add(node);
        this.treeModel.reload(selectedNode);
        this.expandPath(new TreePath(node.getPath()));
        TreeUtil.selectNode(this, node);
        return node;
    }

    public void delete(final Consumer<TreePath[]> consumer) {
        final TreePath[] paths = this.getSelectionPaths();
        if (Objects.isNull(paths)) {
            return;
        }
        for (final TreePath path : paths) {
            this.treeModel.removeNodeFromParent((DefaultMutableTreeNode) path.getLastPathComponent());
        }
        this.reloadTree();
        consumer.accept(paths);
    }

    public void reloadTree() {
        this.reloadTree(null);
    }

    public void reloadTree(final TreeNode treeNode) {
        if (Objects.nonNull(treeNode)) {
            this.treeModel.reload(treeNode);
        }
        final Enumeration<TreePath> expandedPaths = this.getExpandedDescendants(new TreePath(this.treeModel.getRoot()));
        this.treeModel.reload();
        Optional.ofNullable(expandedPaths)
                .ifPresent(treePathEnumeration -> {
                    while (treePathEnumeration.hasMoreElements()) {
                        this.expandPath(treePathEnumeration.nextElement());
                    }
                });
    }

    <T> void buildNode(final Function<T, ?>[] functions, final T datum, final Map<Integer, Map<Object, DefaultMutableTreeNode>> deepMap, final int deep, final Object o, final Function<T, ?> parent) {
        final DefaultMutableTreeNode node = deepMap.computeIfAbsent(deep, dp -> Maps.newHashMap()).computeIfAbsent(o, DefaultMutableTreeNode::new);
        final int parentDeep = deep - 1;
        final DefaultMutableTreeNode parentNode = Optional.ofNullable(parent)
                .map(func -> deepMap.get(parentDeep).get(func.apply(datum)))
                .orElse(this.root);
        parentNode.add(node);
        if (deep == functions.length) {
            node.setAllowsChildren(false);
        }
    }

}