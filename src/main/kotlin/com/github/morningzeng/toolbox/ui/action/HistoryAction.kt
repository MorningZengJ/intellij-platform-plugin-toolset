package com.github.morningzeng.toolbox.ui.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import javax.swing.Icon
import javax.swing.Timer

/**
 * @author Morning Zeng
 * @since 2025-07-29
 */
@Suppress("unused")
open class HistoryAction<T>(
    text: String? = null,
    icon: Icon? = null,
    description: String? = null,
) : AnAction(text, description, icon) {

    private val items: MutableList<T> = mutableListOf()
    private val itemClickedCallback: MutableList<(T) -> Unit> = mutableListOf()
    private val itemHoverCallback: MutableList<(T) -> Unit> = mutableListOf()
    private val popupClosedCallback: MutableList<() -> Unit> = mutableListOf()
    private val popupCanceledCallback: MutableList<() -> Unit> = mutableListOf()
    private var historyRender: (T) -> String = { it.toString() }

    override fun actionPerformed(e: AnActionEvent) {
        this.showPopup(e)
    }

    fun showPopup(e: AnActionEvent) {
        val popup = JBPopupFactory.getInstance().createListPopup(popupStep()).apply {
            addListener(object : JBPopupListener {
                override fun onClosed(event: LightweightWindowEvent) {
                    popupClosedCallback.forEach { it() }
                }
            })
        }

        popup.showInBestPositionFor(e.dataContext)
    }

    protected fun popupStep(): BaseListPopupStep<T> {
        return object : BaseListPopupStep<T>(null, items) {
            private var isPopupReady = false
            private var lastSelectedValue: T? = null
            private var finalChoice = false

            init {
                Timer(200) {
                    isPopupReady = true
                }.apply {
                    isRepeats = false
                    start()
                }
            }

            override fun onChosen(selectedValue: T, finalChoice: Boolean): PopupStep<*>? {
                if (finalChoice) {
                    itemClickedCallback.forEach { it(selectedValue) }
                }
                this.finalChoice = finalChoice
                return FINAL_CHOICE
            }

            override fun getTextFor(value: T?): String {
                return value?.let { historyRender(it) } ?: ""
            }

            override fun isSpeedSearchEnabled(): Boolean = true

            override fun hasSubstep(selectedValue: T?): Boolean {
                if (!finalChoice && isPopupReady && lastSelectedValue != selectedValue) {
                    lastSelectedValue = selectedValue
                    selectedValue?.also { item -> itemHoverCallback.forEach { it(item) } }
                }
                return super.hasSubstep(selectedValue)
            }

            override fun canceled() {
                popupCanceledCallback.forEach { it() }
            }
        }
    }

    fun historyRender(render: (item: T) -> String) {
        this.historyRender = render
    }

    fun addItem(item: T) {
        removeItem(item)
        this.items.addFirst(item)
        if (items.size > 30) {
            items.removeLast()
        }
    }

    fun removeItem(item: T) {
        this.items.remove(item)
    }

    fun addItemClickedListener(callback: (T) -> Unit) {
        this.itemClickedCallback.add(callback)
    }

    fun removeItemClickedListener(callback: (T) -> Unit) {
        this.itemClickedCallback.remove(callback)
    }

    fun addItemHoverListener(callback: (T) -> Unit) {
        this.itemHoverCallback.add(callback)
    }

    fun removeItemHoverListener(callback: (T) -> Unit) {
        this.itemHoverCallback.remove(callback)
    }

    fun addPopupClosedListener(callback: () -> Unit) {
        this.popupClosedCallback.add(callback)
    }

    fun removePopupClosedListener(callback: () -> Unit) {
        this.popupClosedCallback.remove(callback)
    }

    fun addPopupCanceledListener(callback: () -> Unit) {
        this.popupCanceledCallback.add(callback)
    }

    fun removePopupCanceledListener(callback: () -> Unit) {
        this.popupCanceledCallback.remove(callback)
    }

}