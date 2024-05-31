package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.config.JWTProp;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.dialog.JWTPropDialog;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;

import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Comparator;

/**
 * @author Morning Zeng
 * @since 2024-05-29
 */
public class JWTComponent extends JBPanel<JBPanelWithEmptyText> {

    final LocalConfigFactory STATE_FACTORY = LocalConfigFactory.getInstance();
    private final Project project;

    private final ComboBox<JWTProp> signKeyComboBox = new ComboBox<>(STATE_FACTORY.jwtPropsMap().values().stream()
            .flatMap(Collection::stream)
            .sorted(Comparator.comparing(JWTProp::getSorted))
            .toArray(JWTProp[]::new));
    private final JButton jwtPropManageBtn = new JButton(General.Ellipsis);

    public JWTComponent(final Project project) {
        this.project = project;
        this.setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.signKeyComboBox)
                .newCell().weightX(0).add(this.jwtPropManageBtn);
        this.initEvent();
    }

    void initEvent() {
        this.jwtPropManageBtn.addActionListener(e -> {
            final JWTPropDialog dialog = new JWTPropDialog(this.project);
            dialog.showAndGet();
            this.refresh();
        });
    }

    void refresh() {
        this.signKeyComboBox.removeAllItems();
        STATE_FACTORY.jwtPropsMap().values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(JWTProp::getSorted))
                .forEach(this.signKeyComboBox::addItem);
    }
}
