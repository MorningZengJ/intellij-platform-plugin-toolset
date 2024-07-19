package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.DateFormatterPreserve;
import com.github.morningzeng.toolset.Constants.DateFormatterPreserve.DateTimeFormatterPreserve;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.utils.DialogWrapperUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.GridBag;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-07-19
 */
public final class DateTimestamp extends JBPanel<JBPanelWithEmptyText> {

    private final Project project;

    private final List<DateTimeFormatterPreserve> formatterPreserves = Lists.newArrayList(
            DateFormatterPreserve.YYYY_MM_DD_HH_MM_SS, DateTimeFormatterPreserve.EMPTY
    );
    private final CollectionComboBoxModel<DateTimeFormatterPreserve> dateFormatterModel = new CollectionComboBoxModel<>(formatterPreserves);
    private final ComboBox<DateTimeFormatterPreserve> dateFormatterBox = new ComboBox<>(this.dateFormatterModel);

    public DateTimestamp(final Project project) {
        this.project = project;

        this.render();
        this.initEvent();

        this.initLayout();
    }

    private void render() {
        this.dateFormatterBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (Objects.isNull(value.format())) {
                return new JBLabel("Add date formatter...", IconC.ADD, SwingConstants.LEFT);
            }
            return new JBLabel(value.format());
        });
    }

    private void initEvent() {
        this.dateFormatterBox.addItemListener(e -> {
            if (DateTimeFormatterPreserve.EMPTY.equals(this.dateFormatterBox.getItem())) {
                try {
                    DialogWrapperUtils.singleTextField("Add date formatter", "Format", format -> {
                        this.dateFormatterBox.setSelectedIndex(-1);
                        final DateTimeFormatterPreserve item = DateTimeFormatterPreserve.of(format);
                        this.dateFormatterModel.add(this.dateFormatterBox.getItemCount() - 1, item);
                        this.dateFormatterBox.setSelectedItem(item);
                    });
                } catch (final Exception ex) {
                    Messages.showErrorDialog(ex.getMessage(), "Add Date Formatter Error");
                }
                return;
            }

        });
    }

    private void initLayout() {
        this.setLayout(new GridBagLayout());

        this.dateFormatterBox.setEditable(true);
        this.dateFormatterBox.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                ExtendableTextField ecbEditor = new ExtendableTextField();
                ecbEditor.setBorder(null);
                return ecbEditor;
            }
        });
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).add(this.dateFormatterBox);
    }

}
