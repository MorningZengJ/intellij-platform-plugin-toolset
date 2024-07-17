package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;

import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Optional;

public class DatePicker extends JBPanel<JBPanelWithEmptyText> {
    final Year thisYear = Year.now();

    final LocalDateTime dateTime;
    final JBIntSpinner yearPicker = new JBIntSpinner(thisYear.getValue(), thisYear.getValue(), 2099);
    final ComboBox<Month> monthPicker = new ComboBox<>(Month.values());
    final ComboBox<Integer> dayPicker = new ComboBox<>();

    final JBIntSpinner hourPicker = new JBIntSpinner(0, 0, 23);
    final JBIntSpinner minutePicker = new JBIntSpinner(0, 0, 59);
    final JBIntSpinner secondPicker = new JBIntSpinner(0, 0, 59);

    public DatePicker(final LocalDateTime dateTime) {
        super(new GridBagLayout());
        this.dateTime = Optional.ofNullable(dateTime).orElse(LocalDateTime.now());

        this.initEvent();

        this.yearPicker.setValue(this.dateTime.getYear());
        this.monthPicker.setSelectedItem(this.dateTime.getMonth());
        this.dayPicker.setSelectedItem(this.dateTime.getDayOfMonth());
        this.hourPicker.setValue(this.dateTime.getHour());
        this.minutePicker.setValue(this.dateTime.getMinute());
        this.secondPicker.setValue(this.dateTime.getSecond());

        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.dayPicker)
                .newCell().gridWidth(3).add(this.monthPicker)
                .newCell().gridX(2).add(this.yearPicker)
                .newRow().weightX(1).add(this.hourPicker)
                .newCell().add(new JBLabel(":", SwingConstants.CENTER))
                .newCell().add(this.minutePicker)
                .newCell().add(new JBLabel(":", SwingConstants.CENTER))
                .newCell().add(this.secondPicker);
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(
                yearPicker.getNumber(), monthPicker.getItem(), dayPicker.getItem(),
                hourPicker.getNumber(), minutePicker.getNumber(), secondPicker.getNumber()
        );
    }

    void initEvent() {
        this.monthPicker.addItemListener(e -> {
            this.dayPicker.removeAllItems();
            final Month month = this.monthPicker.getItem();
            final YearMonth yearMonth = YearMonth.of(yearPicker.getNumber(), month);
            for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
                this.dayPicker.addItem(i);
            }
        });
    }

}