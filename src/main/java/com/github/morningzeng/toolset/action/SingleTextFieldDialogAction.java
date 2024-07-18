package com.github.morningzeng.toolset.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-22
 */
public class SingleTextFieldDialogAction extends AnAction {

    private final String title;
    private final Consumer<String> consumer;

    public SingleTextFieldDialogAction(final String text, final String title, final Consumer<String> consumer) {
        this(text, null, null, title, consumer);
    }

    public SingleTextFieldDialogAction(final Icon icon, final String title, final Consumer<String> consumer) {
        this(null, null, icon, title, consumer);
    }

    public SingleTextFieldDialogAction(final String text, final String description, final Icon icon, final String title, final Consumer<String> consumer) {
        super(text, description, icon);
        this.title = title;
        this.consumer = consumer;
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
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
                panel.add(new JBLabel("Name"));
                panel.add(this.textField);
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
