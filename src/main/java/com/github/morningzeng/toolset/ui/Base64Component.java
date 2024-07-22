package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public final class Base64Component extends JBPanel<JBPanelWithEmptyText> {
    private final LanguageTextArea encodeArea;
    private final LanguageTextArea decodeArea;
    private final JButton encodeBtn = new JButton("Encode", IconC.DOUBLE_ANGLES_DOWN);
    private final JButton decodeBtn = new JButton("Decode", IconC.DOUBLE_ANGLES_UP);

    public Base64Component(final Project project) {
        this.encodeArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "", true);
        this.decodeArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.encodeArea.setPlaceholder("Base64 encoded text");
        this.decodeArea.setPlaceholder("Base64 decoded text");

        this.setLayout(new GridBagLayout());
        final JBPanel<JBPanelWithEmptyText> btnPanel = new JBPanel<>();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.LINE_AXIS));
        btnPanel.add(encodeBtn);
        btnPanel.add(decodeBtn);

        GridLayoutUtils.builder()
                .container(this).fill(GridBag.BOTH).weightX(1).weightY(1).add(this.decodeArea)
                .newRow().weightY(0).add(btnPanel)
                .newRow().weightY(1).add(this.encodeArea);
        this.initEvent();
    }

    void initEvent() {
        this.encodeBtn.addActionListener(e -> {
            try {
                final String enc = Base64.getEncoder().encodeToString(this.decodeArea.getText().getBytes(UTF_8));
                this.encodeArea.setText(enc);
            } catch (Exception ex) {
                Messages.showMessageDialog(this, ex.getMessage(), "Encoding Error", Messages.getErrorIcon());
            }
        });
        this.decodeBtn.addActionListener(e -> {
            try {
                final String dec = new String(Base64.getDecoder().decode(this.encodeArea.getText()), UTF_8);
                this.decodeArea.setText(dec);
            } catch (Exception ex) {
                Messages.showMessageDialog(this, ex.getMessage(), "Decoding Error", Messages.getErrorIcon());
            }
        });
    }

}
