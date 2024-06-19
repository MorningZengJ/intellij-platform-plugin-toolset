package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.component.AbstractComponent.ComboBoxEditorTextField;
import com.github.morningzeng.toolset.component.ActionBar;
import com.github.morningzeng.toolset.component.CollapsibleTitledSeparator;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.ToolbarDecorator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.net.HTTPMethod;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI.Borders;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.GridBagLayout;

/**
 * @author Morning Zeng
 * @since 2024-06-07
 */
@Slf4j
public final class HttpComponent extends JBPanel<JBPanelWithEmptyText> {

    private final Project project;

    private final AnAction importAction;
    private final ActionBar actionBar;
    private final ComboBoxEditorTextField<HTTPMethod> urlBar;

    private final CollapsibleTitledSeparator headersSeparator = new CollapsibleTitledSeparator("Headers");
    private final LanguageTextArea headersPanel;
    private final LanguageTextArea responseArea;

    public HttpComponent(final Project project) {
        this.project = project;
        this.importAction = ActionUtils.drawerActions(
                "Import", "Import HTTP Request", ToolbarDecorator.Import, this.importCurl()
        );
        this.actionBar = new ActionBar(this.importAction);
        this.urlBar = new ComboBoxEditorTextField<>("Enter URL or paste text", new JButton("execute", Actions.Execute), HTTPMethod.values());

        this.headersPanel = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.headersPanel.setPlaceholder("Enter HTTP Headers");
        this.responseArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "", true);
        this.responseArea.setPlaceholder("Show HTTP Response");

        this.headersPanel.setBorder(Borders.empty());
        this.headersPanel.setPreferredSize(new Dimension(this.headersPanel.getWidth(), 50));
        this.headersSeparator.addExpandedListener(this.headersPanel::setVisible);

        this.initializeLayout();
    }

    AnAction importCurl() {
        return new AnAction("CURL Command...") {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                new DialogWrapper(project) {
                    {
                        init();
                        setTitle("Convert CURL to HTTP Request");
                    }

                    @Override
                    protected JComponent createCenterPanel() {
                        final LanguageTextArea textArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
                        textArea.setPlaceholder("curl -i http://hostname/ip");
                        textArea.setPreferredSize(new Dimension(450, 200));
                        textArea.setLineNumbersShown(false);
                        return textArea;
                    }

                    @Override
                    protected void doOKAction() {
                        super.doOKAction();
                    }

                    @Override
                    protected @NotNull Action getOKAction() {
                        final Action okAction = super.getOKAction();
                        okAction.putValue(Action.NAME, "Convert");
                        return okAction;
                    }
                }.showAndGet();
            }
        };
    }

    private void initializeLayout() {
        this.setLayout(new GridBagLayout());

        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.actionBar)
                .newRow().weightX(1).add(this.urlBar)
                .newRow().add(this.headersSeparator)
                .newRow().fill(GridBag.BOTH).weightY(.5).add(this.headersPanel)
                .newRow().weightY(1).add(this.responseArea);
    }

}
