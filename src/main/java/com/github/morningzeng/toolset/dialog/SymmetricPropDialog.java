package com.github.morningzeng.toolset.dialog;

import com.github.morningzeng.toolset.component.DialogGroupAction;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.config.LocalConfigFactory.SymmetricCryptoProp;
import com.github.morningzeng.toolset.config.LocalConfigFactory.SymmetricCryptoProp.SymmetricCryptoPropBuilder;
import com.github.morningzeng.toolset.listener.ContentBorderListener;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
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
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
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

    private final JBTextField groupTextField = new JBTextField(50);
    private final JBTextField titleTextField = new JBTextField(50);
    private final JBTextField keyTextField = new JBTextField(25);
    private final JBTextField ivTextField = new JBTextField(25);
    private final JBTextArea descTextArea = new JBTextArea(5, 50);
    private final SimpleTree tree = new SimpleTree();
    private final DefaultTreeModel treeModel;
    private final JBScrollPane scrollPane = new JBScrollPane(this.tree);

    private final String type;

    public SymmetricPropDialog(final Project project, final String type) {
        super(project);
        this.type = type;
        this.btnPanel = new DialogGroupAction(this, this.pane);
        this.btnPanel.setLayout(new BoxLayout(this.btnPanel, BoxLayout.LINE_AXIS));

        final DefaultMutableTreeNode treeNode = STATE_FACTORY.symmetricCryptos().stream()
                .filter(cryptoProp -> this.type.equals(cryptoProp.typeOrDefault()) || cryptoProp.typeOrDefault().startsWith("Default"))
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.groupingBy(
                                        SymmetricCryptoProp::groupOrDefault
                                ),
                                map -> {
                                    final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                                    map.forEach((g, gs) -> {
                                        final DefaultMutableTreeNode sec = new DefaultMutableTreeNode(g);
                                        node.add(sec);
                                        gs.forEach(prop -> {
                                            final DefaultMutableTreeNode child = new DefaultMutableTreeNode(prop, false);
                                            sec.add(child);
                                        });
                                    });
                                    map.forEach((r, chi) -> node.add(new DefaultMutableTreeNode(r)));
                                    return node;
                                }
                        )
                );

        this.treeModel = new DefaultTreeModel(treeNode);
        this.tree.setModel(this.treeModel);
        this.tree.setRootVisible(false);
        this.tree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            if (!selectedNode.getAllowsChildren()) {
                final SymmetricCryptoProp prop = (SymmetricCryptoProp) selectedNode.getUserObject();
                this.createRightPanel(prop);
            }
        });

        init();
        setTitle("Symmetric Properties");
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
    protected Action @NotNull [] createActions() {
        return Stream.concat(
                Stream.of(new DialogWrapperAction("Apply") {
                    @Override
                    protected void doAction(final ActionEvent e) {
                        SymmetricPropDialog.this.writeProp();
                        STATE_FACTORY.loadState(STATE_FACTORY.getState());
                    }
                }),
                Stream.of(super.createActions())
        ).toArray(Action[]::new);
    }

    @Override
    public void create() {
        final SymmetricCryptoPropBuilder builder = SymmetricCryptoProp.builder()
                .type(this.type)
                .title("Key pairs");
        final TreePath selectedPath = this.tree.getSelectionPath();
        DefaultMutableTreeNode groupNode;
        if (Objects.nonNull(selectedPath)) {
            groupNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            final Object userObject = groupNode.getUserObject();
            if (userObject instanceof SymmetricCryptoProp prop) {
                groupNode = (DefaultMutableTreeNode) groupNode.getParent();
                builder.group(prop.groupOrDefault());
            } else {
                builder.group(String.valueOf(userObject));
            }
        } else {
            groupNode = new DefaultMutableTreeNode("Default Group");
            final DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.treeModel.getRoot();
            root.add(groupNode);
        }

        final SymmetricCryptoProp prop = builder.build();
        final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(prop, false);
        groupNode.add(newNode);
        STATE_FACTORY.symmetricCryptos().add(prop);
        this.reloadTree();
        TreeUtil.selectNode(this.tree, newNode);

        this.createRightPanel(prop);
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
                .setDesc(this.descTextArea.getText())
                .setGroup(this.groupTextField.getText());
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

        this.groupTextField.setText(cryptoProp.getGroup());
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
                .container(panel).fill(GridBag.HORIZONTAL).add(new JBLabel("Group"))
                .newCell().weightX(1).gridWidth(3).add(this.groupTextField)
                .newRow().add(new JBLabel("Title"))
                .newCell().weightX(1).gridWidth(3).add(this.titleTextField)
                .newRow().add(new JBLabel("Key"))
                .newCell().weightX(.5).add(this.keyTextField)
                .newCell().weightX(0).add(new JBLabel("IV"))
                .newCell().weightX(.5).add(this.ivTextField)
                .newRow().add(new JBLabel("Desc"))
                .newCell().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(descScrollPane);
    }

}
