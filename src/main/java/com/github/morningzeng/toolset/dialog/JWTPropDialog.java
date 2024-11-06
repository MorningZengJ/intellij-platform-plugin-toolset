package com.github.morningzeng.toolset.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelComboBox;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.enums.AlgorithmEnum;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import com.github.morningzeng.toolset.model.JWTProp;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import lombok.extern.slf4j.Slf4j;

import javax.swing.tree.TreeNode;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
@Slf4j
public final class JWTPropDialog extends AbstractPropDialog<JWTProp> {

    private final LabelTextField titleTextField = new LabelTextField("Title");
    private final LabelComboBox<AlgorithmEnum> signAlgorithmCombo = new LabelComboBox<>("SA", AlgorithmEnum.values());
    private final LabelTextField symmetricKeyTextField = new LabelTextField("Symmetric Key");
    private final LabelComboBox<DataToBinaryTypeEnum> symmetricKeyTypeCombo = new LabelComboBox<>("Symmetric key type", DataToBinaryTypeEnum.values());
    private final LabelTextArea privateKeyTextArea = new LabelTextArea("Private key");
    private final LabelTextArea publicKeyTextArea = new LabelTextArea("Public key");
    private final LabelTextArea descTextArea = new LabelTextArea("Desc");

    public JWTPropDialog(final Project project, final Consumer<List<JWTProp>> okAfterConsumer) {
        super(project, okAfterConsumer);
        init();
        setTitle("JWT Properties");
        this.initEvent();
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
    void writeProp() {
        final JWTProp prop = this.tree.getSelectedValue();
        if (prop == null || prop.isDirectory()) {
            return;
        }

        this.tree.reloadTree((TreeNode) this.tree.getLastSelectedPathComponent());
    }

    @Override
    void createRightPanel(final JWTProp prop) {
        final JBPanel<JBPanelWithEmptyText> panel = this.defaultRightPanel();
        if (Objects.isNull(prop)) {
            return;
        }

        GridBagUtils.builder(panel)
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL));
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
