package com.github.morningzeng.toolbox.utils

import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.util.ui.GridBag
import java.awt.Component
import java.awt.Container
import java.awt.GridBagLayout

/**
 * @author Morning Zeng
 * @since 2025-05-20
 */
@Suppress("unused")
object GridBagUtils {

    fun builder(): GridBagBuilder<JBPanel<JBPanelWithEmptyText>> = builder(JBPanel<JBPanelWithEmptyText>())

    fun <T : Container> builder(container: T): GridBagBuilder<T> = container.let {
        it.layout = GridBagLayout()
        GridBagBuilder(it)
    }

    enum class GridBagFill(val fill: Int) {
        NONE(GridBag.NONE),
        BOTH(GridBag.BOTH),
        HORIZONTAL(GridBag.HORIZONTAL),
        VERTICAL(GridBag.VERTICAL),
    }

    sealed class AbstractGrid<T : Container>(
        protected var bag: GridBag
    ) {

        open fun fill(fill: GridBagFill): AbstractGrid<T> = this.apply { bag.fill = fill.fill }

    }

    class GridBagBuilder<T : Container>(val container: T) : AbstractGrid<T>(GridBag()) {

        fun build(): T {
            return container
        }

        fun row(consumer: (Row<T>) -> Unit): GridBagBuilder<T> {
            bag.gridx = 0
            bag.gridy++
            bag.weightx = 0.0
            bag.gridwidth = 1
            consumer(Row(this, bag))
            return this
        }

        override fun fill(fill: GridBagFill): GridBagBuilder<T> = this.apply { super.fill(fill) }
    }

    class Row<T : Container>(
        val builder: GridBagBuilder<T>,
        bag: GridBag
    ) : AbstractGrid<T>(bag) {

        override fun fill(fill: GridBagFill): Row<T> = this.apply { super.fill(fill) }

        fun gridY(gridY: Int): Row<T> = this.apply { bag.gridy = gridY }

        fun gridHeight(gridHeight: Int): Row<T> = this.apply { bag.gridheight = gridHeight }

        fun cell(): Cell<T> = this.bag.let {
            it.gridx++
            it.gridheight = 1
            Cell(this, bag)
        }
    }

    class Cell<T : Container>(
        val row: Row<T>,
        bag: GridBag
    ) : AbstractGrid<T>(bag) {

        override fun fill(fill: GridBagFill): Cell<T> = this.apply { super.fill(fill) }

        fun weightX(weightX: Double): Cell<T> = this.apply { bag.weightx = weightX }

        fun weightY(weightY: Double): Cell<T> = this.apply { bag.weighty = weightY }

        fun gridX(gridX: Int): Cell<T> = this.apply { bag.gridx = gridX }

        fun gridWidth(gridWidth: Int): Cell<T> = this.apply { bag.gridwidth = gridWidth }

        fun add(component: Component): Row<T> = this.row.also {
            it.builder.container.add(component, bag)
        }
    }

}