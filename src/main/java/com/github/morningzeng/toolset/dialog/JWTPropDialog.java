package com.github.morningzeng.toolset.dialog;

import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelComboBox;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.config.JWTProp;
import com.github.morningzeng.toolset.enums.AlgorithmEnum;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ItemEvent;
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
public final class JWTPropDialog extends AbstractPropDialog {

    private final LabelTextField titleTextField = new LabelTextField("Title");
    private final LabelComboBox<AlgorithmEnum> signAlgorithmCombo = new LabelComboBox<>("SA", AlgorithmEnum.values());
    private final LabelTextField symmetricKeyTextField = new LabelTextField("Symmetric Key");
    private final LabelComboBox<DataToBinaryTypeEnum> symmetricKeyTypeCombo = new LabelComboBox<>("Symmetric key type", DataToBinaryTypeEnum.values());
    private final LabelTextArea privateKeyTextArea = new LabelTextArea("Private key");
    private final LabelTextArea publicKeyTextArea = new LabelTextArea("Public key");
    private final LabelTextArea descTextArea = new LabelTextArea("Desc");

    public JWTPropDialog(final Project project) {
        super(project);
        this.btnPanel.setLayout(new BoxLayout(this.btnPanel, BoxLayout.LINE_AXIS));

        this.initTree();

        init();
        setTitle("JWT Properties");
        this.initEvent();
    }

    void initTree() {
        final Set<Entry<String, Set<JWTProp>>> entries = STATE_FACTORY.jwtPropsMap().entrySet();
        this.tree.setNodes(entries, Entry::getKey, Entry::getValue);
        this.tree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            if (Objects.nonNull(selectedNode) && !selectedNode.getAllowsChildren()) {
                final JWTProp prop = (JWTProp) selectedNode.getUserObject();
                this.createRightPanel(prop);
            }
        });
    }

    AnAction[] initGroupAction() {
        return new AnAction[]{
                new SingleTextFieldDialogAction(this.project, "Add Group", "Group", group -> {
                    STATE_FACTORY.jwtPropsMap().computeIfAbsent(group, g -> Sets.newHashSet());
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
        final Map<String, Set<JWTProp>> collect = this.tree.leafNodes().stream().collect(
                Collectors.groupingBy(node -> {
                    final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                    return parent.isRoot() ? node.getUserObject().toString() : parent.getUserObject().toString();
                }, Collectors.mapping(
                        node -> (JWTProp) node.getUserObject(), Collectors.toUnmodifiableSet()
                ))
        );
        STATE_FACTORY.jwtPropsMap(collect);
        STATE_FACTORY.loadState(STATE_FACTORY.getState());
    }

    @Override
    <T> void createRightPanel(final T t) {
        final JBPanel<JBPanelWithEmptyText> panel = this.defaultRightPanel();
        if (Objects.isNull(t)) {
            return;
        }
        if (t instanceof JWTProp cryptoProp) {
            this.itemRightPanel(panel, cryptoProp);
        }
    }

    void createPropItem() {
        final JWTProp cryptoProp = JWTProp.builder()
                .title("Key pairs")
                .build();
        this.tree.createNodeOnSelectNode(cryptoProp, false);
        this.createRightPanel(cryptoProp);
    }

    void writeProp() {
        this.tree.lastSelected(selectedNode -> {
            if (selectedNode.getUserObject() instanceof JWTProp prop) {
                prop.setTitle(this.titleTextField.getText())
                        .setSignAlgorithm(this.signAlgorithmCombo.getItem())
                        .setSymmetricKey(this.symmetricKeyTextField.getText())
                        .setSymmetricKeyType(this.symmetricKeyTypeCombo.getItem())
                        .setPrivateKey(this.privateKeyTextArea.getText())
                        .setPublicKey(this.publicKeyTextArea.getText())
                        .setDesc(this.descTextArea.getText());
                this.tree.reloadTree(selectedNode);
            }
        });
    }

    void itemRightPanel(final JBPanel<JBPanelWithEmptyText> panel, final JWTProp prop) {
        this.titleTextField.setText(prop.getTitle());
        this.signAlgorithmCombo.setSelectedItem(prop.signAlgorithm());
        this.symmetricKeyTextField.setText(prop.getSymmetricKey());
        this.symmetricKeyTypeCombo.setSelectedItem(prop.symmetricKeyType());
        this.privateKeyTextArea.setText(prop.getPrivateKey());
        this.publicKeyTextArea.setText(prop.getPublicKey());
        this.descTextArea.setText(prop.getDesc());


        handleSignAlgorithmChange(prop.signAlgorithm());

        GridLayoutUtils.builder()
                .container(panel).fill(GridBag.HORIZONTAL).weightX(1).add(this.titleTextField)
                .newRow().weightX(1).add(this.signAlgorithmCombo)
                .newRow().weightX(1).add(this.symmetricKeyTextField)
                .newRow().weightX(1).add(this.symmetricKeyTypeCombo)
                .newRow().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(this.privateKeyTextArea)
                .newRow().gridWidth(3).add(this.publicKeyTextArea)
                .newRow().gridWidth(3).add(this.descTextArea);
    }

    void initEvent() {
        this.signAlgorithmCombo.tConsumer(comboBox -> comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final AlgorithmEnum selected = (AlgorithmEnum) e.getItem();
                handleSignAlgorithmChange(selected);
            }
        }));
    }

    private void handleSignAlgorithmChange(final AlgorithmEnum algorithm) {
        switch (algorithm) {
            case NONE -> {
                this.symmetricKeyTextField.setVisible(false);
                this.symmetricKeyTypeCombo.setVisible(false);
                this.privateKeyTextArea.setVisible(false);
                this.publicKeyTextArea.setVisible(false);
            }
            case HS256, HS384, HS512 -> {
                this.symmetricKeyTextField.setVisible(true);
                this.symmetricKeyTypeCombo.setVisible(true);
                this.privateKeyTextArea.setVisible(false);
                this.publicKeyTextArea.setVisible(false);
            }
            default -> {
                this.symmetricKeyTextField.setVisible(false);
                this.symmetricKeyTypeCombo.setVisible(false);
                this.privateKeyTextArea.setVisible(true);
                this.publicKeyTextArea.setVisible(true);
            }
        }
    }

}
