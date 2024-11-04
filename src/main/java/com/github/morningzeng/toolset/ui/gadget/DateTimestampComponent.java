package com.github.morningzeng.toolset.ui.gadget;

import com.github.morningzeng.toolset.Constants.DateFormatterPreserve;
import com.github.morningzeng.toolset.Constants.DateFormatterPreserve.DateTimeFormatterPreserve;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.AbstractComponent.LabelTextField;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.utils.DialogWrapperUtils;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBDimension;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.GridBagLayout;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Morning Zeng
 * @since 2024-07-19
 */
public final class DateTimestampComponent extends JBPanel<JBPanelWithEmptyText> {

    private final AtomicLong timestamp;
    private final ZoneOffset defaultZoneOffset;

    private final LabelTextField presentTimestampTextField = new LabelTextField("Unix timestamps now: ");
    private final CollectionComboBoxModel<DateTimeFormatterPreserve> dateFormatterModel = new CollectionComboBoxModel<>(
            Lists.newArrayList(DateFormatterPreserve.YYYY_MM_DD_HH_MM_SS, DateTimeFormatterPreserve.EMPTY)
    );
    private final ComboBox<DateTimeFormatterPreserve> dateFormatterBox = new ComboBox<>(this.dateFormatterModel);
    private final ComboBox<ChronoUnit> timeUnitBox = new ComboBox<>(new ChronoUnit[]{ChronoUnit.SECONDS, ChronoUnit.MILLIS});
    private final EditorTextField datetimeTextField;
    private final EditorTextField timestampTextField;
    private final JButton covertToTimestampButton = new JButton("Convert to Timestamp", IconC.DOUBLE_ANGLES_RIGHT);
    private final JButton convertToDatetimeButton = new JButton("Convert to Datetime", IconC.DOUBLE_ANGLES_LEFT);

    public DateTimestampComponent(final Project project) {

        this.presentTimestampTextField.second().setEnabled(false);
        this.presentTimestampTextField.labelConsumer(label -> label.setPreferredSize(new JBDimension(200, label.getHeight())));
        this.datetimeTextField = new LanguageTextArea(project);
        this.datetimeTextField.setOneLineMode(true);
        this.datetimeTextField.setPlaceholder("Datetime in string format    eg: 2024-07-21 12:12:12");
        this.timestampTextField = new LanguageTextArea(project);
        this.timestampTextField.setOneLineMode(true);
        this.timestampTextField.setPlaceholder("Timestamp    eg: 1720671132");

        final LocalDateTime now = LocalDateTime.now();
        this.defaultZoneOffset = ZoneId.systemDefault().getRules().getOffset(now);
        this.timestamp = new AtomicLong(now.toEpochSecond(this.defaultZoneOffset));
        EdtExecutorService.getScheduledExecutorInstance().scheduleWithFixedDelay(
                () -> {
                    final long second = this.timestamp.incrementAndGet();
                    this.presentTimestampTextField.setText(String.valueOf(second));
                }, 1, 1, TimeUnit.SECONDS
        );

        this.render();
        this.initEvent();

        this.initDateFormatterBoxEditor();

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
            }
        });

        this.covertToTimestampButton.addActionListener(e -> {
            final DateTimeFormatter formatter = this.dateFormatterBox.getItem().formatter();
            final LocalDateTime dateTime = LocalDateTime.parse(this.datetimeTextField.getText(), formatter);
            final long timestamp = switch (this.timeUnitBox.getItem()) {
                case SECONDS -> dateTime.toEpochSecond(this.defaultZoneOffset);
                case MILLIS -> dateTime.toInstant(this.defaultZoneOffset).toEpochMilli();
                default -> throw new IllegalStateException("Unexpected value: " + this.timeUnitBox.getItem());
            };
            this.timestampTextField.setText(String.valueOf(timestamp));
        });
        this.convertToDatetimeButton.addActionListener(e -> {
            final DateTimeFormatter formatter = this.dateFormatterBox.getItem().formatter();
            final long timestamp = Long.parseLong(this.timestampTextField.getText());
            final LocalDateTime dateTime = switch (this.timeUnitBox.getItem()) {
                case SECONDS -> LocalDateTime.ofEpochSecond(timestamp, 0, this.defaultZoneOffset);
                case MILLIS ->
                        LocalDateTime.ofEpochSecond(timestamp / 1_000, (int) (timestamp % 1_000), this.defaultZoneOffset);
                default -> throw new IllegalStateException("Unexpected value: " + this.timeUnitBox.getItem());
            };
            this.datetimeTextField.setText(dateTime.format(formatter));
        });
    }

    private void initLayout() {
        this.setLayout(new GridBagLayout());

        GridLayoutUtils.builder()
                .container(this).fill(GridBag.NONE).gridWidth(4).add(this.presentTimestampTextField)
                .newRow().fill(GridBag.BOTH).gridHeight(3).weightX(.5).add(this.datetimeTextField)
                .newCell().weightX(0).add(this.dateFormatterBox)
                .newCell().add(this.timeUnitBox)
                .newCell().gridHeight(3).weightX(.5).add(this.timestampTextField)
                .newRow().gridX(1).gridWidth(2).gridHeight(1).add(this.covertToTimestampButton)
                .newRow().gridX(1).gridWidth(2).add(this.convertToDatetimeButton);
    }

    private void initDateFormatterBoxEditor() {
        this.dateFormatterBox.setEditable(true);
        this.dateFormatterBox.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                ExtendableTextField ecbEditor = new ExtendableTextField();
                ecbEditor.setBorder(null);
                return ecbEditor;
            }

            @Override
            public void setItem(final Object obj) {
                final DateTimeFormatterPreserve item = (DateTimeFormatterPreserve) obj;
                super.setItem(item.format());
            }

            @Override
            public Object getItem() {
                final Object obj = super.getItem();
                if (obj instanceof DateTimeFormatterPreserve item) {
                    return item;
                }
                return DateTimeFormatterPreserve.of(obj.toString());
            }
        });
    }

}
