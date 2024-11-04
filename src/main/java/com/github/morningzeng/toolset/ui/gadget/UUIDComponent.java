package com.github.morningzeng.toolset.ui.gadget;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;

import javax.swing.JButton;
import java.util.UUID;

/**
 * @author Morning Zeng
 * @since 2024-11-04
 */
public class UUIDComponent extends JBPanel<JBPanelWithEmptyText> {

    private final LanguageTextArea contentArea;
    private final JButton generateBtn = new JButton("Generate", IconC.AUTORENEW);
    private final JBCheckBox shortCheckbox = new JBCheckBox("Short");
    private final JBCheckBox upperCaseCheckbox = new JBCheckBox("Upper case");
    private volatile UUID uuid;

    public UUIDComponent(final Project project) {
        this.contentArea = new LanguageTextArea(project);
        this.contentArea.setReadOnly(true);
        this.contentArea.autoReformat(false);
        this.contentArea.setPlaceholder("You can click the Generate button above to get the UUID");

        this.initLayout();
        this.initEvent();
    }

    private void initLayout() {
        GridBagUtils.builder(this)
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().weightX(1).add(this.generateBtn)
                        .newCell().weightX(0).add(this.shortCheckbox)
                        .newCell().add(this.upperCaseCheckbox))
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightX(1).weightY(1).gridWidth(3).add(this.contentArea));
    }

    private void initEvent() {
        this.generateBtn.addActionListener(e -> {
            this.uuid = UUID.randomUUID();
            this.renderUUID();
        });
        this.shortCheckbox.addItemListener(e -> this.renderUUID());
        this.upperCaseCheckbox.addItemListener(e -> this.renderUUID());
    }

    private void renderUUID() {
        String uuidString = uuid.toString();
        if (this.shortCheckbox.isSelected()) {
            uuidString = uuidString.replace("-", "");
        }
        if (this.upperCaseCheckbox.isSelected()) {
            uuidString = uuidString.toUpperCase();
        }
        this.contentArea.setText(uuidString);
    }
}
