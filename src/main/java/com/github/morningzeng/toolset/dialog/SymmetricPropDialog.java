package com.github.morningzeng.toolset.dialog;

import com.github.morningzeng.toolset.component.DialogGroupAction;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.config.LocalConfigFactory.SymmetricCryptoProp;
import com.github.morningzeng.toolset.listener.ContentBorderListener;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
@Slf4j
public final class SymmetricPropDialog extends DialogWrapper implements DialogSupport {

    final LocalConfigFactory STATE_FACTORY = LocalConfigFactory.getInstance();
    final Splitter pane = new Splitter(false, .3f);
    final JBPanel<JBPanelWithEmptyText> btnPanel;
    final Project project;

    private final JBTextField titleTextField = new JBTextField(50);
    private final JBTextField keyTextField = new JBTextField(25);
    private final JBTextField ivTextField = new JBTextField(25);
    private final JBTextArea descTextArea = new JBTextArea(5, 50);
    private final SimpleTree tree = new SimpleTree();
    private final DefaultTreeModel treeModel;
    private final JBScrollPane scrollPane = new JBScrollPane(this.tree);


    public SymmetricPropDialog(final Project project) {
        super(project);
        this.project = project;
        this.btnPanel = new DialogGroupAction(this, this.pane, this.initGroupAction());
        this.btnPanel.setLayout(new BoxLayout(this.btnPanel, BoxLayout.LINE_AXIS));

        this.treeModel = this.initTree();

        init();
        setTitle("Symmetric Properties");
    }

