package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.component.AbstractComponent.EditorTextFieldButton;
import com.github.morningzeng.toolset.component.ActionBar;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.ToolbarDecorator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JButton;
import java.awt.GridBagLayout;

/**
 * @author Morning Zeng
 * @since 2024-06-07
 */
@Slf4j
public final class HttpComponent extends JBPanel<JBPanelWithEmptyText> {

    private final Project project;

    private final AnAction importAction = ActionUtils.drawerActions("Import", "Import HTTP Request", ToolbarDecorator.Import);
    private final ActionBar actionBar = new ActionBar(this.importAction);
    private final EditorTextFieldButton urlBar = new EditorTextFieldButton("", new JButton("execute", Actions.Execute));

    public HttpComponent(final Project project) {
        this.project = project;

        this.setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.BOTH).weightX(1).weightY(0).add(this.actionBar)
                .newRow().weightX(1).add(this.urlBar);
    }

}
