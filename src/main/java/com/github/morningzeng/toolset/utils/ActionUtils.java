package com.github.morningzeng.toolset.utils;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.awt.Component;

/**
 * @author Morning Zeng
 * @since 2024-06-16
 */
public final class ActionUtils {

    public static AnAction drawerActions(final String text, final String description, final Icon icon, final AnAction... actions) {
        final DefaultActionGroup actionGroup = new DefaultActionGroup(text, description, icon);
        actionGroup.addAll(actions);
        actionGroup.setPopup(true);

        return new AnAction(text, description, icon) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                final ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, actionGroup);

                final Component component = e.getInputEvent().getComponent();
                final int width = component.getWidth();
                final int height = component.getHeight();
                popupMenu.getComponent().show(component, width, height);
            }
        };
    }

}
