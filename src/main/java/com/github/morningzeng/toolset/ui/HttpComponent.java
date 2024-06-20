package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.component.AbstractComponent.ComboBoxEditorTextField;
import com.github.morningzeng.toolset.component.ActionBar;
import com.github.morningzeng.toolset.component.CollapsibleTitledSeparator;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Maps;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.ToolbarDecorator;
import com.intellij.json.json5.Json5Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTabbedPane;
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
import java.util.Map;
import java.util.stream.Collectors;

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

    private final CollapsibleTitledSeparator requestParamSeparator = new CollapsibleTitledSeparator("Request Parameter");
    private final JBTabbedPane requestParamTabPane;
    private final LanguageTextArea headersPanel;

    private final JBList<String> bodyLists = new JBList<>("Form Data", "X-www-Form-Urlencoded", "Raw");
    private final JBSplitter bodySplitter;

    private final LanguageTextArea responseArea;

    public HttpComponent(final Project project) {
        this.project = project;
        this.importAction = ActionUtils.drawerActions(
                "Import", "Import HTTP Request", ToolbarDecorator.Import, this.importCurl()
        );
        this.actionBar = new ActionBar(this.importAction);
        this.urlBar = new ComboBoxEditorTextField<>("Enter URL or paste text", new JButton("execute", Actions.Execute), HTTPMethod.values());

        this.requestParamTabPane = new JBTabbedPane(JBTabbedPane.TOP);
        this.headersPanel = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.headersPanel.setPlaceholder("Enter HTTP Headers");
        this.headersPanel.setBorder(Borders.empty());

        this.bodySplitter = new JBSplitter(false, "http-body-splitter", .3f);
        this.bodySplitter.setDividerWidth(3);
        this.bodySplitter.setFirstComponent(ScrollSupport.getInstance(this.bodyLists).verticalAsNeededScrollPane());
        this.bodySplitter.setSecondComponent(new LanguageTextArea(PlainTextLanguage.INSTANCE, project, ""));

        this.requestParamTabPane.addTab("Headers", this.headersPanel);
        this.requestParamTabPane.addTab("Body", this.bodySplitter);
        this.requestParamTabPane.setPreferredSize(new Dimension(this.headersPanel.getWidth(), 70));
        this.requestParamSeparator.addExpandedListener(this.requestParamTabPane::setVisible);

        this.responseArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "", true);
        this.responseArea.setPlaceholder("Show HTTP Response");

        this.initializeLayout();
    }

    AnAction importCurl() {
        return new AnAction("CURL Command...") {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                new DialogWrapper(project) {
                    final LanguageTextArea textArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
                    private final Map<String, String> headers = Maps.newHashMap();

                    {
                        this.textArea.setPlaceholder("curl -i http://hostname/ip");
                        this.textArea.setPreferredSize(new Dimension(450, 200));
                        this.textArea.setLineNumbersShown(false);
                        init();
                        setTitle("Convert CURL to HTTP Request");
                    }

                    @Override
                    protected JComponent createCenterPanel() {
                        return this.textArea;
                    }

                    @Override
                    protected void doOKAction() {
                        final LanguageTextArea bodyTextArea = (LanguageTextArea) bodySplitter.getSecondComponent();
                        final String curl = this.textArea.getText().trim();
                        if (!"curl".equalsIgnoreCase(curl.substring(0, 4))) {
                            return;
                        }
                        curl.lines().forEach(line -> {
                            final String trim = line.trim();
                            if ("curl".equalsIgnoreCase(trim.substring(0, 4))) {
                                urlBar.setText(trim.split("'")[1]);
                            }
                            if ("-X".equalsIgnoreCase(trim.substring(0, 2))) {
                                urlBar.setItem(HTTPMethod.valueOf(trim.split("'")[1]));
                            }
                            if ("-H".equalsIgnoreCase(trim.substring(0, 2))) {
                                final String[] header = trim.split("'")[1].split(":");
                                this.headers.put(header[0].trim(), header[1].trim());
                            }
                            if ("-d".equalsIgnoreCase(trim.substring(0, 2))
                                    || "--data".equalsIgnoreCase(trim.substring(0, 6))
                                    || "--data-raw".equalsIgnoreCase(trim.substring(0, 10))) {
                                final String data = trim.split("'")[1];
                                bodyTextArea.setText(data);
                            }
                            if ("-F".equalsIgnoreCase(trim.substring(0, 2))
                                    || "--form".equalsIgnoreCase(trim.substring(0, 6))) {
                                final String data = trim.split("'")[1];
                                final String[] split = data.split(";");
                                bodyTextArea.setText(String.join("\n", split));
                            }

                        });
                        final String headers = this.headers.entrySet().stream()
                                .map(entry -> String.join(" ", entry.getKey(), entry.getValue()))
                                .collect(Collectors.joining("\n"));
                        headersPanel.setText(headers);

                        switch (this.headers.get("Content-Type")) {
                            case "multipart/form-data" -> {
                                bodyTextArea.setLanguage(PlainTextLanguage.INSTANCE);
                                bodyLists.setSelectedValue("Form Data", true);
                            }
                            case "application/x-www-form-urlencoded" -> {
                                bodyTextArea.setLanguage(PlainTextLanguage.INSTANCE);
                                bodyLists.setSelectedValue("X-www-Form-Urlencoded", true);
                            }
                            case "application/json" -> {
                                bodyTextArea.setLanguage(Json5Language.INSTANCE);
                                bodyLists.setSelectedValue("Raw", true);
                            }
                            case "application/xml" -> {
                                bodyTextArea.setLanguage(XMLLanguage.INSTANCE);
                                bodyLists.setSelectedValue("Raw", true);
                            }
                            default -> {
                                bodyTextArea.setLanguage(PlainTextLanguage.INSTANCE);
                                bodyLists.setSelectedValue("Raw", true);
                            }
                        }
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
                .newRow().add(this.requestParamSeparator)
                .newRow().fill(GridBag.BOTH).weightY(.5).add(this.requestParamTabPane)
                .newRow().weightY(1).add(this.responseArea);
    }

}
