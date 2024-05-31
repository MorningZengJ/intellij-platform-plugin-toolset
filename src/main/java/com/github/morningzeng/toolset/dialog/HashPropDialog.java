package com.github.morningzeng.toolset.dialog;

import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.FocusColorTextArea;
import com.github.morningzeng.toolset.config.HashCryptoProp;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
@Slf4j
public final class HashPropDialog extends AbstractPropDialog {

    private final JBTextField titleTextField = new JBTextField(50);
    private final JBTextField keyTextField = new JBTextField(50);
    private final FocusColorTextArea descTextArea = FocusColorTextArea.builder()
            .row(5)
            .column(50)
            .focusListener();

    public HashPropDialog(final Project project) {
        super(project);
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

    @Override
    <T> void createRightPanel(final T t) {
        final JBPanel<JBPanelWithEmptyText> panel = this.defaultRightPanel();
        if (Objects.isNull(t)) {
            return;
        }
        if (t instanceof HashCryptoProp cryptoProp) {
            this.itemRightPanel(panel, cryptoProp);
        }
    }

    void createPropItem() {
        final HashCryptoProp cryptoProp = HashCryptoProp.builder()
                .title("Key pairs")
                .build();
        this.tree.createNodeOnSelectNode(cryptoProp, false);
        this.createRightPanel(cryptoProp);
    }

    void writeProp() {
        this.tree.lastSelected(selectedNode -> {
            if (selectedNode.getUserObject() instanceof HashCryptoProp cryptoProp) {
                cryptoProp.setTitle(this.titleTextField.getText())
                        .setKey(this.keyTextField.getText())
                        .setDesc(this.descTextArea.getText());
                this.tree.reloadTree(selectedNode);
            }
        });
    }

    void itemRightPanel(final JBPanel<JBPanelWithEmptyText> panel, final HashCryptoProp cryptoProp) {
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
