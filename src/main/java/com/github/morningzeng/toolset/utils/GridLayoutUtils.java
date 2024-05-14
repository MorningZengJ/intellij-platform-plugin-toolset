package com.github.morningzeng.toolset.utils;

import com.intellij.util.ui.GridBag;
import lombok.Builder;
import lombok.Data;

import java.awt.Component;
import java.awt.Container;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-05-14
 */
@Data
@Builder
public class GridLayoutUtils {

    private Container container;
    private int fill;

    public static class GridLayoutUtilsBuilder {
        private final GridBag gridBag = new GridBag();
        private int gridX;
        private int gridY;
        private double weightX;
        private double weightY;
        private int gridWidth = 1;
        private int gridHeight = 1;

        public GridLayoutUtilsBuilder add(final Component component) {
            assert Objects.nonNull(this.container);
            this.gridBag.fill = this.fill;
            this.gridBag.gridx = this.gridX;
            this.gridBag.gridy = this.gridY;
            this.gridBag.gridwidth = this.gridWidth;
            this.gridBag.gridheight = this.gridHeight;
            this.gridBag.weightx = this.weightX;
            this.gridBag.weighty = this.weightY;
            this.container.add(component, this.gridBag);
            return this;
        }

        public GridLayoutUtilsBuilder newCell() {
            this.gridX++;
            return this;
        }

        public GridLayoutUtilsBuilder newRow() {
            this.gridX = 0;
            this.weightX = 0;
            this.gridWidth = 1;
            this.gridY++;
            return this;
        }

        public GridLayoutUtilsBuilder weightX(final double weightX) {
            this.weightX = weightX;
            return this;
        }

        public GridLayoutUtilsBuilder weightY(final double weightY) {
            this.weightY = weightY;
            return this;
        }

        public GridLayoutUtilsBuilder gridWidth(final int gridWidth) {
            this.gridWidth = gridWidth;
            return this;
        }

        public GridLayoutUtilsBuilder gridHeight(final int gridHeight) {
            this.gridHeight = gridHeight;
            return this;
        }

    }

}
