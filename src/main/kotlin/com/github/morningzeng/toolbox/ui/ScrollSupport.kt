@file:Suppress("unused")

package com.github.morningzeng.toolbox.ui

import com.intellij.ui.components.JBScrollPane
import java.awt.Component

/**
 * @author Morning Zeng
 * @since 2025-05-20
 */
interface ScrollSupport<T : Component> {

    companion object {
        fun <T : Component> getInstance(component: T): ScrollSupport<T> {
            return object : ScrollSupport<T> {
                override fun component(): T = component
            }
        }
    }

    fun component(): T

    fun scrollPane(): JBScrollPane {
        val component: T = component()
        if (component is JBScrollPane) {
            return component as JBScrollPane
        }
        return JBScrollPane(component)
    }

    fun verticalAsNeededScrollPane(): JBScrollPane {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
    }

    fun verticalAlwaysScrollPane(): JBScrollPane {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS)
    }

    fun horizontalAsNeededScrollPane(): JBScrollPane {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, JBScrollPane.VERTICAL_SCROLLBAR_NEVER)
    }

    fun horizontalAlwaysScrollPane(): JBScrollPane {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, JBScrollPane.VERTICAL_SCROLLBAR_NEVER)
    }

    fun asNeedScrollPane(): JBScrollPane {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
    }

    fun alwaysScrollPane(): JBScrollPane {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS)
    }

    fun scrollPane(horizontalScrollBarPolicy: Int, verticalScrollBarPolicy: Int): JBScrollPane {
        val scrollPane = this.scrollPane()
        scrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy)
        scrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy)
        return scrollPane
    }

}