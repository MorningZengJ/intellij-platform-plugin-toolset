package com.github.morningzeng.toolset.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.model.HashCryptoProp;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
@Slf4j
public final class HashPropDialog extends AbstractPropDialog<HashCryptoProp> {

    private final LabelTextField titleTextField = new LabelTextField("Title");
    private final LabelTextField keyTextField = new LabelTextField("Key");
    private final LabelTextArea descTextArea = new LabelTextArea("Desc");

    public HashPropDialog(final Project project, final Consumer<List<HashCryptoProp>> callback) {
        super(project, callback);
        this.actionBar.setLayout(new BoxLayout(this.actionBar, BoxLayout.LINE_AXIS));

        this.initTree();

        init();
        setTitle("Hash Properties");
    }

    void initTree() {
        final List<HashCryptoProp> props = ScratchFileUtils.read(new TypeReference<>() {
        });
        this.tree.setNodes(props, HashCryptoProp::isDirectory);
        this.tree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            if (Objects.nonNull(selectedNode) && !selectedNode.getAllowsChildren()) {
                final HashCryptoProp prop = (HashCryptoProp) selectedNode.getUserObject();
                this.createRightPanel(prop);
            }
        });
        this.tree.cellRenderer(prop -> {
            final Icon icon = prop.isDirectory() ? Nodes.Folder : IconC.TREE_NODE;
            return new JBLabel(prop.getTitle(), icon, SwingConstants.LEFT);
        });
    }

    AnAction[] initGroupAction() {
        return new AnAction[]{
                new SingleTextFieldDialogAction(this.project, "Add Group", "Group", group -> {
                    final HashCryptoProp prop = HashCryptoProp.builder().title(group).directory(true).build();
                    this.tree.create(prop, true);
                }),
                new AnAction("KeyPair") {
                    @Override
                    public void actionPerformed(@NotNull final AnActionEvent e) {
                        createPropItem();
                    }
                }
        };
    }

    @Override
    void writeProp() {
        final HashCryptoProp prop = this.tree.getSelectedValue();
        if (Objects.isNull(prop)) {
            return;
        }
        prop.setTitle(this.titleTextField.getText())
                .setKey(this.keyTextField.getText())
                .setDescription(this.descTextArea.getText());
        this.tree.reloadTree((TreeNode) this.tree.getLastSelectedPathComponent());
    }

    @Override
    void createRightPanel(final HashCryptoProp prop) {
        final JBPanel<JBPanelWithEmptyText> panel = this.defaultRightPanel();
        if (Objects.isNull(prop)) {
            return;
        }
        this.itemRightPanel(panel, prop);
    }

    void createPropItem() {
        final HashCryptoProp cryptoProp = HashCryptoProp.builder()
                .title("Key pairs")
                .build();
        this.tree.create(cryptoProp, false);
        this.createRightPanel(cryptoProp);
    }

    void itemRightPanel(final JBPanel<JBPanelWithEmptyText> panel, final HashCryptoProp cryptoProp) {
        this.titleTextField.setText(cryptoProp.getTitle());
        this.keyTextField.setText(cryptoProp.getKey());
        this.descTextArea.setText(cryptoProp.getDescription());

        GridLayoutUtils.builder()
                .container(panel).fill(GridBag.HORIZONTAL).weightX(1).add(this.titleTextField)
                .newRow().weightX(1).add(this.keyTextField)
                .newRow().fill(GridBag.BOTH).weightY(1).add(this.descTextArea);
    }

}
