package com.github.morningzeng.toolset.ui.game.sudoku;

import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.game.SudokuUtils;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import lombok.extern.slf4j.Slf4j;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;

/**
 * @author Morning Zeng
 * @since 2024-11-22
 */
@Slf4j
public final class SudokuComponent extends JBPanel<JBPanelWithEmptyText> {

    private final SudokuTable table;
    private final OperationPanel operationPanel;

    public SudokuComponent(final Project project) {
        super();
        this.table = new SudokuTable();
        this.operationPanel = new OperationPanel(project, this.table::eraseSelected, this.table::replay, this.table::switchNoteMode, this::renderSudoku);
        this.table.operationPanel(this.operationPanel);
        this.table.newSudoku(this::renderSudoku);

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
                .newRow(row -> row.newCell().add(this.table)
                        .newCell().add(this.operationPanel));
    }

    private void setBorders() {
        final TableCellRenderer tableCellRenderer = new TableCellRenderer();
        this.table.setDefaultRenderer(Object.class, tableCellRenderer);
    }

    void renderSudoku() {
        final int[][] origins = new SudokuUtils().generate();
        this.table.origins(origins);

        int[] count = new int[]{
                9, 9, 9, 9, 9, 9, 9, 9, 9
        };
        Arrays.stream(origins)
                .flatMapToInt(Arrays::stream)
                .forEach(value -> {
                    if (value > 0) {
                        count[value - 1]--;
                    }
                });
        this.operationPanel.setNumberCount(count);
    }

    void resize() {
        final int height = this.getHeight();
        final int width = this.getWidth();
        final int firstBoxSize = Math.min(height, width);
        if (firstBoxSize < 27) {
            return;
        }
        final Dimension dimension = new Dimension(firstBoxSize, firstBoxSize);
        this.table.setPreferredSize(dimension);
        this.table.setMinimumSize(dimension);
        this.table.setMaximumSize(dimension);
        this.table.setPreferredScrollableViewportSize(dimension);

        final int secondBoxSize = firstBoxSize / 3;

        final int thirdBoxSize = secondBoxSize / 3;
        this.table.setRowHeight(thirdBoxSize);
        this.table.resetNotePanelSize(thirdBoxSize);
    }

}
