package com.github.morningzeng.toolset.ui.game.sudoku;

import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI.Borders;

import java.awt.Dimension;

class NumberPanel extends JBPanel<JBPanelWithEmptyText> {
    private final JBLabel label = new JBLabel();
    private final JBLabel countLabel = new JBLabel();

    NumberPanel(final int text, final int count) {
        super();
        this.label.setText("" + text);
        this.countLabel.setText("" + count);
        final Dimension dimension = new Dimension(30, 50);
        this.setMaximumSize(dimension);
        this.setMinimumSize(dimension);
        this.setPreferredSize(dimension);

        GridBagUtils.builder(this)
                .fill(GridBagFill.HORIZONTAL)
                .newRow(row -> row.newCell().add(this.label))
                .newRow(row -> {
                    this.countLabel.setMaximumSize(new Dimension(30, 15));
                    this.countLabel.setFont(this.countLabel.getFont().deriveFont(8.0F));
                    this.countLabel.setLabelFor(this.label);
                    row.newCell().add(this.countLabel);
                });
        this.setBorder(Borders.customLine(JBColor.GRAY, 1));
    }

    void minus() {
        this.countLabel.setText(String.valueOf(Integer.parseInt(countLabel.getText()) - 1));
        this.setVisible();
    }

    void plus() {
        this.countLabel.setText(String.valueOf(Integer.parseInt(countLabel.getText()) + 1));
        this.setVisible();
    }

    void setCount(final int count) {
        this.countLabel.setText(String.valueOf(count));
        this.setVisible();
    }

    void setVisible() {
        final boolean visible = Integer.parseInt(countLabel.getText()) > 0;
        this.label.setVisible(visible);
        this.countLabel.setVisible(visible);
        if (visible) {
            this.setBorder(Borders.customLine(JBColor.GRAY, 1));
        } else {
            this.setBorder(Borders.empty());
        }
    }

}