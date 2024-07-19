package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.ActionBar;
import com.github.morningzeng.toolset.component.Tree;
import com.github.morningzeng.toolset.enums.OutputType;
import com.github.morningzeng.toolset.model.Children;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.github.morningzeng.toolset.ui.HttpComponent.HttpTabPanel;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils.GridLayoutUtilsBuilder;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.google.common.collect.Maps;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.GridBagLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Morning Zeng
 * @since 2024-07-15
 */
public abstract sealed class AbstractTreePanelComponent<T extends Children<T>> extends JBPanel<JBPanelWithEmptyText>
        permits HttpComponent, RemindsComponent {

    protected final Project project;
    protected final JBSplitter splitter;
    protected final Class<T> tClass;
    protected final Tree<T> tree = new Tree<>();
    protected final Map<T, JBPanel<?>> components = Maps.newHashMap();
    protected final Map<String, VirtualFile> virtualFileMap = Maps.newHashMap();

    public AbstractTreePanelComponent(final Project project, final Class<T> tClass, final String proportionKey) {
        this(project, tClass, proportionKey, true);
    }

    public AbstractTreePanelComponent(final Project project, final Class<T> tClass, final String proportionKey, final boolean needActionBar) {
        super(new GridBagLayout());
        this.project = project;
        this.tClass = tClass;

        this.splitter = new JBSplitter(false, proportionKey, .05f, .3f);
        this.splitter.setDividerWidth(3);
        this.splitter.setFirstComponent(ScrollSupport.getInstance(this.tree).verticalAsNeededScrollPane());
        this.splitter.setSecondComponent(new JBPanelWithEmptyText());


        final GridLayoutUtilsBuilder builder = GridLayoutUtils.builder().container(this);
        if (needActionBar) {
            builder.fill(GridBag.HORIZONTAL).weightX(1).add(new ActionBar(this.actions()))
                    .newRow();
        }
        builder.fill(GridBag.BOTH).weightX(1).weightY(1).add(this.splitter);

        this.initTree();
        this.reloadScratchFile();
    }

    void reloadScratchFile() {
        this.tree.clear(ts -> this.components.clear());
        try {
            ScratchFileUtils.childrenFile(this.configFileDirectory(), stream -> {
                final List<T> ts = stream.filter(file -> !file.isDirectory())
                        .sorted(
                                Comparator.comparing(VirtualFile::getPath)
                                        .thenComparing(VirtualFile::getName)
                        )
                        .map(file -> {
                            final T t = ScratchFileUtils.read(file, OutputType.YAML, this.tClass);
                            this.virtualFileMap.put(t.name(), file);
                            return t;
                        })
                        .toList();
                this.tree.addNodes(ts, Children::isGroup);
            });
        } catch (final Exception ex) {
            Messages.showErrorDialog(ex.getMessage(), "Failed Load");
        }
    }

    AnAction[] actions() {
        return new AnAction[]{this.addAction(), this.deleteAction(), this.saveAllAction(), this.saveFileAction(), this.reloadFileAction()};
    }

    AnAction addAction() {
        return ActionUtils.drawerActions(
                "Add", "Add Group And Item", IconC.ADD_DRAWER,
                new SingleTextFieldDialogAction(
                        "Group", "Add Group",
                        name -> this.getOrCreatePanel(this.generateBean(name, true), true)
                ),
                new SingleTextFieldDialogAction(
                        "Item", "Add Item",
                        name -> this.getOrCreatePanel(this.generateBean(name, false), true)
                )
        );
    }

    AnAction deleteAction() {
        return new AnAction("Delete", "Delete...", IconC.REMOVE_RED) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                if (tree.isSelectionEmpty()) {
                    return;
                }
                tree.delete(treeNodes -> {
                    for (final DefaultMutableTreeNode treeNode : treeNodes) {
                        //noinspection unchecked
                        final T t = (T) treeNode.getUserObject();
                        Optional.ofNullable(components.remove(t))
                                .filter(HttpTabPanel.class::isInstance)
                                .map(HttpTabPanel.class::cast)
                                .ifPresent(HttpTabPanel::release);
                        if (Objects.isNull(t.getParent())) {
                            virtualFileMap.remove(t.name());
                        }
                    }
                });
            }
        };
    }

    AnAction saveAllAction() {
        return new AnAction("Save All", "Save all to file", IconC.SAVE_ALL) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                ScratchFileUtils.childrenFile(configFileDirectory(), stream -> stream.filter(Predicate.not(VirtualFile::isDirectory))
                        .forEach(virtualFile -> {
                            final T t = ScratchFileUtils.read(virtualFile, OutputType.YAML, tClass);
                            if (!virtualFileMap.containsKey(t.name())) {
                                deleteVirtualFile(virtualFile);
                            }
                        }));
                final List<T> data = tree.data();
                data.forEach(t -> {
                    final VirtualFile virtualFile = virtualFileMap.computeIfAbsent(
                            t.name(), name -> ScratchFileUtils.findOrCreate(configFileDirectory(), OutputType.YAML.fullName(name))
                    );
                    ScratchFileUtils.write(virtualFile, OutputType.YAML, t);
                });
            }
        };
    }

    AnAction saveFileAction() {
        return new AnAction("Save", "Save to file", IconC.SAVE) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                T selectedValue = tree.getSelectedValue();
                if (Objects.isNull(selectedValue)) {
                    return;
                }
                while (selectedValue.getParent() != null) {
                    if (virtualFileMap.containsKey(selectedValue.name())) {
                        break;
                    }
                    selectedValue = selectedValue.getParent();
                }
                final VirtualFile virtualFile = virtualFileMap.computeIfAbsent(
                        selectedValue.name(), name -> ScratchFileUtils.findOrCreate(configFileDirectory(), OutputType.YAML.fullName(name))
                );
                ScratchFileUtils.write(virtualFile, OutputType.YAML, selectedValue);
            }
        };
    }

    AnAction reloadFileAction() {
        return new AnAction("Reload", "Reload from file", Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                reloadFileEvent(e);
            }
        };
    }

    void reloadFileEvent(final AnActionEvent e) {
        this.reloadScratchFile();
    }

    JBPanel<?> getOrCreatePanel(final T t, final boolean addTree) {
        if (addTree) {
            final T selectedValue = this.tree.getSelectedValue();
            if (Objects.nonNull(selectedValue) && selectedValue.isGroup()) {
                t.setParent(selectedValue);
            } else if (this.virtualFileMap.containsKey(t.name())) {
                Messages.showErrorDialog("%s already exists".formatted(t.name()), "Duplicate Name");
                return new JBPanelWithEmptyText();
            }
            this.tree.create(t, t.isGroup());
        }
        return this.components.computeIfAbsent(t, hb -> {
            if (t.isGroup()) {
                return new JBPanelWithEmptyText();
            }
            return this.childPanel(hb);
        });
    }

    abstract JBPanel<?> childPanel(final T t);

    abstract String configFileDirectory();

    abstract T generateBean(final String name, final boolean isGroup);

    private void initTree() {
        this.tree.clearSelectionIfClickedOutside();
        this.tree.addTreeSelectionListener(
                e -> Optional.ofNullable(this.tree.getSelectedValue())
                        .ifPresent(t -> splitter.setSecondComponent(this.getOrCreatePanel(t, false)))
        );
        this.tree.cellRenderer(t -> new JBLabel(t.name(), t.icon(), SwingConstants.LEFT));

    }

    private void deleteVirtualFile(final VirtualFile virtualFile) {
        try {
            ApplicationManager.getApplication().runWriteAction((ThrowableComputable<Void, Exception>) () -> {
                virtualFile.delete(null);
                return null;
            });
        } catch (Exception ex) {
            final Notification notification = new Notification("virtual-file-notify", "Failed to delete the file", ex.getMessage(), NotificationType.WARNING);
            notification.notify(project);
        }
    }
}
