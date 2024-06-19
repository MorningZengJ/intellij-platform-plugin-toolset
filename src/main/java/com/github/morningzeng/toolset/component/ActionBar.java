package com.github.morningzeng.toolset.component;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI.Borders;

import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

/**
 * @author Morning Zeng
 * @since 2024-06-16
 */
public class ActionBar extends JBPanel<JBPanelWithEmptyText> {

    private final AnAction[] actions;

    public ActionBar(final AnAction... actions) {
        this(true, actions);
    }

    public ActionBar(final boolean horizontal, final AnAction... actions) {
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        this.setBorder(Borders.customLineBottom(JBColor.GRAY));
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        this.actions = actions;
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.POPUP, new DefaultActionGroup(actions), horizontal);
            toolbar.setTargetComponent(this);
            SwingUtilities.invokeLater(() -> this.add(toolbar.getComponent()));
        });
    }

}
