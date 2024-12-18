package com.github.morningzeng.toolset.ui.game.sudoku;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI.Borders;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import java.awt.Component;

class TableCellRenderer extends JBLabel implements javax.swing.table.TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (table == null) {
            return this;
        }
        this.setBorder(row, column);
        this.setFont(table.getFont());
        this.setValue(value);

        return this;
    }

    public void setValue(final Object value) {
        this.setHorizontalAlignment(SwingConstants.CENTER);
        if (value instanceof Integer val) {
            super.setText(val != 0 ? value.toString() : "");
        }
    }

    private void setBorder(final int row, final int column) {
        int top = 0, left = 0, bottom = 0, right = 0;
        switch (row % 3) {
            case 0 -> top = 2;
            case 2 -> bottom = 2;
        }
        switch (column % 3) {
            case 0 -> left = 2;
            case 2 -> right = 2;
        }
        this.setBorder(Borders.customLine(JBColor.gray, top, left, bottom, right));
    }
}