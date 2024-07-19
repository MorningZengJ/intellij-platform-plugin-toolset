package com.github.morningzeng.toolset.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import com.github.morningzeng.toolset.model.SymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
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
public final class SymmetricPropDialog extends AbstractPropDialog<SymmetricCryptoProp> {

    private final LabelTextField titleTextField = new LabelTextField("Title");
    private final LabelTextField keyTextField = new LabelTextField("Key");
    private final ComboBox<DataToBinaryTypeEnum> keyTypeCombo = new ComboBox<>(DataToBinaryTypeEnum.values());
    private final LabelTextField ivTextField = new LabelTextField("IV");
    private final ComboBox<DataToBinaryTypeEnum> ivTypeCombo = new ComboBox<>(DataToBinaryTypeEnum.values());
    private final LabelTextArea descTextArea = new LabelTextArea("Desc");

    public SymmetricPropDialog(final Project project, final Consumer<List<SymmetricCryptoProp>> okAfterConsumer) {
        super(project, okAfterConsumer);
        this.actionBar.setLayout(new BoxLayout(this.actionBar, BoxLayout.LINE_AXIS));

        init();
        setTitle("Symmetric Properties");
    }

    @Override
    TypeReference<List<SymmetricCryptoProp>> typeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    SymmetricCryptoProp generateBean(final String name, final boolean isGroup) {
        return SymmetricCryptoProp.builder().title(name).directory(isGroup).build();
    }

    @Override
    void writeProp() {
        final SymmetricCryptoProp prop = this.tree.getSelectedValue();
        if (Objects.isNull(prop) || prop.isDirectory()) {
            return;
        }
        prop.setTitle(this.titleTextField.getText())
                .setKey(this.keyTextField.getText())
                .setKeyType(this.keyTypeCombo.getItem())
                .setIv(this.ivTextField.getText())
                .setIvType(this.ivTypeCombo.getItem())
                .setDescription(this.descTextArea.getText());
        this.tree.reloadTree((TreeNode) this.tree.getLastSelectedPathComponent());
    }

    @Override
    void createRightPanel(final SymmetricCryptoProp prop) {
        final JBPanel<JBPanelWithEmptyText> panel = this.defaultRightPanel();
        if (Objects.isNull(prop)) {
            return;
        }
        this.itemRightPanel(panel, prop);
    }

    void itemRightPanel(final JBPanel<JBPanelWithEmptyText> panel, final SymmetricCryptoProp cryptoProp) {
        this.titleTextField.setText(cryptoProp.getTitle());
        this.keyTextField.setText(cryptoProp.getKey());
        this.keyTypeCombo.setSelectedItem(cryptoProp.keyType());
        this.ivTextField.setText(cryptoProp.getIv());
        this.ivTypeCombo.setSelectedItem(cryptoProp.ivType());
        this.descTextArea.setText(cryptoProp.getDescription());

        GridLayoutUtils.builder()
                .container(panel).fill(GridBag.HORIZONTAL).weightX(1).gridWidth(2).add(this.titleTextField)
                .newRow().weightX(1).add(this.keyTextField)
                .newCell().weightX(0).add(this.keyTypeCombo)
                .newRow().weightX(1).add(this.ivTextField)
                .newCell().weightX(0).add(this.ivTypeCombo)
                .newRow().fill(GridBag.BOTH).weightY(1).gridWidth(2).add(this.descTextArea);
    }

}
