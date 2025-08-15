package com.github.morningzeng.toolbox.ui.view.crypto

import com.github.morningzeng.toolbox.model.Children
import com.github.morningzeng.toolbox.ui.action.AutoFormatAction
import com.github.morningzeng.toolbox.ui.action.HistoryAction
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import java.util.stream.Stream

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
abstract class AbstractCryptoComponent<T : Children<T>>(
    project: Project,
) : AbstractCryptoPropComponent<T>(project) {

    private val regex = Regex("(?<=^.{10}).+?(?=.{10}$)")
    var placeholderString = " ...... "

    protected val encHistoryAction = HistoryAction<String>().apply {
        historyRender { it.replace(regex, placeholderString) }
    }
    protected val decHistoryAction = HistoryAction<String>().apply {
        historyRender { it.replace(regex, placeholderString) }
    }

    init {
        historyActionEvent(encHistoryAction) { encryptArea }
        historyActionEvent(decHistoryAction) { decryptArea }
    }

    protected val encryptArea: LanguageTextArea = object : LanguageTextArea(project) {
        override fun defaultRightBarActions(): Array<AnAction> {
            return arrayOf(
                softWrapAction(),
                scrollToEndAction(),
                encHistoryAction,
                clearAllAction()
            )
        }
    }
        .also { it.setPlaceholder("Encrypted text content") }
    protected val decryptArea: LanguageTextArea = object : LanguageTextArea(project) {
        override fun defaultRightBarActions(): Array<AnAction> {
            return arrayOf(
                softWrapAction(),
                scrollToEndAction(),
                decHistoryAction,
                AutoFormatAction(this),
                clearAllAction()
            )
        }
    }
        .also { it.setPlaceholder("Decrypted text content") }

    override fun flatProps(props: MutableList<T>?): Stream<T> {
        return props?.stream()
            ?.sorted(Children.comparable())
            ?.mapMulti { t, consumer ->
                consumer.accept(t)
                t.children.stream()
                    .filter(filterProp())
                    .sorted(Children.comparable())
                    .forEach { consumer.accept(it) }
            }
            ?: Stream.empty()
    }

    protected open fun historyActionEvent(action: HistoryAction<String>, textArea: () -> LanguageTextArea) {
        var temp: String? = null
        action.addItemClickedListener {
            temp = null
            textArea().text = it
        }
        action.addItemHoverListener {
            if (temp == null) {
                temp = textArea().text
            }
            textArea().text = it
        }
        action.addPopupClosedListener {
            temp?.let {
                textArea().text = it
                temp = null
            }
        }
        action.addPopupCanceledListener {
            temp?.let {
                textArea().text = it
                temp = null
            }
        }
    }
}