    DefaultTreeModel initTree() {
        final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode();
        STATE_FACTORY.symmetricCryptoPropsMap().forEach((group, symmetricCryptoProps) -> {
            final DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
            treeNode.add(groupNode);
            symmetricCryptoProps.forEach(prop -> groupNode.add(new DefaultMutableTreeNode(prop, false)));
        });

        final DefaultTreeModel treeModel = new DefaultTreeModel(treeNode);
        this.tree.setModel(treeModel);
        this.tree.setRootVisible(false);
        this.tree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            if (Objects.nonNull(selectedNode) && !selectedNode.getAllowsChildren()) {
                final SymmetricCryptoProp prop = (SymmetricCryptoProp) selectedNode.getUserObject();
                this.createRightPanel(prop);
            }
        });
        return treeModel;
    }

    @Override
    protected JComponent createCenterPanel() {
        this.createLeftPanel();
        this.createRightPanel(null);
        this.pane.setMinimumSize(new Dimension(700, 500));
        this.pane.setDividerWidth(1);
        return pane;
    }

    @Override
    protected void doOKAction() {
        this.saveConfig();
        super.doOKAction();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return Stream.concat(
                Stream.of(new DialogWrapperAction("Apply") {
                    @Override
                    protected void doAction(final ActionEvent e) {
                        saveConfig();
                    }
                }),
                Stream.of(super.createActions())
        ).toArray(Action[]::new);
    }

    void saveConfig() {
        SymmetricPropDialog.this.writeProp();
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        final Map<String, Set<SymmetricCryptoProp>> map = Sets.newHashSet(root.children().asIterator()).stream()
                .map(treeNode -> (DefaultMutableTreeNode) treeNode)
                .collect(
                        Collectors.groupingBy(node -> node.getUserObject().toString(), Collectors.mapping(
                                node -> Sets.newHashSet(node.children().asIterator()).stream()
                                        .map(child -> (DefaultMutableTreeNode) child)
                                        .map(child -> (SymmetricCryptoProp) child.getUserObject())
                                        .collect(Collectors.toUnmodifiableSet()),
                                Collectors.flatMapping(Collection::stream, Collectors.toUnmodifiableSet())
                        ))
                );
        STATE_FACTORY.symmetricCryptoPropsMap(map);
        STATE_FACTORY.loadState(STATE_FACTORY.getState());
    }

    AnAction[] initGroupAction() {
        return new AnAction[]{
                new AnAction("Group") {
                    @Override
                    public void actionPerformed(@NotNull final AnActionEvent e) {
                        new DialogWrapper(project) {
                            private final JBTextField textField = new JBTextField();

                            {
                                init();
                                setTitle("Add Group");
                            }

                            @Override
                            protected JComponent createCenterPanel() {
                                final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>();
                                panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
                                panel.add(new JBLabel("Group"));
                                panel.add(this.textField);
                                return panel;
                            }

                            @Override
                            protected void doOKAction() {
                                final String group = this.textField.getText();
                                STATE_FACTORY.symmetricCryptoPropsMap().computeIfAbsent(group, g -> Sets.newHashSet());
                                final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
                                final DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
                                root.add(groupNode);
                                treeModel.reload();
                                TreeUtil.selectNode(tree, groupNode);
                                super.doOKAction();
                            }
                        }.showAndGet();
                    }
                },
                new AnAction("KeyPair") {
                    @Override
                    public void actionPerformed(@NotNull final AnActionEvent e) {
                        createPropItem();
                    }
                }
        };
    }

    public void createPropItem() {
        final SymmetricCryptoProp cryptoProp = SymmetricCryptoProp.builder()
                .title("Key pairs")
                .build();
        DefaultMutableTreeNode selectedNodeModel = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
        if (Objects.isNull(selectedNodeModel)) {
            return;
        }
        if (!selectedNodeModel.getAllowsChildren()) {
            selectedNodeModel = (DefaultMutableTreeNode) selectedNodeModel.getParent();
        }

        final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(cryptoProp, false);
        selectedNodeModel.add(newNode);
        this.reloadTree();
        this.tree.expandPath(new TreePath(newNode.getPath()));
        TreeUtil.selectNode(this.tree, newNode);

        this.createRightPanel(cryptoProp);
    }

    @Override
    public void delete() {
        final TreePath selectedPath = this.tree.getSelectionPath();
        Optional.ofNullable(selectedPath)
                .ifPresent(treePath -> {
                    final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                    this.treeModel.removeNodeFromParent(selectedNode);
                    this.reloadTree();
                    this.createRightPanel(null);
                });
    }

    void reloadTree() {
        final Enumeration<TreePath> expandedPaths = this.tree.getExpandedDescendants(new TreePath(this.treeModel.getRoot()));
        this.treeModel.reload();
        Optional.ofNullable(expandedPaths)
                .ifPresent(treePathEnumeration -> {
                    while (treePathEnumeration.hasMoreElements()) {
                        this.tree.expandPath(treePathEnumeration.nextElement());
                    }
                });
    }

    void writeProp() {
        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
        final SymmetricCryptoProp cryptoProp = (SymmetricCryptoProp) selectedNode.getUserObject();
        cryptoProp.setTitle(this.titleTextField.getText())
                .setKey(this.keyTextField.getText())
                .setIv(this.ivTextField.getText())
                .setDesc(this.descTextArea.getText());
        this.treeModel.reload(selectedNode);
    }

    void createLeftPanel() {
        final JBPanel<JBPanelWithEmptyText> leftPanel = new JBPanel<>();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(this.btnPanel);

        // add scroll
        this.scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftPanel.add(scrollPane);

        this.pane.setFirstComponent(leftPanel);
    }

    public void createRightPanel(final SymmetricCryptoProp cryptoProp) {
        final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>(new GridBagLayout());
        this.pane.setSecondComponent(panel);

        if (Objects.isNull(cryptoProp)) {
            return;
        }

        this.titleTextField.setText(cryptoProp.getTitle());
        this.keyTextField.setText(cryptoProp.getKey());
        this.ivTextField.setText(cryptoProp.getIv());
        this.descTextArea.setText(cryptoProp.getDesc());
        this.descTextArea.setLineWrap(true);
        this.descTextArea.setWrapStyleWord(true);
        this.descTextArea.addFocusListener(ContentBorderListener.builder().component(this.descTextArea).init());
        final JBScrollPane descScrollPane = new JBScrollPane(this.descTextArea);
        descScrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descScrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        GridLayoutUtils.builder()
                .container(panel).fill(GridBag.HORIZONTAL).add(new JBLabel("Title"))
                .newCell().weightX(1).gridWidth(3).add(this.titleTextField)
                .newRow().add(new JBLabel("Key"))
                .newCell().weightX(.5).add(this.keyTextField)
                .newCell().weightX(0).add(new JBLabel("IV"))
                .newCell().weightX(.5).add(this.ivTextField)
                .newRow().add(new JBLabel("Desc"))
                .newCell().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(descScrollPane);
    }

}
