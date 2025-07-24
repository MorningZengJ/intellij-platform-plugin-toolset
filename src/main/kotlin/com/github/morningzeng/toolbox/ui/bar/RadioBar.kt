@file:Suppress("unused")

package com.github.morningzeng.toolbox.ui.bar

import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBRadioButton
import java.awt.event.ItemEvent
import javax.swing.BoxLayout
import javax.swing.ButtonGroup

/**
 * @author Morning Zeng
 * @since 2025-07-23
 */
class RadioBar<T>(vertical: Boolean, selected: T, items: List<T>) : JBPanel<JBPanelWithEmptyText>() {

    private val radioMap: Map<T, JBRadioButton>
    private val group: ButtonGroup = ButtonGroup()
    private var selected: T? = selected
    private val changeListeners: MutableList<(T?) -> Unit> = mutableListOf()

    constructor(vararg items: T) : this(false, items[0], items.toList())

    constructor(vertical: Boolean, selected: T, vararg items: T) : this(vertical, selected, items.toList())

    constructor(items: List<T>) : this(false, items[0], items.toList())

    constructor(vertical: Boolean, vararg items: T) : this(vertical, items[0], items.toList())

    constructor(selected: T, items: List<T>) : this(false, selected, items)

    init {
        this.layout = BoxLayout(this, if (vertical) BoxLayout.Y_AXIS else BoxLayout.X_AXIS)
        radioMap = items.associateWith {
            JBRadioButton(it.toString(), it == selected).also { xit ->
                add(xit)
                group.add(xit)
                xit.addActionListener { cit ->
                    fireChangeEvent(it)
                }
            }
        }
        radioMap.forEach { (t, radio) ->
            radio.addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    setSelected(t)
                }
            }
        }
    }

    fun getSelected(): T? {
        return selected
    }

    fun setSelected(selected: T?) {
        if (selected == this.selected) return
        this.selected?.let { radioMap[it]?.isSelected = false }
        radioMap[selected]?.also {
            it.isSelected = true
            this.selected = selected
        }
        fireChangeEvent(selected)
    }

    fun addChangeListener(listener: (T?) -> Unit) {
        changeListeners.add(listener)
    }

    fun removeChangeListener(listener: (T?) -> Unit) {
        changeListeners.remove(listener)
    }

    private fun fireChangeEvent(selected: T?) {
        changeListeners.forEach {
            try {
                it(selected)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        radioMap.values.forEach { it.isEnabled = enabled }
    }

}