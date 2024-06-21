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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.net.HTTPMethod;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI.Borders;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-06-07
 */
@Slf4j
public final class HttpComponent extends JBPanel<JBPanelWithEmptyText> {

    private final Project project;

    private final OkHttpClient httpClient = new OkHttpClient();

    private final AnAction importAction;
    private final ActionBar actionBar;
    private final ComboBoxEditorTextField<HTTPMethod> urlBar;

    private final CollapsibleTitledSeparator requestParamSeparator = new CollapsibleTitledSeparator("Request Parameter");
    private final JBTabbedPane requestParamTabPane;
    private final LanguageTextArea headersPanel;

    private final JBList<String> bodyLists = new JBList<>("Form Data", "X-www-Form-Urlencoded", "Raw");
    private final ComboBox<MediaType> mediaTypeComboBox = new ComboBox<>(new MediaType[]{
            MediaType.parse("application/json"),
            MediaType.parse("application/xml")
    });
    private final JBSplitter bodySplitter;

    private final LanguageTextArea responseArea;

    public HttpComponent(final Project project) {
        this.project = project;
        this.importAction = ActionUtils.drawerActions(
                "Import", "Import HTTP Request", ToolbarDecorator.Import, this.importCurl()
        );
        this.actionBar = new ActionBar(this.importAction);
        this.urlBar = new ComboBoxEditorTextField<>("Enter URL or paste text", this.executeBtn(), HTTPMethod.values());

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

    private JButton executeBtn() {
        final JButton execute = new JButton("execute", Actions.Execute);
        execute.addActionListener(e -> {
            final RequestBody requestBody = this.requestBody();
            final Headers headers = Headers.of(
                    this.headersPanel.getText().lines()
                            .<String[]>mapMulti((line, consumer) -> consumer.accept(line.split(": ")))
                            .collect(Collectors.toMap(h -> h[0], h -> h[1]))
            );
            final Request request = new Builder()
                    .url(this.urlBar.getText())
                    .method(this.urlBar.getItem().name(), requestBody)
                    .headers(headers)
                    .build();
            try (final Response response = this.httpClient.newCall(request).execute()) {
                final String result = response.body().string();
                this.responseArea.setText(result);
            } catch (IOException ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Request Error", Messages.getErrorIcon());
            }
        });
        return execute;
    }

    private RequestBody requestBody() {
        final LanguageTextArea bodyTextArea = (LanguageTextArea) this.bodySplitter.getSecondComponent();
        final HTTPMethod httpMethod = this.urlBar.getItem();
        if (httpMethod == HTTPMethod.GET || httpMethod == HTTPMethod.HEAD) {
            return null;
        }
        return switch (this.bodyLists.getSelectedValue()) {
            case "Form Data", "X-www-Form-Urlencoded" -> {
                final FormBody.Builder builder = new FormBody.Builder();
                bodyTextArea.getText().lines()
                        .forEach(line -> {
                            final String[] split = line.split(": ");
                            builder.add(split[0], split[1]);
                        });
                yield builder.build();
            }
            case "Raw" -> RequestBody.create(bodyTextArea.getText(), mediaTypeComboBox.getItem());
            default -> null;
        };
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
                    public void doCancelAction() {
                        super.doCancelAction();
                        this.textArea.releaseEditor();
                    }

                    @Override
                    protected void doOKAction() {
                        try {
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
                                    bodyTextArea.setText(
                                            Arrays.stream(split)
                                                    .map(sl -> String.join(": ", sl.split("=")))
                                                    .collect(Collectors.joining("\n"))
                                    );
                                }

                            });
                            final String headers = this.headers.entrySet().stream()
                                    .map(entry -> String.join(": ", entry.getKey(), entry.getValue()))
                                    .collect(Collectors.joining("\n"));
                            headersPanel.setText(headers);

                            final String contentType = this.headers.get("Content-Type");
                            if (StringUtil.isEmpty(contentType)) {
                                super.doOKAction();
                                return;
                            }
                            switch (contentType) {
                                case "multipart/form-data" -> bodyLists.setSelectedValue("Form Data", true);
                                case "application/x-www-form-urlencoded" ->
                                        bodyLists.setSelectedValue("X-www-Form-Urlencoded", true);
                                case "application/json" -> {
                                    bodyLists.setSelectedValue("Raw", true);
                                    mediaTypeComboBox.setSelectedIndex(0);
                                }
                                case "application/xml" -> {
                                    bodyLists.setSelectedValue("Raw", true);
                                    mediaTypeComboBox.setSelectedIndex(1);
                                }
                                default -> bodyLists.setSelectedValue("Raw", true);
                            }
                            super.doOKAction();
                        } finally {
                            this.textArea.releaseEditor();
                        }
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
