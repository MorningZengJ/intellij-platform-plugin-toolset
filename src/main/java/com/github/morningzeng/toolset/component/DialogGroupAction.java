package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.dialog.DialogSupport;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI.Borders;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
public final class DialogGroupAction extends JBPanel<JBPanelWithEmptyText> {

    final DialogSupport dialogSupport;
    final AnAction[] actions;

    public DialogGroupAction(final DialogSupport dialogSupport, final JComponent component, final AnAction... actions) {
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        this.setBorder(Borders.customLineBottom(JBColor.GRAY));
        this.dialogSupport = dialogSupport;
        this.actions = actions;
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        final DefaultActionGroup actionGroup = this.createDialogGroupAction();
        final AnAction popupAction = new AnAction("Add Item", "New create crypto prop item", General.Add) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                // Here is the operation of the pop-up menu
                ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu("toolbar", actionGroup);
                popupMenu.getComponent().show(e.getInputEvent().getComponent(),
                        e.getInputEvent().getComponent().getWidth() / 2,
                        e.getInputEvent().getComponent().getHeight() / 2);
            }
        };
        ApplicationManager.getApplication().invokeAndWait(() -> {
                    final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("toolbar", new DefaultActionGroup(popupAction, this.deleteAction()), true);
                    toolbar.setTargetComponent(component);
                    SwingUtilities.invokeLater(() -> this.add(toolbar.getComponent()));
                }
        );
    }

    DefaultActionGroup createDialogGroupAction() {
        final Icon icon = LayeredIcon.create(IconC.ADD_GREEN, General.Dropdown);

        final DefaultActionGroup actionGroup = new DefaultActionGroup("Add Item", "New create crypto prop item", icon);

        actionGroup.addAll(this.actions);
        actionGroup.setPopup(true);
        return actionGroup;
    }

    AnAction deleteAction() {
        return new AnAction(IconC.REMOVE_RED) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                dialogSupport.delete();
            }
        };
    }

}
