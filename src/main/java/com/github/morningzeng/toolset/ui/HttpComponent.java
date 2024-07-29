package com.github.morningzeng.toolset.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants;
import com.github.morningzeng.toolset.Constants.CompletionItem;
import com.github.morningzeng.toolset.component.AbstractComponent.ComboBoxEditorTextField;
import com.github.morningzeng.toolset.component.CollapsibleTitledSeparator;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.enums.HttpBodyTypeEnum;
import com.github.morningzeng.toolset.model.HttpBean;
import com.github.morningzeng.toolset.model.HttpBean.BodyBean;
import com.github.morningzeng.toolset.model.HttpBean.HttpBeanBuilder;
import com.github.morningzeng.toolset.model.HttpBean.RequestBean;
import com.github.morningzeng.toolset.model.Pair;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.CURLUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.JacksonUtils;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.ToolbarDecorator;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-06-07
 */
@Slf4j
public final class HttpComponent extends AbstractTreePanelComponent<HttpBean> {

    public HttpComponent(final Project project) {
        super(project, HttpBean.class, "http-tab-splitter");
    }

    @Override
    AnAction[] actions() {
        final AnAction importAction = ActionUtils.drawerActions(
                "Import", "Import HTTP Request", ToolbarDecorator.Import,
                new CURLAction(project, this::getOrCreatePanel),
                new PostmanAction(project)
        );
        return new AnAction[]{
                this.addAction(), this.deleteAction(), importAction, this.copyAction(), this.saveAllAction(), this.saveFileAction(), this.reloadFileAction()
        };
    }

    @Override
    JBPanel<?> childPanel(final HttpBean httpBean) {
        return new HttpTabPanel(project, httpBean);
    }

    @Override
    String configFileDirectory() {
        return "HTTP";
    }

    @Override
    HttpBean generateBean(final String name, final boolean isGroup) {
        final HttpBeanBuilder<?, ?> builder = HttpBean.builder().name(name);
        if (!isGroup) {
            builder.request(RequestBean.builder().build());
        }
        return builder.build();
    }

    private void getOrCreatePanel(final HttpBean httpBean) {
        this.getOrCreatePanel(httpBean, true);
    }

