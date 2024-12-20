package com.github.morningzeng.toolset.ui.game.sudoku;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.ActionBar;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI.Borders;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Optional;

class OperationPanel extends JBPanel<JBPanelWithEmptyText> {

    private final NumberPanel[] numberPanels = new NumberPanel[9];
    private final Runnable eraseRunnable;
    private final Runnable replayRunnable;
    private final Runnable noteRunnable;
    private final Runnable newRunnable;

    private final AnAction clearAction = new AnAction("Erase", "Erase selected column", IconC.INK_ERASER) {
        @Override
        public void actionPerformed(@NotNull final AnActionEvent e) {
            eraseRunnable.run();
        }
    };
    private final AnAction noteAction = new AnAction("Notes", "Take notes", IconC.STYLUS_NOTE) {
        @Override
        public void actionPerformed(@NotNull final AnActionEvent e) {
            noteRunnable.run();
        }
    };
    private final AnAction replayAction = new AnAction("Replay", "Replay this sudoku", IconC.AUTORENEW) {
        @Override
        public void actionPerformed(@NotNull final AnActionEvent e) {
            replayRunnable.run();
        }
    };
    private final AnAction newAction = new AnAction("New Sudoku", "New sudoku", IconC.GENERATE) {
        @Override
        public void actionPerformed(@NotNull final AnActionEvent e) {
            newRunnable.run();
        }
    };

    private int[] numberCount = new int[9];

    public OperationPanel(final Project project, final Runnable eraseRunnable, final Runnable replayRunnable, final Runnable noteRunnable, final Runnable newRunnable) {
        super();
        this.eraseRunnable = eraseRunnable;
        this.replayRunnable = replayRunnable;
        this.noteRunnable = noteRunnable;
        this.newRunnable = newRunnable;
        this.initLayout();
    }

    private void initLayout() {
        final Dimension dimension = new Dimension(410, 150);
        this.setMaximumSize(dimension);
        this.setMinimumSize(dimension);
        this.setPreferredSize(dimension);
        GridBagUtils.builder(this)
                .fill(GridBagFill.HORIZONTAL)
                .newRow(row -> {
                    final ActionBar actionBar = new ActionBar(this.clearAction, this.noteAction, this.replayAction, this.newAction);
                    actionBar.setSize(actionBar.getWidth(), 50);
                    actionBar.setBorder(Borders.empty());
                    row.newCell().add(actionBar);
                })
                .newRow(row -> {
                    final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
                    for (int i = 0; i < numberCount.length; i++) {
                        numberPanels[i] = new NumberPanel(i + 1, numberCount[i]);
                        panel.add(numberPanels[i]);
                        if (i < numberCount.length - 1) {
                            panel.add(JBBox.createHorizontalStrut(5));
                        }
                    }
                    row.newCell().add(panel);
                });
    }

    void setNumberCount(final int[] numberCount) {
        this.numberCount = numberCount;
        for (int i = 0; i < this.numberCount.length; i++) {
            numberPanels[i].setCount(this.numberCount[i]);
        }
    }

    void minus(final int number) {
        Optional.of(number)
                .filter(val -> val > 0 && val < this.numberPanels.length + 1)
                .ifPresent(idx -> {
                    this.numberPanels[idx - 1].minus();
                    this.numberCount[idx - 1]--;
                });
    }

    void plus(final int number) {
        Optional.of(number)
                .filter(val -> val > 0 && val < this.numberPanels.length + 1)
                .ifPresent(idx -> {
                    this.numberPanels[idx - 1].plus();
                    this.numberCount[idx - 1]++;
                });
    }

    boolean complete() {
        return Arrays.stream(this.numberCount)
                .filter(val -> val > 0)
                .findAny()
                .isEmpty();
    }

}