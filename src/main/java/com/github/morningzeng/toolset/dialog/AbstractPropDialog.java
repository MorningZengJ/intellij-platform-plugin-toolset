package com.github.morningzeng.toolset.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.ActionBar;
import com.github.morningzeng.toolset.component.Tree;
import com.github.morningzeng.toolset.model.Children;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-05-27
 */
public abstract sealed class AbstractPropDialog<T extends Children<T>> extends DialogWrapper implements DialogSupport
        permits AsymmetricPropDialog, HashPropDialog, JWTPropDialog, SymmetricPropDialog {

    final Project project;
    final JBSplitter pane = new JBSplitter(false, "prop-dialog-splitter", .3f);
    final Tree<T> tree = new Tree<>();
    final AnAction addActions = ActionUtils.drawerActions("Add Item", "New create crypto prop item", General.Add, this.initGroupAction());
    final ActionBar actionBar = new ActionBar(this.barActions());
    private final Consumer<List<T>> okAfterConsumer;

    protected AbstractPropDialog(@Nullable final Project project) {
        this(project, symmetricCryptoProps -> {
        });
    }

    protected AbstractPropDialog(@Nullable final Project project, final Consumer<List<T>> okAfterConsumer) {
        super(project);
        this.project = project;
        this.okAfterConsumer = okAfterConsumer;

        this.tree.clearSelectionIfClickedOutside();
        this.tree.setNodes(ScratchFileUtils.read(this.typeReference()), Children::isGroup);
        final JBPanel<JBPanelWithEmptyText> emptyPanel = new JBPanelWithEmptyText();
        this.tree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            Optional.ofNullable(selectNode)
                    .filter(Predicate.not(DefaultMutableTreeNode::getAllowsChildren))
                    .ifPresentOrElse(userObject -> {
                        final T t = this.tree.getSelectedValue();
                        this.createRightPanel(t);
                    }, () -> this.pane.setSecondComponent(emptyPanel));
        });
        this.tree.cellRenderer(prop -> new JBLabel(prop.name(), prop.icon(), SwingConstants.LEFT));
        this.actionBar.setLayout(new BoxLayout(this.actionBar, BoxLayout.LINE_AXIS));
    }

    AnAction[] barActions() {
        return new AnAction[]{this.addActions, this.deleteAction()};
    }

    AnAction deleteAction() {
        return new AnAction(IconC.REMOVE_RED) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                delete();
            }
        };
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
        this.writeProp();
        ScratchFileUtils.write(this.tree.data(), this.typeReference());
        this.okAfterConsumer.accept(this.tree.data());
        super.doOKAction();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return Stream.concat(
                Stream.of(new DialogWrapperAction("Apply") {
                    @Override
                    protected void doAction(final ActionEvent e) {
                        okAfterConsumer.accept(tree.data());
                    }
                }),
                Stream.of(super.createActions())
        ).toArray(Action[]::new);
    }

    AnAction[] initGroupAction() {
        return new AnAction[]{
                new SingleTextFieldDialogAction(
                        "Group", "Add Group", name -> this.tree.create(this.generateBean(name, true), true)
                ) {
                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return Optional.of(super.getActionUpdateThread())
                                .filter(ActionUpdateThread.BGT::equals)
                                .orElse(ActionUpdateThread.EDT);
                    }

                    @Override
                    public void update(@NotNull final AnActionEvent e) {
                        e.getPresentation().setEnabled(tree.isSelectionEmpty());
                    }
                },
                new AnAction("KeyPair") {
                    @Override
                    public void actionPerformed(@NotNull final AnActionEvent e) {
                        final T cryptoProp = generateBean("", false);
                        tree.create(cryptoProp, true);
                        createRightPanel(cryptoProp);
                    }
                }
        };
    }

    abstract TypeReference<List<T>> typeReference();

    abstract T generateBean(final String name, final boolean isGroup);

    abstract void writeProp();

    abstract void createRightPanel(final T t);

    JBPanel<JBPanelWithEmptyText> defaultRightPanel() {
        final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>();
        panel.setLayout(new GridBagLayout());
        this.pane.setSecondComponent(panel);
        return panel;
    }

    void createLeftPanel() {
        final JBPanel<JBPanelWithEmptyText> leftPanel = new JBPanel<>();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(this.actionBar);

        // add scroll
        leftPanel.add(ScrollSupport.getInstance(this.tree).verticalAsNeededScrollPane());

        this.pane.setFirstComponent(leftPanel);
    }
}
