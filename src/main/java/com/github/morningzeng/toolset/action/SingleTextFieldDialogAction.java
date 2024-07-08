package com.github.morningzeng.toolset.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-22
 */
public class SingleTextFieldDialogAction extends AnAction {

    private final Project project;
    private final String title;
    private final String label;
    private final Consumer<String> consumer;

    public SingleTextFieldDialogAction(final Project project, final String title, final String label, final Consumer<String> consumer) {
        super(label);
        this.project = project;
        this.title = title;
        this.label = label;
        this.consumer = consumer;
    }

    public SingleTextFieldDialogAction(final @Nullable Icon icon, final Project project, final String title, final String label, final Consumer<String> consumer) {
        super(icon);
        this.project = project;
        this.title = title;
        this.label = label;
        this.consumer = consumer;
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        new DialogWrapper(this.project) {
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
