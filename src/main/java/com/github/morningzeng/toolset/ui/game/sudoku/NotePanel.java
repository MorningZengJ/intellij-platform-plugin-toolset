package com.github.morningzeng.toolset.ui.game.sudoku;

import com.intellij.ui.table.JBTable;

class NotePanel extends JBTable {

    NotePanel() {

        this.initEvent();
    }

    private void initEvent() {
        this.disabledSelected();
    }

    private void disabledSelected() {
        this.setCellSelectionEnabled(false);
        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(false);
    }

}