package com.github.morningzeng.toolset.ui.game.sudoku;

import com.github.morningzeng.toolset.model.PairVariable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBFont;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Getter
@Accessors(fluent = true)
class SudokuTable extends JBTable {

    private final Map<Integer, Set<Integer>> errorMap = Maps.newHashMap();
    private final Map<Integer, PairVariable<Integer, Integer>> valueIndexMap = Maps.newHashMap();
    private final Map<Integer, Map<Integer, NotePanel>> noteMap = Maps.newHashMap();

    @Setter
    private volatile JBColor valueSameForegroundColor = new JBColor(0x4D00FFFF, 0x4D00FFFF);
    @Setter
    private volatile JBColor valueSameBackgroundColor = new JBColor(0x804BFFC8, 0x804BFFC8);
    @Setter
    private volatile JBColor groupSameForegroundColor = new JBColor(0xDCCDFFFF, 0xDCCDFFFF);
    @Setter
    private volatile JBColor groupSameBackgroundColor = new JBColor(0x4BFFC822, 0x4BFFC822);
    @Setter
    private volatile JBColor fillColor = new JBColor(0xE41BD7D7, 0xE41BD7D7);
    @Setter
    private volatile JBColor errorColor = JBColor.RED;
    @Setter
    private volatile OperationPanel operationPanel;

    private int[][] origins = new int[9][9];
    private int[][] sudoku = new int[9][9];

    public SudokuTable() {
        super(new DefaultTableModel(9, 9) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        this.setDragEnabled(false);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent e) {
                final char keyChar = e.getKeyChar();
                if (keyChar < '1' || keyChar > '9') {
                    return;
                }
                final String value = getSelectedValue();
                if (Objects.isNull(value)) {
                    return;
                }
                final int row = getSelectedRow();
                final int column = getSelectedColumn();
                final int origin = origins[row][column];
                if (origin != 0) {
                    return;
                }
                final int digit = keyChar - '0';
                setValueAt(digit, row, column);
                sudoku[row][column] = digit;
                verifyUnique();
                callbackOperationPanel(digit, Integer.parseInt(value));
            }
        });
    }

    @NotNull
    @Override
    public Component prepareRenderer(@NotNull javax.swing.table.TableCellRenderer renderer, int row, int column) {
        final Component result = super.prepareRenderer(renderer, row, column);
        if (result instanceof JBLabel label) {
            final int selectedRow = this.getSelectedRow();
            final int selectedColumn = this.getSelectedColumn();
            if (selectedRow == row && selectedColumn == column) {
                this.setColor(label, this.valueSameForegroundColor, this.valueSameBackgroundColor, row, column);
            } else if (selectedRow == row || selectedColumn == column || this.sameGroup(row, column)) {
                this.setColor(label, this.groupSameForegroundColor, this.groupSameBackgroundColor, row, column);
            } else {
                this.setColor(label, JBColor.BLACK, JBColor.WHITE, row, column);
            }
        }
        return result;
    }

    protected void origins(final int[][] origins) {
        this.origins = origins;
        this.sudoku = new int[9][9];
        for (int i = 0; i < this.sudoku.length; i++) {
            for (int j = 0; j < this.sudoku[i].length; j++) {
                this.sudoku[i][j] = origins[i][j];
                this.getModel().setValueAt(this.sudoku[i][j], i, j);
            }
        }
        this.verifyUnique();
    }

    boolean sameGroup(final int row, final int column) {
        final int selectedRow = this.getSelectedRow();
        final int selectedColumn = this.getSelectedColumn();
        if (selectedRow < 0 || selectedColumn < 0) {
            return false;
        }
        return selectedRow / 3 == row / 3 && selectedColumn / 3 == column / 3;
    }

    void setColor(final JBLabel label, JBColor foreground, JBColor background, final int row, final int column) {
        if (label.getText().equals(this.getSelectedValue())) {
            background = this.valueSameForegroundColor;
        }
        if (row > -1 && column > -1) {
            if (this.origins[row][column] == 0) {
                label.setFont(label.getFont().deriveFont(JBFont.PLAIN));
                foreground = this.fillColor;
            } else {
                label.setFont(label.getFont().deriveFont(JBFont.BOLD));
            }
            if (this.errorMap.getOrDefault(row, Collections.emptySet()).contains(column)) {
                foreground = this.errorColor;
            }
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

    void verifyUnique() {
        this.errorMap.clear();
        for (int i = 0; i < 9; i++) {
            this.valueIndexMap.clear();
            for (int j = 0; j < 9; j++) {
                this.verifyUniqueValue(i, j);
            }
            this.valueIndexMap.clear();
            for (int j = 0; j < 9; j++) {
                this.verifyUniqueValue(j, i);
            }
        }
        this.verifyBlockUnique();
    }

    void verifyBlockUnique() {
        for (int blockRow = 0; blockRow < 3; blockRow++) {
            for (int blockCol = 0; blockCol < 3; blockCol++) {
                this.valueIndexMap.clear();
                for (int i = 0; i < 3; i++) {
                    // Iterate over the rows of a 3x3 block
                    for (int j = 0; j < 3; j++) {
                        // Iterate over the columns of a 3x3 block
                        this.verifyUniqueValue(blockRow * 3 + i, blockCol * 3 + j);
                    }
                }
            }
        }
    }

    void eraseSelected() {
        final int selectedRow = this.getSelectedRow();
        final int selectedColumn = this.getSelectedColumn();
        if (selectedRow < 0 || selectedColumn < 0) {
            return;
        }
        if (this.origins[selectedRow][selectedColumn] != 0) {
            return;
        }
        final int value = this.sudoku[selectedRow][selectedColumn];
        this.sudoku[selectedRow][selectedColumn] = 0;
        this.getModel().setValueAt(this.sudoku[selectedRow][selectedColumn], selectedRow, selectedColumn);
        this.verifyUnique();
        this.callbackOperationPanel(0, value);
    }

    void replay() {
        this.origins(this.origins);
    }

    private void verifyUniqueValue(final int i, final int j) {
        final int value = this.sudoku[i][j];
        if (value == 0) {
            return;
        }
        final PairVariable<Integer, Integer> pair = this.valueIndexMap.computeIfAbsent(value, val -> PairVariable.of(null, null));
        if (Objects.isNull(pair.key())) {
            pair.set(i, j);
        } else {
            this.errorMap.computeIfAbsent(i, key -> Sets.newHashSet()).add(j);
            this.errorMap.computeIfAbsent(pair.key(), key -> Sets.newHashSet()).add(pair.value());
        }
    }

    private void callbackOperationPanel(final int newValue, final int oldValue) {
        this.operationPanel.getNumberPanel(newValue).ifPresent(NumberPanel::minus);
        if (0 != oldValue) {
            this.operationPanel.getNumberPanel(oldValue).ifPresent(NumberPanel::plus);
        }
    }
}