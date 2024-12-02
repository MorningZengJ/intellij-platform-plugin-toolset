package com.github.morningzeng.toolset.ui.game;

import com.github.morningzeng.toolset.utils.ColorUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.game.SudokuUtils;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI.Borders;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Enumeration;

/**
 * @author Morning Zeng
 * @since 2024-11-22
 */
@Slf4j
public final class SudokuComponent extends JBPanel<JBPanelWithEmptyText> {
    final int[][] sudoku = new SudokuUtils().generate();

    private final JBTable table = new SudokuTable();

    public SudokuComponent(final Project project) {


        this.initEvent();
        this.initLayout();
    }

    private void initEvent() {
        this.renderSudoku();
        this.setBorders();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                resize();
            }
        });
    }

    private void initLayout() {
        GridBagUtils.builder(this)
                .fill(GridBagUtils.GridBagFill.BOTH)
                .newRow(row -> row.newCell().add(this.table));
    }

    private void setBorders() {
        final Enumeration<TableColumn> columns = this.table.getColumnModel().getColumns();
        final TableCellRenderer tableCellRenderer = new TableCellRenderer();
        this.table.setDefaultRenderer(Object.class, tableCellRenderer);
    }

    void renderSudoku() {
        for (int i = 0; i < this.sudoku.length; i++) {
            for (int j = 0; j < this.sudoku[i].length; j++) {
                this.table.getModel().setValueAt(this.sudoku[i][j], i, j);
            }
        }
    }

    void resize() {
        final int height = this.getHeight();
        final int width = this.getWidth();
        final int firstBoxSize = Math.min(height, width);
        final Dimension dimension = new Dimension(firstBoxSize, firstBoxSize);
        this.table.setPreferredSize(dimension);
        this.table.setMinimumSize(dimension);
        this.table.setMaximumSize(dimension);
        this.table.setPreferredScrollableViewportSize(dimension);

        final int secondBoxSize = firstBoxSize / 3;

        final int thirdBoxSize = secondBoxSize / 3;
        if (thirdBoxSize > 1) {
            this.table.setRowHeight(thirdBoxSize);
        }

        // Note box size
        final int fourthBoxSize = secondBoxSize / 3;

    }

    static class TableCellRenderer extends JBLabel implements javax.swing.table.TableCellRenderer {
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

    private static class SudokuTable extends JBTable {

        public SudokuTable() {
            super(new DefaultTableModel(9, 9) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
            this.setDragEnabled(false);
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        @NotNull
        @Override
        public Component prepareRenderer(@NotNull javax.swing.table.TableCellRenderer renderer, int row, int column) {
            final Component result = super.prepareRenderer(renderer, row, column);
            if (result instanceof JBLabel label) {
                final int selectedRow = this.getSelectedRow();
                final int selectedColumn = this.getSelectedColumn();
                if (selectedRow == row && selectedColumn == column) {
                    final JBColor color = new JBColor(0x3D644F, 0x3D644F);
                    this.setColor(label, ColorUtils.invert(color), color);
                } else if (selectedRow == row || selectedColumn == column || this.sameGroup(row, column)) {
                    final JBColor color = new JBColor(0x365157, 0x365157);
                    this.setColor(label, ColorUtils.invert(color), color);
                } else {
                    this.setColor(label, JBColor.BLACK, JBColor.WHITE);
                }
            }
            return result;
        }

        boolean sameGroup(final int row, final int column) {
            final int selectedRow = this.getSelectedRow();
            final int selectedColumn = this.getSelectedColumn();
            if (selectedRow < 0 || selectedColumn < 0) {
                return false;
            }
            return selectedRow / 3 == row / 3 && selectedColumn / 3 == column / 3;
        }

        void setColor(final JBLabel label, JBColor foreground, final JBColor background) {
            if (label.getText().equals(this.getSelectedValue())) {
                foreground = JBColor.RED;
                label.setFont(label.getFont().deriveFont(JBFont.BOLD));
            }
            super.setForeground(foreground);
            label.setForeground(foreground);
            label.setBackground(background);
        }

        String getSelectedValue() {
            final int selectedRow = this.getSelectedRow();
            final int selectedColumn = this.getSelectedColumn();
            if (selectedRow < 0 || selectedColumn < 0) {
                return null;
            }
            return this.getValueAt(selectedRow, selectedColumn).toString();
        }
    }


}
