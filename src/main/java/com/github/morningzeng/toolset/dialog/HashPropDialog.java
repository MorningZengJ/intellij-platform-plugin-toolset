package com.github.morningzeng.toolset.dialog;

import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.DialogGroupAction;
import com.github.morningzeng.toolset.component.FocusColorTextArea;
import com.github.morningzeng.toolset.component.TreeComponent;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.config.LocalConfigFactory.HashCryptoProp;
import com.github.morningzeng.toolset.support.ScrollSupport;
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
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
@Slf4j
public final class HashPropDialog extends DialogWrapper implements DialogSupport {

    final LocalConfigFactory STATE_FACTORY = LocalConfigFactory.getInstance();
    final Splitter pane = new Splitter(false, .3f);
    final JBPanel<JBPanelWithEmptyText> btnPanel;
    final Project project;

    private final JBTextField titleTextField = new JBTextField(50);
    private final JBTextField keyTextField = new JBTextField(50);
    private final FocusColorTextArea descTextArea = FocusColorTextArea.builder()
            .row(5)
            .column(50)
            .focusListener();
    private final TreeComponent tree = new TreeComponent();


    public HashPropDialog(final Project project) {
        super(project);
        this.project = project;
        this.btnPanel = new DialogGroupAction(this, this.pane, this.initGroupAction());
        this.btnPanel.setLayout(new BoxLayout(this.btnPanel, BoxLayout.LINE_AXIS));

        this.initTree();

        init();
        setTitle("Hash Properties");
    }

    void initTree() {
        final Set<Entry<String, Set<HashCryptoProp>>> entries = STATE_FACTORY.hashCryptoPropsMap().entrySet();
        this.tree.setNodes(entries, Entry::getKey, Entry::getValue);
        this.tree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            if (Objects.nonNull(selectedNode) && !selectedNode.getAllowsChildren()) {
                final HashCryptoProp prop = (HashCryptoProp) selectedNode.getUserObject();
                this.createRightPanel(prop);
            }
        });
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
        this.writeProp();
        final Map<String, Set<HashCryptoProp>> collect = this.tree.leafNodes().stream().collect(
                Collectors.groupingBy(node -> {
                    final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                    return parent.isRoot() ? node.getUserObject().toString() : parent.getUserObject().toString();
                }, Collectors.mapping(
                        node -> (HashCryptoProp) node.getUserObject(), Collectors.toUnmodifiableSet()
                ))
        );
        STATE_FACTORY.hashCryptoPropsMap(collect);
        STATE_FACTORY.loadState(STATE_FACTORY.getState());
    }

    AnAction[] initGroupAction() {
        return new AnAction[]{
                new SingleTextFieldDialogAction(this.project, "Add Group", "Group", group -> {
                    STATE_FACTORY.hashCryptoPropsMap().computeIfAbsent(group, g -> Sets.newHashSet());
                    final DefaultMutableTreeNode root = this.tree.getRoot();
                    final DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
                    root.add(groupNode);
                    this.tree.reloadTree();
                    TreeUtil.selectNode(tree, groupNode);
                }),
                new AnAction("KeyPair") {
                    @Override
                    public void actionPerformed(@NotNull final AnActionEvent e) {
                        createPropItem();
                    }
                }
        };
    }

    public void createPropItem() {
        final HashCryptoProp cryptoProp = HashCryptoProp.builder()
                .title("Key pairs")
                .build();
        this.tree.createNodeOnSelectNode(cryptoProp, false);
        this.createRightPanel(cryptoProp);
    }

    @Override
    public void delete() {
        this.tree.delete(treePaths -> this.createRightPanel(null));
    }

    void writeProp() {
        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
        if (Objects.isNull(selectedNode)) {
            return;
        }
        if (selectedNode.getUserObject() instanceof HashCryptoProp cryptoProp) {
            cryptoProp.setTitle(this.titleTextField.getText())
                    .setKey(this.keyTextField.getText())
                    .setDesc(this.descTextArea.getText());
            this.tree.reloadTree(selectedNode);
        }
    }

    void createLeftPanel() {
        final JBPanel<JBPanelWithEmptyText> leftPanel = new JBPanel<>();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(this.btnPanel);

        // add scroll
        leftPanel.add(ScrollSupport.getInstance(this.tree).scrollPane());

        this.pane.setFirstComponent(leftPanel);
    }

    public void createRightPanel(final HashCryptoProp cryptoProp) {
        final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>();
        panel.setLayout(new GridBagLayout());
        this.pane.setSecondComponent(panel);

        if (Objects.isNull(cryptoProp)) {
            return;
        }

        this.titleTextField.setText(cryptoProp.getTitle());
        this.keyTextField.setText(cryptoProp.getKey());
        this.descTextArea.setText(cryptoProp.getDesc());

        GridLayoutUtils.builder()
                .container(panel).fill(GridBag.HORIZONTAL).add(new JBLabel("Title"))
                .newCell().weightX(1).gridWidth(3).add(this.titleTextField)
                .newRow().add(new JBLabel("Key"))
                .newCell().weightX(1).add(this.keyTextField)
                .newRow().add(new JBLabel("Desc"))
                .newCell().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(this.descTextArea.scrollPane());
    }

}
