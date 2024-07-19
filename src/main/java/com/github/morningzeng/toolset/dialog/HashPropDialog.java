package com.github.morningzeng.toolset.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.model.HashCryptoProp;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import lombok.extern.slf4j.Slf4j;

import javax.swing.BoxLayout;
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

        init();
        setTitle("Hash Properties");
    }

    @Override
    TypeReference<List<HashCryptoProp>> typeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    HashCryptoProp generateBean(final String name, final boolean isGroup) {
        return HashCryptoProp.builder().title(name).directory(isGroup).build();
    }

    @Override
    void writeProp() {
        final HashCryptoProp prop = this.tree.getSelectedValue();
        if (Objects.isNull(prop) || prop.isDirectory()) {
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
