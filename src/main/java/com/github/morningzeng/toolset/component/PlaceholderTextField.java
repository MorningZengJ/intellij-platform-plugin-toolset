package com.github.morningzeng.toolset.component;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nls;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author Morning Zeng
 * @since 2024-05-09
 */
@Slf4j
public class PlaceholderTextField extends JBTextField implements DocumentListener {

    private String placeholder;
    private volatile boolean hint;

    {
        this.hint = true;
        super.setForeground(JBColor.GRAY);
        this.getDocument().addDocumentListener(this);
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                PlaceholderTextField.this.hint = false;
                setText("");
                setForeground(JBColor.BLACK);
            }

            @Override
            public void focusLost(final FocusEvent e) {
                if (StringUtil.isEmpty(PlaceholderTextField.super.getText())) {
                    PlaceholderTextField.this.hint = true;
                    setText(PlaceholderTextField.this.placeholder);
                    setForeground(JBColor.GRAY);
                }
            }
        });
    }

    public PlaceholderTextField() {
    }

    public PlaceholderTextField(final int columns) {
        super(columns);
    }

    public PlaceholderTextField(@Nls final String text) {
        super(text);
    }

    public PlaceholderTextField(@Nls final String text, final int columns) {
        super(text, columns);
    }

    public PlaceholderTextField(@Nls final String text, final int columns, final String placeholder) {
        super(text, columns);
        this.placeholder(placeholder);
    }

    public PlaceholderTextField(final int columns, final String placeholder) {
        super(columns);
        this.placeholder(placeholder);
    }

    public void placeholder(final String placeholder) {
        this.placeholder = placeholder;
        this.setText(placeholder);
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        this.hint();
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        this.hint();
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        this.hint();
    }

    @Override
    public String getText() {
        return this.hint ? "" : super.getText();
    }

    void hint() {
    }

}
