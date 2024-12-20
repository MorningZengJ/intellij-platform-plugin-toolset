package com.github.morningzeng.toolset.ui.game.sudoku;

import com.intellij.ui.table.JBTable;

import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.util.Arrays;

class NotePanel extends JBTable {

    private final int[][] numbers = new int[3][3];

    NotePanel(final int number) {
        super(new DefaultTableModel(3, 3) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        this.setDefaultRenderer(Object.class, new TableCellRenderer());
        this.initEvent();
        this.switchNumber(number);
    }

    void switchNumber(final int number) {
        if (number > 0 && number < 10) {
            final int idx = number - 1;
            int row = idx / 3, col = idx % 3;
            if (this.numbers[row][col] == 0) {
                this.numbers[row][col] = number;
                this.setValueAt(number, row, col);
            } else {
                this.numbers[row][col] = 0;
                this.setValueAt(0, row, col);
            }
        }
    }

    void removeNumber(final int number) {
        if (number > 0 && number < 10) {
            final int idx = number - 1;
            int row = idx / 3, col = idx % 3;
            this.numbers[row][col] = 0;
            this.setValueAt(0, row, col);
        }
    }

    void resetSize(final int size) {
        final int width = this.getWidth();
        final int height = this.getHeight();
        if (width != size || height != size) {
            final Dimension dimension = new Dimension(size, size);
            this.setPreferredSize(dimension);
            this.setMinimumSize(dimension);
            this.setMaximumSize(dimension);
            this.setPreferredScrollableViewportSize(dimension);
            this.setRowHeight(size / 3);
        }
    }

    int[] nonZeroCount() {
        return Arrays.stream(this.numbers)
                .flatMapToInt(Arrays::stream)
                .filter(value -> value > 0)
                .toArray();
    }

    private void initEvent() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.getModel().setValueAt(this.numbers[i][j], i, j);
            }
        }
        this.disabledSelected();
    }

    private void disabledSelected() {
        this.setCellSelectionEnabled(false);
        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(false);
    }

}