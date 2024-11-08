package com.github.morningzeng.toolset.utils;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-14
 */
@Data
public class GridBagUtils {

    public static GridBagBuilder<JBPanel<JBPanelWithEmptyText>> builder() {
        final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>(new GridBagLayout());
        return builder(panel);
    }

    public static <T extends Container> GridBagBuilder<T> builder(final T container) {
        container.setLayout(new GridBagLayout());
        return new GridBagBuilder<T>().container(container);
    }

    @AllArgsConstructor
    public enum GridBagFill {
        NONE(GridBag.NONE),
        BOTH(GridBag.BOTH),
        HORIZONTAL(GridBag.HORIZONTAL),
        VERTICAL(GridBag.VERTICAL),
        ;

        private final int fill;
    }

    static sealed abstract class AbstractGrid<T extends Container> {
        final GridBag bag;

        AbstractGrid(final GridBag bag) {
            this.bag = bag;
        }

        @SuppressWarnings("UnusedReturnValue")
        AbstractGrid<T> fill(GridBagFill fill) {
            this.bag.fill = fill.fill;
            return this;
        }
    }

    @Accessors(fluent = true)
    public static final class GridBagBuilder<T extends Container> extends AbstractGrid<T> {

        @Setter(AccessLevel.PRIVATE)
        private T container;

        GridBagBuilder() {
            super(new GridBag());
        }

        public GridBagBuilder<T> newRow(final Consumer<Row<T>> rowConsumer) {
            this.bag.gridx = 0;
            this.bag.gridy++;
            this.bag.weightx = 0;
            this.bag.gridwidth = 1;
            final Row<T> row = new Row<>(this, this.bag);
            rowConsumer.accept(row);
            return this;
        }

        @Override
        public GridBagBuilder<T> fill(final GridBagFill fill) {
            super.fill(fill);
            return this;
        }

        public T build() {
            return this.container;
        }

    }

    public static final class Row<T extends Container> extends AbstractGrid<T> {

        private final GridBagBuilder<T> builder;

        Row(final GridBagBuilder<T> builder, final GridBag bag) {
            super(bag);
            this.builder = builder;
        }

        @Override
        public Row<T> fill(final GridBagFill fill) {
            super.fill(fill);
            return this;
        }

        public Row<T> gridY(final int gridY) {
            this.bag.gridy = gridY;
            return this;
        }

        public Row<T> gridHeight(final int gridHeight) {
            this.bag.gridheight = gridHeight;
            return this;
        }

        public Cell<T> newCell() {
            this.bag.gridx++;
            this.bag.gridheight = 1;
            return new Cell<>(this, this.bag);
        }

    }

    public static final class Cell<T extends Container> extends AbstractGrid<T> {
        private final Row<T> row;

        Cell(final Row<T> row, final GridBag bag) {
            super(bag);
            this.row = row;
        }

        @Override
        public Cell<T> fill(final GridBagFill fill) {
            super.fill(fill);
            return this;
        }

        public Cell<T> weightX(final double weightX) {
            this.bag.weightx = weightX;
            return this;
        }

        public Cell<T> weightY(final double weightY) {
            this.bag.weighty = weightY;
            return this;
        }

        public Cell<T> gridX(final int gridX) {
            this.bag.gridx = gridX;
            return this;
        }

        public Cell<T> gridWidth(final int gridWidth) {
            this.bag.gridwidth = gridWidth;
            return this;
        }

        public Row<T> add(final Component component) {
            this.row.builder.container.add(component, this.bag);
            return this.row;
        }

    }

}
