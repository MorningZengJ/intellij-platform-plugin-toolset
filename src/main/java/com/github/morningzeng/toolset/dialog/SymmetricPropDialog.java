package com.github.morningzeng.toolset.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.dialog.SymmetricPropDialog.RightPanel;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import com.github.morningzeng.toolset.model.Pair;
import com.github.morningzeng.toolset.model.SymmetricCryptoProp;
import com.github.morningzeng.toolset.proxy.InitializingBean;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagBuilder;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
@Slf4j
public final class SymmetricPropDialog extends AbstractPropDialog<SymmetricCryptoProp, RightPanel> {

    public SymmetricPropDialog(final Project project, final Consumer<List<SymmetricCryptoProp>> okAfterConsumer) {
        super(project, okAfterConsumer);
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
    void writeProp(final SymmetricCryptoProp prop, final RightPanel rightPanel) {
        prop.setTitle(rightPanel.titleTextField.getText())
                .setKey(rightPanel.keyTextField.getText())
                .setKeyType(rightPanel.keyTypeCombo.getItem())
                .setIv(rightPanel.ivTextField.getText())
                .setIvType(rightPanel.ivTypeCombo.getItem())
                .setDescription(rightPanel.descTextArea.getText());
    }

    @Override
    RightPanel createRightItemPanel(final SymmetricCryptoProp prop) {
        return InitializingBean.create(
                RightPanel.class,
                Pair.of(Project.class, this.project),
                Pair.of(prop.getClass(), prop)
        );
    }

    static final class RightPanel extends AbstractRightPanel<SymmetricCryptoProp> {

        private final LabelTextField keyTextField = new LabelTextField("Key");
        private final ComboBox<DataToBinaryTypeEnum> keyTypeCombo = new ComboBox<>(DataToBinaryTypeEnum.values());
        private final LabelTextField ivTextField = new LabelTextField("IV");
        private final ComboBox<DataToBinaryTypeEnum> ivTypeCombo = new ComboBox<>(DataToBinaryTypeEnum.values());
        private final LabelTextArea descTextArea;

        RightPanel(final Project project, final SymmetricCryptoProp prop) {
            super(prop);
            this.descTextArea = new LabelTextArea(project, "Desc");
        }

        @Override
        protected Consumer<GridBagBuilder<AbstractRightPanel<SymmetricCryptoProp>>> itemLayout() {
            return builder -> {
                this.keyTextField.setText(prop.getKey());
                Optional.ofNullable(prop.keyType()).ifPresent(this.keyTypeCombo::setSelectedItem);
                this.ivTextField.setText(prop.getIv());
                Optional.ofNullable(prop.ivType()).ifPresent(this.ivTypeCombo::setSelectedItem);
                this.descTextArea.setText(prop.getDescription());

                builder.newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                                .newCell().weightX(1).gridWidth(2).add(this.titleTextField))
                        .newRow(row -> row.newCell().add(this.keyTextField)
                                .newCell().weightX(0).add(this.keyTypeCombo))
                        .newRow(row -> row.newCell().weightX(1).add(this.ivTextField)
                                .newCell().weightX(0).add(this.ivTypeCombo))
                        .newRow(row -> row.fill(GridBagFill.BOTH)
                                .newCell().weightY(1).gridWidth(2).add(this.descTextArea));
            };
        }
    }

}
