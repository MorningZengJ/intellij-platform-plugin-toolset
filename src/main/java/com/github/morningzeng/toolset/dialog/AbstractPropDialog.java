package com.github.morningzeng.toolset.dialog;

import com.beust.jcommander.internal.Maps;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.action.SingleTextFieldDialogAction;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.component.ActionBar;
import com.github.morningzeng.toolset.component.Tree;
import com.github.morningzeng.toolset.dialog.AbstractPropDialog.AbstractRightPanel;
import com.github.morningzeng.toolset.model.Children;
import com.github.morningzeng.toolset.proxy.InitializingBean;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.github.morningzeng.toolset.utils.ActionUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagBuilder;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-05-27
 */
@Slf4j
public abstract sealed class AbstractPropDialog<T extends Children<T>, P extends AbstractRightPanel<T>> extends DialogWrapper implements DialogSupport
        permits AsymmetricPropDialog, HashPropDialog, JWTPropDialog, SymmetricPropDialog {

    static final JBPanel<JBPanelWithEmptyText> EMPTY_PANEL = new JBPanelWithEmptyText();
    final Project project;
    final JBSplitter pane = new JBSplitter(false, "prop-dialog-splitter", .3f);
    final Tree<T> tree = new Tree<>();
    private final Consumer<List<T>> okAfterConsumer;
    private final Map<T, P> rightPanelMap = Maps.newHashMap();
    final AnAction addActions = ActionUtils.drawerActions("Add Item", "New create crypto prop item", IconC.ADD_DRAWER, this.initGroupAction());
    final ActionBar actionBar = new ActionBar(this.barActions());

    @SuppressWarnings("unused")
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
        this.tree.addTreeSelectionListener(e -> {
            final DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            if (Objects.nonNull(selectNode)) {
                final T value = tree.getNodeValue(selectNode);
                if (!this.enabledNode().test(value)) {
                    this.tree.clearSelection();
                    this.pane.setSecondComponent(EMPTY_PANEL);
                    return;
                }
            }
            this.defaultRightPanel();
        });
        this.tree.cellRenderer(prop -> {
            final JBLabel label = new JBLabel(prop.name(), prop.icon(), SwingConstants.LEFT);
            label.setEnabled(this.enabledNode().test(prop));
            return label;
        });
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

    Predicate<T> enabledNode() {
        return t -> true;
    }

    @Override
    public void delete() {
        this.tree.delete(treePaths -> this.defaultRightPanel());
    }

    @Override
    protected JComponent createCenterPanel() {
        this.createLeftPanel();
        this.defaultRightPanel();
        this.pane.setMinimumSize(new Dimension(700, 500));
        this.pane.setDividerWidth(3);
        return pane;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return Stream.concat(
                Stream.of(new DialogWrapperAction("Apply") {
                    @Override
                    protected void doAction(final ActionEvent e) {
                        applyProp();
                    }
                }),
                Stream.of(super.createActions())
        ).toArray(Action[]::new);
    }

    private void applyProp() {
        this.rightPanelMap.forEach(this::writeProp);
        this.tree.reloadTree((TreeNode) this.tree.getLastSelectedPathComponent());
        ScratchFileUtils.write(this.tree.data(), this.typeReference());
        this.okAfterConsumer.accept(this.tree.data());
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
                        defaultRightPanel();
                    }
                }
        };
    }

    abstract TypeReference<List<T>> typeReference();

    abstract T generateBean(final String name, final boolean isGroup);

    abstract void writeProp(final T prop, final P rightPanel);

    abstract P createRightItemPanel(final T t);

    void defaultRightPanel() {
        final T t = this.tree.getSelectedValue();
        if (Objects.isNull(t)) {
            this.pane.setSecondComponent(EMPTY_PANEL);
            return;
        }
        final P rightPanel = this.rightPanelMap.computeIfAbsent(t, this::createRightItemPanel);
        this.pane.setSecondComponent(rightPanel);
    }

    void createLeftPanel() {
        final JBPanel<JBPanelWithEmptyText> leftPanel = new JBPanel<>();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(this.actionBar);

        // add scroll
        leftPanel.add(ScrollSupport.getInstance(this.tree).verticalAsNeededScrollPane());

        this.pane.setFirstComponent(leftPanel);
    }

    protected sealed static abstract class AbstractRightPanel<T extends Children<T>> extends JBPanel<JBPanelWithEmptyText> implements InitializingBean
            permits AsymmetricPropDialog.RightPanel, HashPropDialog.RightPanel, JWTPropDialog.RightPanel, SymmetricPropDialog.RightPanel {
        private static final JBPanel<JBPanelWithEmptyText> EMPTY_PANEL = new JBPanelWithEmptyText();

        protected final LabelTextField titleTextField = new LabelTextField("Title");
        protected T prop;

        protected AbstractRightPanel(final T prop) {
            this.prop = prop;
        }

        @Override
        public void afterPropertiesSet() {
            this.titleTextField.setText(prop.name());
            final GridBagBuilder<AbstractRightPanel<T>> builder = GridBagUtils.builder(this);
            if (this.prop.isGroup()) {
                builder.newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                                .newCell().weightX(1).add(this.titleTextField))
                        .newRow(row -> row.fill(GridBagFill.BOTH)
                                .newCell().weightX(1).weightY(1).add(EMPTY_PANEL));
                return;
            }
            this.itemLayout().accept(builder);
        }

        protected abstract Consumer<GridBagBuilder<AbstractRightPanel<T>>> itemLayout();
    }

}
