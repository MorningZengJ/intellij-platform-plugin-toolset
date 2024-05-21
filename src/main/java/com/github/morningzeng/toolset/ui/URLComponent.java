package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.FocusColorTextArea;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public final class URLComponent extends JBPanel<JBPanelWithEmptyText> {

    private final FocusColorTextArea encodeArea = FocusColorTextArea.builder()
            .row(5)
            .column(20)
            .focusListener();
    private final FocusColorTextArea decodeArea = FocusColorTextArea.builder()
            .row(5)
            .column(20)
            .focusListener();
    private final JButton encodeBtn = new JButton("Encode", IconC.DOUBLE_ARROW_DOWN);
    private final JButton decodeBtn = new JButton("Decode", IconC.DOUBLE_ARROW_UP);

    {
        this.setLayout(new GridBagLayout());
        final JBPanel<JBPanelWithEmptyText> btnPanel = new JBPanel<>();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.LINE_AXIS));
        btnPanel.add(encodeBtn);
        btnPanel.add(decodeBtn);

        GridLayoutUtils.builder()
                .container(this).fill(GridBag.BOTH).weightX(1).weightY(1).add(this.decodeArea.scrollPane())
                .newRow().weightY(0).add(btnPanel)
                .newRow().weightY(1).add(this.encodeArea.scrollPane());
        this.initEvent();
    }

    void initEvent() {
        this.encodeBtn.addActionListener(e -> {
            try {
                final String enc = URLEncoder.encode(this.decodeArea.getText(), UTF_8);
                this.encodeArea.setText(enc);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Encoding Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        this.decodeBtn.addActionListener(e -> {
            try {
                final String dec = URLDecoder.decode(this.encodeArea.getText(), UTF_8);
                this.decodeArea.setText(dec);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Decoding Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
