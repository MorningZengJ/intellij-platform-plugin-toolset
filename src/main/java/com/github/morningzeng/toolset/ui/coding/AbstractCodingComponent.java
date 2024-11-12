package com.github.morningzeng.toolset.ui.coding;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;

import javax.swing.JButton;
import java.util.function.Supplier;

/**
 * @author Morning Zeng
 * @since 2024-11-12
 */
sealed abstract class AbstractCodingComponent extends JBPanel<JBPanelWithEmptyText> permits Base64Component, URLComponent {

    final LanguageTextArea encodeArea;
    final LanguageTextArea decodeArea;
    private final JButton encodeBtn = new JButton("Encode", IconC.DOUBLE_ANGLES_DOWN);
    private final JButton decodeBtn = new JButton("Decode", IconC.DOUBLE_ANGLES_UP);

    AbstractCodingComponent(final Project project) {
        this.encodeArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.decodeArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");

        this.initLayout();
        this.initEvent();
    }

    void initLayout() {
        GridBagUtils.builder(this)
                .fill(GridBagFill.BOTH)
                .newRow(row -> row.newCell().weightX(1).weightY(1).add(this.decodeArea.withRightBar()))
                .newRow(row -> {
                    final JBPanel<JBPanelWithEmptyText> btnPanel = GridBagUtils.builder()
                            .fill(GridBagFill.HORIZONTAL)
                            .newRow(_row -> _row.newCell().add(this.encodeBtn)
                                    .newCell().add(this.decodeBtn))
                            .build();
                    row.fill(GridBagFill.HORIZONTAL).newCell().weightY(0).add(btnPanel);
                })
                .fill(GridBagFill.BOTH)
                .newRow(row -> row.newCell().weightY(1).add(this.encodeArea.withRightBar()));
    }

    void initEvent() {
        this.encodeBtn.addActionListener(e -> {
            try {
                final String enc = this.encode().get();
                this.encodeArea.setText(enc);
            } catch (Exception ex) {
                Messages.showMessageDialog(this, ex.getMessage(), "Encoding Error", Messages.getErrorIcon());
            }
        });
        this.decodeBtn.addActionListener(e -> {
            try {
                final String dec = this.decode().get();
                this.decodeArea.setText(dec);
            } catch (Exception ex) {
                Messages.showMessageDialog(this, ex.getMessage(), "Decoding Error", Messages.getErrorIcon());
            }
        });
    }

    abstract Supplier<String> encode();

    abstract Supplier<String> decode();

}
