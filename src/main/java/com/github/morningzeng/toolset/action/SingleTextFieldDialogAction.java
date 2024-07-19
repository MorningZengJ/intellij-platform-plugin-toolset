package com.github.morningzeng.toolset.action;

import com.github.morningzeng.toolset.utils.DialogWrapperUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
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
        DialogWrapperUtils.singleTextField(title, "Name", consumer);
    }
}
