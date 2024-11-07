package com.github.morningzeng.toolset.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextArea;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.dialog.HashPropDialog.RightPanel;
import com.github.morningzeng.toolset.model.HashCryptoProp;
import com.github.morningzeng.toolset.proxy.InitializingBean;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagBuilder;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
@Slf4j
public final class HashPropDialog extends AbstractPropDialog<HashCryptoProp, RightPanel> {

    public HashPropDialog(final Project project, final Consumer<List<HashCryptoProp>> callback) {
        super(project, callback);
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
    void writeProp(final HashCryptoProp prop, final RightPanel rightPanel) {
        prop.setTitle(rightPanel.titleTextField.getText())
                .setKey(rightPanel.keyTextField.getText())
                .setDescription(rightPanel.descTextArea.getText());
    }

    @Override
    RightPanel createRightItemPanel(final HashCryptoProp prop) {
        return InitializingBean.create(RightPanel.class, prop);
    }

    static final class RightPanel extends AbstractRightPanel<HashCryptoProp> {

        private final LabelTextField keyTextField = new LabelTextField("Key");
        private final LabelTextArea descTextArea = new LabelTextArea("Desc");

        RightPanel(final HashCryptoProp prop) {
            super(prop);
        }

        @Override
        protected Consumer<GridBagBuilder<AbstractRightPanel<HashCryptoProp>>> itemLayout() {
            return builder -> {
                this.keyTextField.setText(prop.getKey());
                this.descTextArea.setText(prop.getDescription());

                builder.newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                                .newCell().weightX(1).add(this.titleTextField))
                        .newRow(row -> row.newCell().weightX(1).add(this.keyTextField))
                        .newRow(row -> row.fill(GridBagFill.BOTH).newCell().weightY(1).add(this.descTextArea));
            };
        }
    }

}
