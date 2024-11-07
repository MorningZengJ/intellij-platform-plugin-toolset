package com.github.morningzeng.toolset.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.dialog.JWTPropDialog.RightPanel;
import com.github.morningzeng.toolset.enums.AlgorithmEnum;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import com.github.morningzeng.toolset.model.JWTProp;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
@Slf4j
public final class JWTPropDialog extends AbstractPropDialog<JWTProp, RightPanel> {

    public JWTPropDialog(final Project project, final Consumer<List<JWTProp>> okAfterConsumer) {
        super(project, okAfterConsumer);
        init();
        setTitle("JWT Properties");
    }

    @Override
    TypeReference<List<JWTProp>> typeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    JWTProp generateBean(final String name, final boolean isGroup) {
        return JWTProp.builder().title(name).directory(isGroup).build();
    }

    @Override
    void writeProp(final JWTProp prop, final RightPanel rightPanel) {
        prop.setTitle(rightPanel.titleTextField.getText())
                .setSignAlgorithm(rightPanel.signAlgorithmCombo.getItem())
                .setSymmetricKey(rightPanel.symmetricKeyTextField.getText())
                .setSymmetricKeyType(rightPanel.symmetricKeyTypeCombo.getItem())
                .setPrivateKey(rightPanel.privateKeyTextArea.getText())
                .setPublicKey(rightPanel.publicKeyTextArea.getText())
                .setDescription(rightPanel.descTextArea.getText());
    }

    @Override
    RightPanel createRightPanel(final JWTProp prop) {
        return new RightPanel(prop);
    }

    static final class RightPanel extends AbstractRightPanel<JWTProp> {
        private final LabelTextField titleTextField = new LabelTextField("Title");
        private final ComboBox<AlgorithmEnum> signAlgorithmCombo = new ComboBox<>(
                Arrays.stream(AlgorithmEnum.values())
                        .filter(algorithm -> AlgorithmEnum.NONE != algorithm)
                        .toArray(AlgorithmEnum[]::new)
        );
        private final LabelTextField symmetricKeyTextField = new LabelTextField("Key");
        private final ComboBox<DataToBinaryTypeEnum> symmetricKeyTypeCombo = new ComboBox<>(DataToBinaryTypeEnum.values());
        private final LabelTextArea privateKeyTextArea = new LabelTextArea("Private key");
        private final LabelTextArea publicKeyTextArea = new LabelTextArea("Public key");
        private final LabelTextArea descTextArea = new LabelTextArea("Description");

        RightPanel(final JWTProp prop) {
            super(prop);
            this.initEvent();

            GridBagUtils.builder(this)
                    .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                            .newCell().weightX(1).add(this.titleTextField)
                            .newCell().weightX(0).add(this.signAlgorithmCombo))
                    .newRow(row -> row.newCell().weightX(1).add(this.symmetricKeyTextField)
                            .newCell().weightX(0).add(this.symmetricKeyTypeCombo))
                    .newRow(row -> row.fill(GridBagFill.BOTH)
                            .newCell().weightY(.5).gridWidth(2).add(this.privateKeyTextArea))
                    .newRow(row -> row.newCell().weightY(.5).gridWidth(2).add(this.publicKeyTextArea))
                    .newRow(row -> row.newCell().weightY(1).gridWidth(2).add(this.descTextArea));
        }


        void initEvent() {
            this.handleSignAlgorithmChange(this.signAlgorithmCombo.getItem());

            this.signAlgorithmCombo.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final AlgorithmEnum selected = (AlgorithmEnum) e.getItem();
                    this.handleSignAlgorithmChange(selected);
                }
            });
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

}
