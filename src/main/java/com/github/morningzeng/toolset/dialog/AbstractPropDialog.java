package com.github.morningzeng.toolset.dialog;

import com.github.morningzeng.toolset.component.DialogGroupAction;
import com.github.morningzeng.toolset.component.TreeComponent;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-05-27
 */
public abstract sealed class AbstractPropDialog extends DialogWrapper implements DialogSupport
        permits HashPropDialog, JWTPropDialog, SymmetricPropDialog {

    final Project project;
    final LocalConfigFactory STATE_FACTORY = LocalConfigFactory.getInstance();
    final Splitter pane = new Splitter(false, .3f);
    final JBPanel<JBPanelWithEmptyText> btnPanel = new DialogGroupAction(this, this.pane, this.initGroupAction());
    final TreeComponent tree = new TreeComponent();

    protected AbstractPropDialog(@Nullable final Project project) {
        super(project);
        this.project = project;
    }

    @Override
    public void delete() {
        this.tree.delete(treePaths -> this.createRightPanel(null));
    }

    @Override
    protected JComponent createCenterPanel() {
        this.createLeftPanel();
        this.createRightPanel(null);
        this.pane.setMinimumSize(new Dimension(700, 500));
        this.pane.setDividerWidth(1);
        return pane;
    }

    @Override
    protected void doOKAction() {
        this.saveConfig();
        super.doOKAction();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return Stream.concat(
                Stream.of(new DialogWrapperAction("Apply") {
                    @Override
                    protected void doAction(final ActionEvent e) {
                        saveConfig();
                    }
                }),
                Stream.of(super.createActions())
        ).toArray(Action[]::new);
    }

    abstract AnAction[] initGroupAction();

    abstract void saveConfig();

    abstract <T> void createRightPanel(final T t);

    JBPanel<JBPanelWithEmptyText> defaultRightPanel() {
        final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>();
        panel.setLayout(new GridBagLayout());
        this.pane.setSecondComponent(panel);
        return panel;
    }

    void createLeftPanel() {
        final JBPanel<JBPanelWithEmptyText> leftPanel = new JBPanel<>();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(this.btnPanel);

        // add scroll
        leftPanel.add(ScrollSupport.getInstance(this.tree).scrollPane());

        this.pane.setFirstComponent(leftPanel);
    }
}
