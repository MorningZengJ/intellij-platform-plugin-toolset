package com.github.morningzeng.toolset.dialog;

import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.config.SymmetricCryptoProp;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
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
public final class SymmetricPropDialog extends AbstractPropDialog {

    private final LabelTextField titleTextField = new LabelTextField("Title");
    private final LabelTextField keyTextField = new LabelTextField("Key");
    private final ComboBox<DataToBinaryTypeEnum> keyTypeCombo = new ComboBox<>(DataToBinaryTypeEnum.values());
    private final LabelTextField ivTextField = new LabelTextField("IV");
    private final ComboBox<DataToBinaryTypeEnum> ivTypeCombo = new ComboBox<>(DataToBinaryTypeEnum.values());
    private final LabelTextArea descTextArea = new LabelTextArea("Desc");

    public SymmetricPropDialog(final Project project) {
        super(project);
        this.btnPanel.setLayout(new BoxLayout(this.btnPanel, BoxLayout.LINE_AXIS));

        this.initTree();

        init();
        setTitle("Symmetric Properties");
    }

    void initTree() {
        final Set<Entry<String, Set<SymmetricCryptoProp>>> entries = stateFactory.symmetricCryptoPropsMap().entrySet();
        this.tree.setNodes(entries, Entry::getKey, Entry::getValue);
        this.tree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            if (Objects.nonNull(selectedNode) && !selectedNode.getAllowsChildren()) {
                final SymmetricCryptoProp prop = (SymmetricCryptoProp) selectedNode.getUserObject();
                this.createRightPanel(prop);
            }
        });
    }

    AnAction[] initGroupAction() {
        return new AnAction[]{
                new SingleTextFieldDialogAction(this.project, "Add Group", "Group", group -> {
                    stateFactory.symmetricCryptoPropsMap().computeIfAbsent(group, g -> Sets.newHashSet());
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
        final Map<String, Set<SymmetricCryptoProp>> map = this.tree.leafNodes().stream().collect(
                Collectors.groupingBy(node -> {
                    final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                    return parent.isRoot() ? node.getUserObject().toString() : parent.getUserObject().toString();
                }, Collectors.mapping(
                        node -> (SymmetricCryptoProp) node.getUserObject(), Collectors.toUnmodifiableSet()
                ))
        );
        stateFactory.symmetricCryptoPropsMap(map);
    }

    @Override
    <T> void createRightPanel(final T t) {
        final JBPanel<JBPanelWithEmptyText> panel = this.defaultRightPanel();
        if (Objects.isNull(t)) {
            return;
        }
        if (t instanceof SymmetricCryptoProp cryptoProp) {
            this.itemRightPanel(panel, cryptoProp);
        }
    }

    void createPropItem() {
        final SymmetricCryptoProp cryptoProp = SymmetricCryptoProp.builder()
                .title("Key pairs")
                .build();
        this.tree.createNodeOnSelectNode(cryptoProp, false);
        this.createRightPanel(cryptoProp);
    }

    void writeProp() {
        this.tree.lastSelected(selectedNode -> {
            if (selectedNode.getUserObject() instanceof SymmetricCryptoProp cryptoProp) {
                cryptoProp.setTitle(this.titleTextField.getText())
                        .setKey(this.keyTextField.getText())
                        .setKeyType(this.keyTypeCombo.getItem())
                        .setDesc(this.descTextArea.getText());
                cryptoProp.setIv(this.ivTextField.getText()).setIvType(this.ivTypeCombo.getItem());
                this.tree.reloadTree(selectedNode);
            }
        });
    }

    void itemRightPanel(final JBPanel<JBPanelWithEmptyText> panel, final SymmetricCryptoProp cryptoProp) {
        this.titleTextField.setText(cryptoProp.getTitle());
        this.keyTextField.setText(cryptoProp.getKey());
        this.keyTypeCombo.setSelectedItem(cryptoProp.keyType());
        this.ivTextField.setText(cryptoProp.getIv());
        this.ivTypeCombo.setSelectedItem(cryptoProp.ivType());
        this.descTextArea.setText(cryptoProp.getDesc());

        GridLayoutUtils.builder()
                .container(panel).fill(GridBag.HORIZONTAL).weightX(1).gridWidth(2).add(this.titleTextField)
                .newRow().weightX(1).add(this.keyTextField)
                .newCell().weightX(0).add(this.keyTypeCombo)
                .newRow().weightX(1).add(this.ivTextField)
                .newCell().weightX(0).add(this.ivTypeCombo)
                .newRow().fill(GridBag.BOTH).weightY(1).gridWidth(2).add(this.descTextArea);
    }

}
