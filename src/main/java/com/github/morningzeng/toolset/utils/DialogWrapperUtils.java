package com.github.morningzeng.toolset.utils;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextField;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-07-19
 */
public final class DialogWrapperUtils {

    public static void singleTextField(final String title, final String label, final Consumer<String> consumer) {
        new DialogWrapper(true) {
            private final JBTextField textField = new JBTextField();

            {
                init();
                setTitle(title);
            }

            @Override
            protected JComponent createCenterPanel() {
                final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>();
                panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
                panel.add(new JBLabel(label));
                panel.add(this.textField);
                this.textField.requestFocusInWindow();
                return panel;
            }

            @Override
            protected void doOKAction() {
                final String group = this.textField.getText();
                consumer.accept(group);
                super.doOKAction();
            }
        }.showAndGet();
    }

}
