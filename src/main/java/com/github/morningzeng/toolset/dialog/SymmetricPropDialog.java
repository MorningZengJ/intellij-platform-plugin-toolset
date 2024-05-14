package com.github.morningzeng.toolset.dialog;

import com.github.morningzeng.toolset.component.DialogGroupAction;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.config.LocalConfigFactory.SymmetricCryptoProp;
import com.github.morningzeng.toolset.listener.ContentBorderListener;
import com.github.morningzeng.toolset.ui.AESComponent;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.GridBag;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-05-13
 */
public final class SymmetricPropDialog extends DialogWrapper implements DialogSupport {

    final Splitter pane = new Splitter(false, .3f);
    final DefaultListModel<SymmetricCryptoProp> symmetricProps = new DefaultListModel<>();
    final JBPanel<JBPanelWithEmptyText> btnPanel;


    private final JBTextField titleTextField = new JBTextField(50);
    private final JBTextField keyTextField = new JBTextField(25);
    private final JBTextField ivTextField = new JBTextField(25);
    private final JBTextArea descTextArea = new JBTextArea(5, 50);
    private final JBList<SymmetricCryptoProp> jbList = new JBList<>();

    public SymmetricPropDialog(final Project project) {
        super(project);
        this.btnPanel = new DialogGroupAction(this, project.getComponent(AESComponent.class));
        this.btnPanel.setLayout(new BoxLayout(this.btnPanel, BoxLayout.LINE_AXIS));
        this.symmetricProps.addAll(LocalConfigFactory.getInstance().symmetricCryptos());
        init();
        setTitle("Symmetric Properties");
    }

    @Override
    protected JComponent createCenterPanel() {
        this.createLeftPanel();
        this.createRightPanel(null);
        this.pane.setMinimumSize(new Dimension(700, 500));
        return pane;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return Stream.concat(
                Stream.of(new DialogWrapperAction("Apply") {
                    @Override
                    protected void doAction(final ActionEvent e) {
                        SymmetricPropDialog.this.writeProp();
                        final Set<SymmetricCryptoProp> set = Sets.newHashSet(symmetricProps.elements().asIterator());
                        LocalConfigFactory.getInstance().symmetricCryptos(set);
                        LocalConfigFactory.getInstance().loadState(LocalConfigFactory.getInstance().getState());
                    }
                }),
                Stream.of(super.createActions())
        ).toArray(Action[]::new);
    }

    @Override
    public void create() {
        final SymmetricCryptoProp keyPairsProp = SymmetricCryptoProp.builder()
                .title("Key pairs")
                .build();
        this.symmetricProps.add(this.symmetricProps.size(), keyPairsProp);
        this.jbList.setSelectedIndex(this.symmetricProps.size() - 1);
        this.createRightPanel(keyPairsProp);
    }

    @Override
    public void delete() {
        final int index = this.jbList.getSelectedIndex();
        this.jbList.remove(index);
        this.symmetricProps.remove(index);
        this.jbList.setSelectedIndex(this.symmetricProps.size() - 1);
    }

    void writeProp() {
        final SymmetricCryptoProp cryptoProp = this.jbList.getSelectedValue();
        cryptoProp.setTitle(this.titleTextField.getText())
                .setKey(this.keyTextField.getText())
                .setIv(this.ivTextField.getText())
                .setDesc(this.descTextArea.getText());
    }

    void createLeftPanel() {
        final JBPanel<JBPanelWithEmptyText> leftPanel = new JBPanel<>();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(this.btnPanel);
        this.jbList.setModel(this.symmetricProps);
        this.jbList.setCellRenderer((l, value, index, isSelected, cellHasFocus) -> new JBLabel(value.getTitle()));
        this.jbList.addListSelectionListener(e -> this.createRightPanel(this.jbList.getSelectedValue()));
        leftPanel.add(this.jbList);
        this.pane.setFirstComponent(leftPanel);
    }

    public void createRightPanel(final SymmetricCryptoProp cryptoProp) {
        final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>(new GridBagLayout());
        this.pane.setSecondComponent(panel);

        if (Objects.isNull(cryptoProp)) {
            return;
        }

        this.titleTextField.setText(cryptoProp.getTitle());

        this.keyTextField.setText(cryptoProp.getKey());
        this.ivTextField.setText(cryptoProp.getIv());

        this.descTextArea.setText(cryptoProp.getDesc());
        this.descTextArea.addFocusListener(ContentBorderListener.builder().component(this.descTextArea).init());

        GridLayoutUtils.builder()
                .container(panel).fill(GridBag.HORIZONTAL).add(new JBLabel("Title"))
                .newCell().weightX(1).gridWidth(3).add(this.titleTextField)
                .newRow().add(new JBLabel("Key"))
                .newCell().weightX(.5).add(this.keyTextField)
                .newCell().weightX(0).add(new JBLabel("IV"))
                .newCell().weightX(.5).add(this.ivTextField)
                .newRow().add(new JBLabel("Desc"))
                .newCell().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(this.descTextArea);
    }

}
