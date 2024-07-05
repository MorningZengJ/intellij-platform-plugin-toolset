package com.github.morningzeng.toolset.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.CompletionItem;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.annotations.ScratchConfig.OutputType;
import com.github.morningzeng.toolset.component.AbstractComponent.ComboBoxEditorTextField;
import com.github.morningzeng.toolset.component.ActionBar;
import com.github.morningzeng.toolset.component.CollapsibleTitledSeparator;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.component.Tree;
import com.github.morningzeng.toolset.enums.HttpBodyTypeEnum;
import com.github.morningzeng.toolset.model.HttpBean;
import com.github.morningzeng.toolset.model.HttpBean.RequestBean;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.CURLUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.JacksonUtils;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.google.common.collect.Maps;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.icons.AllIcons.ToolbarDecorator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-06-07
 */
@Slf4j
public final class HttpComponent extends JBPanel<JBPanelWithEmptyText> {
    private final Project project;

    private final Tree<HttpBean> requestTree = new Tree<>();
    private final Map<HttpBean, JBPanel<?>> components = Maps.newHashMap();

    public HttpComponent(final Project project) {
        this.project = project;
        final AnAction importAction = ActionUtils.drawerActions(
                "Import", "Import HTTP Request", ToolbarDecorator.Import,
                new CURLAction(project, this::getOrCreateHttpTabPanel),
                new PostmanAction(project)

        );
        final ActionBar actionBar = new ActionBar(this.addAction(), this.deleteAction(), importAction);
        final JBSplitter splitter = new JBSplitter(false, "http-tab-splitter", .05f, .3f);
        splitter.setDividerWidth(3);
        splitter.setFirstComponent(ScrollSupport.getInstance(this.requestTree).verticalAsNeededScrollPane());

        this.reloadScratchFile();
        splitter.setSecondComponent(new JBPanelWithEmptyText());

        this.requestTree.addTreeSelectionListener(e -> {
            final HttpBean selectedValue = this.requestTree.getSelectedValue();
            if (Objects.nonNull(selectedValue)) {
                final JBPanel<?> panel = this.components.get(selectedValue);
                if (Objects.nonNull(panel)) {
                    splitter.setSecondComponent(panel);
                }
            }
        });
        this.requestTree.setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
            final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            final HttpBean hb = (HttpBean) treeNode.getUserObject();
            if (Objects.isNull(hb)) {
                return null;
            }
            final Icon icon = Optional.ofNullable(hb.getRequest())
                    .map(RequestBean::methodIcon)
                    .orElse(Nodes.Folder);
            final String text = hb.getName();
            final JBLabel label = new JBLabel(text, icon, SwingConstants.CENTER);
            label.setIconTextGap(0);
            return label;
        });

        this.setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(actionBar)
                .newRow().fill(GridBag.BOTH).weightY(1).add(splitter);
    }

    private void reloadScratchFile() {
        this.requestTree.cleanUp(httpBeans -> httpBeans.forEach(
                httpBean -> Optional.ofNullable(this.components.remove(httpBean))
                        .filter(HttpTabPanel.class::isInstance)
                        .map(HttpTabPanel.class::cast)
                        .ifPresent(HttpTabPanel::release)
        ));
        ScratchFileUtils.childrenFile("HTTP", stream -> stream.filter(file -> !file.isDirectory())
                .sorted(
                        Comparator.comparing(VirtualFile::getName)
                                .thenComparing(VirtualFile::getPath)
                )
                .forEach(file -> {
                    final List<HttpBean> beans = ScratchFileUtils.read(file, OutputType.YAML, new TypeReference<>() {
                    });
                    this.requestTree.addNodes(beans);
                }));
    }

    private void getOrCreateHttpTabPanel(final HttpBean httpBean) {
        this.components.computeIfAbsent(httpBean, hb -> new HttpTabPanel(project).render(hb));
        this.requestTree.create(httpBean, true);
    }

    AnAction addAction() {
        return new AnAction("Add", "Add HTTP Request", IconC.ADD_GREEN) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                final HttpBean selectedValue = requestTree.getSelectedValue();

                final String requestName = Optional.ofNullable(selectedValue).map(HttpBean::getName).orElse("request");
                final String name = "%s#%s".formatted(requestName, requestTree.childrenCount() + 1);
                final HttpBean httpBean = HttpBean.builder()
                        .name(name)
                        .request(
                                RequestBean.builder().build()
                        )
                        .build();
                getOrCreateHttpTabPanel(httpBean);
            }
        };
    }

    AnAction deleteAction() {
        return new AnAction("Delete", "Delete HTTP Request", IconC.REMOVE_RED) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                if (requestTree.isSelectionEmpty()) {
                    return;
                }
                requestTree.delete(treeNodes -> {
                    for (final DefaultMutableTreeNode treeNode : treeNodes) {
                        final HttpBean httpBean = (HttpBean) treeNode.getUserObject();
                        Optional.ofNullable(components.remove(httpBean))
                                .filter(HttpTabPanel.class::isInstance)
                                .map(HttpTabPanel.class::cast)
                                .ifPresent(HttpTabPanel::release);
                    }
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
                        httpBeans.forEach(httpBean -> new HttpTabPanel(project).render(httpBean));
                    } catch (IOException ex) {
                        Messages.showMessageDialog(project, ex.getMessage(), "Import Error", Messages.getErrorIcon());
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

        public HttpTabPanel(final Project project) {
            this.project = project;
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
        }

        public HttpTabPanel render(final HttpBean httpBean) {
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
            return this;
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

        void release() {
            this.headersPanel.releaseEditor();
            this.bodyTextArea.releaseEditor();
            this.responseArea.releaseEditor();
        }

    }

}
