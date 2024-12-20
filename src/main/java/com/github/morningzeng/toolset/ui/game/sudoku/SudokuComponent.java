package com.github.morningzeng.toolset.ui.game.sudoku;

import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.github.morningzeng.toolset.utils.game.SudokuUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-11-22
 */
@Slf4j
public final class SudokuComponent extends JBPanel<JBPanelWithEmptyText> {

    private final Project project;
    private final SudokuTable table;
    private final OperationPanel operationPanel;
    private final LabeledComponent<ComboBox<LevelEnum>> levelComboBox = LabeledComponent.create(new ComboBox<>(LevelEnum.values()), "Level", BorderLayout.WEST);
    private volatile ToolWindowAnchor currentAnchor = null;

    public SudokuComponent(final Project project) {
        super();
        this.project = project;
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
        this.project.getMessageBus().connect().subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
            @Override
            public void stateChanged(@NotNull final ToolWindowManager toolWindowManager) {
                final ToolWindow toolset = toolWindowManager.getToolWindow("Toolset");
                if (Objects.nonNull(toolset) && toolset.isVisible()) {
                    final ToolWindowAnchor anchor = toolset.getAnchor();
                    if (!Objects.equals(currentAnchor, anchor)) {
                        currentAnchor = anchor;
                        if (anchor.equals(ToolWindowAnchor.RIGHT) || anchor.equals(ToolWindowAnchor.LEFT)) {
                            verticalLayout();
                        } else if (anchor.equals(ToolWindowAnchor.BOTTOM)) {
                            horizontalLayout();
                        }
                    }
                }
            }
        });
        this.levelComboBox.getComponent().addItemListener(e -> this.renderSudoku());
    }

    private void initLayout() {
        this.levelComboBox.getComponent().setRenderer(
                (list, value, index, isSelected, cellHasFocus) -> {
                    if (Objects.isNull(value)) {
                        return new JBLabel("");
                    }
                    return new JBLabel(value.getSymbol());
                }
        );
    }

    private void verticalLayout() {
        GridBagUtils.builder(this)
                .fill(GridBagFill.BOTH)
                .newRow(row -> row.newCell().add(this.table))
                .newRow(row -> row.newCell().add(this.operationPanel))
                .newRow(row -> row.newCell().add(this.levelComboBox));
    }

    private void horizontalLayout() {
        GridBagUtils.builder(this)
                .fill(GridBagFill.BOTH)
                .newRow(row -> {
                    final JBPanel<JBPanelWithEmptyText> panel = GridBagUtils.builder()
                            .newRow($row -> $row.newCell().add(this.operationPanel))
                            .newRow($row -> $row.newCell().add(this.levelComboBox))
                            .build();
                    row.newCell().add(this.table)
                            .newCell().add(panel);
                });
    }

    private void setBorders() {
        final TableCellRenderer tableCellRenderer = new TableCellRenderer();
        this.table.setDefaultRenderer(Object.class, tableCellRenderer);
    }

    void renderSudoku() {
        final SudokuUtils instance = SudokuUtils.getInstance();
        final int[][] origins = instance.generate(this.levelComboBox.getComponent().getItem().getFactor());
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