    AnAction copyAction() {
        return new AnAction("Convert to CURL and Copy", "Convert to cURL and Copy", Actions.Copy) {
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return Optional.of(super.getActionUpdateThread())
                        .filter(ActionUpdateThread.BGT::equals)
                        .orElse(ActionUpdateThread.EDT);
            }

            @Override
            public void update(@NotNull final AnActionEvent e) {
                //noinspection ConstantValue
                Optional.ofNullable(tree.getSelectedValue())
                        .map(HttpBean::getRequest).filter(Objects::nonNull)
                        .ifPresentOrElse(httpBean -> e.getPresentation().setEnabled(true), () -> e.getPresentation().setEnabled(false));
            }

            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                Optional.ofNullable(tree.getSelectedValue())
                        .ifPresent(httpBean -> {
                            final String cURL = CURLUtils.cURL(httpBean);
                            final String title = "Convert `" + httpBean.getName() + "` to CURL";
                            Messages.showMessageDialog(cURL, title, httpBean.getRequest().methodIcon());
                        });
            }
        };
    }

    static class CURLAction extends AnAction {
        private final Project project;
        private final Consumer<HttpBean> consumer;

        CURLAction(final Project project, final Consumer<HttpBean> consumer) {
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
                        final HttpBean httpBean = CURLUtils.from(this.textArea.getText());
                        consumer.accept(httpBean);
                        this.textArea.releaseEditor();
                        super.doOKAction();
                    } catch (Exception e) {
                        Messages.showErrorDialog(e.getMessage(), "Import Error");
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

    static class PostmanAction extends AnAction {
        private final Project project;

        PostmanAction(final Project project) {
            super("Postman Collection...");
            this.project = project;
        }

        @Override
        public void actionPerformed(@NotNull final AnActionEvent e) {
            new DialogWrapper(this.project) {
                final TextFieldWithBrowseButton browseButton = new TextFieldWithBrowseButton(new JBTextField());

                {
                    this.browseButton.setPreferredSize(new Dimension(750, this.browseButton.getHeight()));
                    this.browseButton.addBrowseFolderListener(
                            "Select Postman Collection File", "Select postman collection file", project,
                            FileChooserDescriptorFactory.createSingleFileDescriptor()
                    );
                    init();
                    setTitle("Import Postman Collection");
                }

                @Override
                protected JComponent createCenterPanel() {
                    this.browseButton.setToolTipText("Postman Collection File ");
                    return this.browseButton;
                }

                @Override
                protected void doOKAction() {
                    try {
                        final String filepath = this.browseButton.getText();
                        final String content = Files.readString(Path.of(filepath));
                        final List<HttpBean> httpBeans = JacksonUtils.IGNORE_TRANSIENT_AND_NULL.fromJson(content, new TypeReference<>() {
                        });
                        httpBeans.forEach(httpBean -> new HttpTabPanel(project, httpBean));
                    } catch (Exception ex) {
                        Messages.showErrorDialog(ex.getMessage(), "Import Error");
                    } finally {
                        super.doOKAction();
                    }
                }

                @Override
                protected @NotNull Action getOKAction() {
                    final Action okAction = super.getOKAction();
                    okAction.putValue(Action.NAME, "Import");
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

        private final JBList<HttpBodyTypeEnum> bodyLists = new JBList<>(HttpBodyTypeEnum.values());
        private final ComboBox<MediaType> mediaTypeComboBox = new ComboBox<>(new MediaType[]{
                MediaType.parse("application/json"),
                MediaType.parse("application/xml")
        });
        private final LanguageTextArea bodyTextArea;

        private final LanguageTextArea responseArea;
        private final HttpBean httpBean;

        public HttpTabPanel(final Project project, final HttpBean httpBean) {
            this.project = project;
            this.httpBean = httpBean;
            this.urlBar = new ComboBoxEditorTextField<>("Enter URL or paste text", this.executeBtn(), HTTPMethod.values());

            this.requestParamTabPane = new JBTabbedPane(JBTabbedPane.TOP);

            this.headersPanel = LanguageTextArea.create(this.project, PlainTextLanguage.INSTANCE, CompletionItem.HTTP_HEADERS, "");
            this.headersPanel.setPlaceholder("Enter HTTP Headers");
            this.headersPanel.setBorder(Borders.empty());

            final JBSplitter bodySplitter = new JBSplitter(false, "http-body-splitter", .3f);
            bodySplitter.setDividerWidth(3);
            bodySplitter.setFirstComponent(ScrollSupport.getInstance(this.bodyLists).verticalAsNeededScrollPane());
            this.bodyTextArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
            bodySplitter.setSecondComponent(this.bodyTextArea);

            this.requestParamTabPane.addTab("Headers", this.headersPanel);
            this.requestParamTabPane.addTab("Body", bodySplitter);
            this.requestParamTabPane.setPreferredSize(new Dimension(this.headersPanel.getWidth(), 70));
            this.requestParamSeparator.addExpandedListener(this.requestParamTabPane::setVisible);

            this.responseArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "", true);
            this.responseArea.setPlaceholder("Show HTTP Response");

            this.initializeLayout();
            this.render();
            this.initEvent();
        }

        public void render() {
            Optional.ofNullable(httpBean.getRequest())
                    .ifPresent(request -> {
                        Optional.ofNullable(request.getUrl()).ifPresent(urlBean -> this.urlBar.setText(String.valueOf(urlBean)));
                        this.urlBar.setItem(request.method());

                        this.headersPanel.setText(request.headerText());
                        Optional.ofNullable(request.getBody())
                                .ifPresent(body -> {
                                    this.bodyLists.setSelectedValue(body.mode(), true);
                                    this.bodyTextArea.setText(body.bodyText());
                                });
                    });
        }

        private JButton executeBtn() {
            final JButton execute = new JButton("execute", Actions.Execute);
            execute.addActionListener(e -> {
                try {
                    final RequestBody requestBody = this.requestBody();
                    final Headers headers = Headers.of(
                            this.headersPanel.getText().lines()
                                    .<String[]>mapMulti((line, consumer) -> consumer.accept(line.split(": ?")))
                                    .collect(Collectors.toMap(h -> h[0], h -> h[1]))
                    );
                    final Request request = new Builder()
                            .url(this.urlBar.getText())
                            .method(this.urlBar.getItem().name(), requestBody)
                            .headers(headers)
                            .build();
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        try (final Response response = httpClient.newCall(request).execute()) {
                            final String result = response.body().string();
                            this.responseArea.setText(result);
                        } catch (IOException ex) {
                            Messages.showMessageDialog(this.project, ex.getMessage(), "Request Error", Messages.getErrorIcon());
                        }
                    });
                } catch (Exception exc) {
                    Messages.showMessageDialog(this.project, exc.getMessage(), "Request Error", Messages.getErrorIcon());
                }
            });
            return execute;
        }

        private RequestBody requestBody() {
            final HTTPMethod httpMethod = this.urlBar.getItem();
            if (httpMethod == HTTPMethod.GET || httpMethod == HTTPMethod.HEAD) {
                return null;
            }
            return switch (this.bodyLists.getSelectedValue()) {
                case FORM_DATA, X_WWW_FORM_URLENCODED -> {
                    final FormBody.Builder builder = new FormBody.Builder();
                    this.bodyTextArea.getText().lines()
                            .forEach(line -> {
                                final String[] split = line.split(": ?");
                                builder.add(split[0], split[1]);
                            });
                    yield builder.build();
                }
                case RAW -> RequestBody.create(this.bodyTextArea.getText(), this.mediaTypeComboBox.getItem());
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

        private void initEvent() {
            this.urlBar.first().addItemListener(e -> this.httpBean.getRequest().setMethod(this.urlBar.getItem().name()));
            this.urlBar.second().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(final FocusEvent e) {
                    httpBean.getRequest().url(urlBar.getText());
                }
            });
            this.headersPanel.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(final FocusEvent e) {
                    final List<Pair<String, String>> header = httpBean.getRequest().getHeader();
                    header.clear();
                    header.addAll(
                            headersPanel.getText().lines()
                                    .map(s -> {
                                        final String[] split = s.split(Constants.COLON_WITH_SPACE);
                                        return Pair.of(split[0], split[1]);
                                    })
                                    .toList()
                    );
                }
            });
            this.bodyLists.addListSelectionListener(e -> {
                final HttpBodyTypeEnum bodyType = this.bodyLists.getSelectedValue();
                this.httpBean.getRequest().getBody().setMode(bodyType.key());
            });
            this.bodyTextArea.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(final FocusEvent e) {
                    final BodyBean body = httpBean.getRequest().getBody();
                    body.bodyText(bodyTextArea.getText());
                }
            });
        }

        void release() {
            this.headersPanel.releaseEditor();
            this.bodyTextArea.releaseEditor();
            this.responseArea.releaseEditor();
        }

    }

}
