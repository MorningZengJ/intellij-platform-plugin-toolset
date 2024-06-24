package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.CompletionItem;
import com.github.morningzeng.toolset.Constants.IconC;
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
import com.intellij.ui.components.JBLabel;
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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-06-07
 */
@Slf4j
public final class HttpComponent extends JBPanel<JBPanelWithEmptyText> {
    private final Project project;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JBList<String> requestNames = new JBList<>(this.listModel);
    private final Map<String, HttpTabPanel> components = Maps.newHashMap();

    public HttpComponent(final Project project) {
        this.project = project;
        final AnAction importAction = ActionUtils.drawerActions(
                "Import", "Import HTTP Request", ToolbarDecorator.Import,
                new CURLAction(project, this::createHttpTabPanel)
        );
        final ActionBar actionBar = new ActionBar(this.addAction(), this.deleteAction(), importAction);
        final JBSplitter splitter = new JBSplitter(false, "http-tab-splitter", .05f, .25f);
        splitter.setDividerWidth(3);
        splitter.setFirstComponent(ScrollSupport.getInstance(this.requestNames).verticalAsNeededScrollPane());
        if (this.requestNames.isSelectionEmpty()) {
            splitter.setSecondComponent(new JBPanelWithEmptyText());
        } else {
            final HttpTabPanel component = this.components.get(this.requestNames.getSelectedValue());
            splitter.setSecondComponent(component);
        }

        this.requestNames.addListSelectionListener(e -> {
            final String chosenRequest = this.requestNames.getSelectedValue();
            splitter.setSecondComponent(components.get(chosenRequest));
        });
        this.requestNames.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            final String name = value.split("-")[0];
            return new JBLabel(name);
        });

        this.setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(actionBar)
                .newRow().fill(GridBag.BOTH).weightY(1).add(splitter);
    }

    AnAction addAction() {
        return new AnAction("Add", "Add HTTP Request", IconC.ADD_GREEN) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                final HttpTabPanel httpTabPanel = new HttpTabPanel(project);
                createHttpTabPanel(httpTabPanel);
            }
        };
    }

    AnAction deleteAction() {
        return new AnAction("Delete", "Delete HTTP Request", IconC.REMOVE_RED) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                if (requestNames.isSelectionEmpty()) {
                    return;
                }
                int index = requestNames.getSelectedIndex();
                final String item = requestNames.getSelectedValue();
                listModel.removeElement(item);
                final HttpTabPanel httpTabPanel = components.remove(item);
                httpTabPanel.release();
                final int itemsCount = requestNames.getItemsCount();
                if (itemsCount > 0) {
                    if (index == itemsCount) {
                        --index;
                    }
                    requestNames.setSelectedIndex(Math.max(index, 0));
                }
            }
        };
    }

    private void createHttpTabPanel(final HttpTabPanel httpTabPanel) {
        final String requestName = "request#%s-%s".formatted(requestNames.getItemsCount() + 1, System.currentTimeMillis());
        this.listModel.addElement(requestName);
        this.components.put(requestName, httpTabPanel);
        this.requestNames.setSelectedValue(requestName, true);
    }

    static class CURLAction extends AnAction {
        private final Project project;
        private final Consumer<HttpTabPanel> consumer;

        CURLAction(final Project project, final Consumer<HttpTabPanel> consumer) {
            super("CURL Command...");
            this.project = project;
            this.consumer = consumer;
        }

        @Override
        public void actionPerformed(@NotNull final AnActionEvent e) {
            new DialogWrapper(project) {
                final LanguageTextArea textArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");

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
                        final HttpTabPanel httpTabPanel = new HttpTabPanel(project, this.textArea);
                        consumer.accept(httpTabPanel);
                    } finally {
                        this.textArea.releaseEditor();
                        super.doOKAction();
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
    }

    static class HttpTabPanel extends JBPanel<JBPanelWithEmptyText> {
        static final OkHttpClient httpClient = new OkHttpClient();
        private final Project project;
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

        public HttpTabPanel(final Project project) {
            this.project = project;
            this.urlBar = new ComboBoxEditorTextField<>("Enter URL or paste text", this.executeBtn(), HTTPMethod.values());

            this.requestParamTabPane = new JBTabbedPane(JBTabbedPane.TOP);

            this.headersPanel = LanguageTextArea.create(this.project, PlainTextLanguage.INSTANCE, CompletionItem.HTTP_HEADERS, "");
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

        public HttpTabPanel(final Project project, final LanguageTextArea curlTextArea) {
            this(project);
            final Map<String, String> headerMap = Maps.newHashMap();
            final LanguageTextArea bodyTextArea = (LanguageTextArea) bodySplitter.getSecondComponent();
            final String curl = curlTextArea.getText().trim();
            if (StringUtil.isEmpty(curl) || !"curl".equalsIgnoreCase(curl.substring(0, 4))) {
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
                    headerMap.put(header[0].trim(), header[1].trim());
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
            final String headers = headerMap.entrySet().stream()
                    .map(entry -> String.join(": ", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"));
            headersPanel.setText(headers);

            final String contentType = headerMap.get("Content-Type");
            if (StringUtil.isEmpty(contentType)) {
                return;
            }
            switch (contentType) {
                case "multipart/form-data" -> bodyLists.setSelectedValue("Form Data", true);
                case "application/x-www-form-urlencoded" -> bodyLists.setSelectedValue("X-www-Form-Urlencoded", true);
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
        }

        private JButton executeBtn() {
            final JButton execute = new JButton("execute", Actions.Execute);
            execute.addActionListener(e -> {
                try {
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
                    try (final Response response = httpClient.newCall(request).execute()) {
                        final String result = response.body().string();
                        this.responseArea.setText(result);
                    } catch (IOException ex) {
                        Messages.showMessageDialog(this.project, ex.getMessage(), "Request Error", Messages.getErrorIcon());
                    }
                } catch (Exception exc) {
                    Messages.showMessageDialog(this.project, exc.getMessage(), "Request Error", Messages.getErrorIcon());
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

        private void initializeLayout() {
            this.setLayout(new GridBagLayout());

            GridLayoutUtils.builder()
                    .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.urlBar)
                    .newRow().add(this.requestParamSeparator)
                    .newRow().fill(GridBag.BOTH).weightY(.5).add(this.requestParamTabPane)
                    .newRow().weightY(1).add(this.responseArea);
        }

        void release() {
            this.headersPanel.releaseEditor();
            ((LanguageTextArea) this.bodySplitter.getSecondComponent()).releaseEditor();
            this.responseArea.releaseEditor();
        }

    }

}
