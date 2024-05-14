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
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-05-09
 */
@Slf4j
public class PlaceholderTextField extends JBTextField implements DocumentListener {

    private String placeholder;

    {
        super.setForeground(JBColor.GRAY);
        this.getDocument().addDocumentListener(this);
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                PlaceholderTextField.this.focusGained();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                PlaceholderTextField.this.focusLost();
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
        this.focusLost();
    }

    public PlaceholderTextField(final int columns, final String placeholder) {
        super(columns);
        this.placeholder(placeholder);
        this.focusLost();
    }

    public void placeholder(final String placeholder) {
        this.placeholder = placeholder;
        this.setText(placeholder);
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
    }


    void focusGained() {
        if (Objects.equals(this.getText(), this.placeholder)) {
            setText("");
        }
        setForeground(JBColor.BLACK);
    }

    void focusLost() {
        if (StringUtil.isEmpty(this.getText())) {
            setText(this.placeholder);
            setForeground(JBColor.GRAY);
        }
    }

}
