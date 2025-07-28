package com.github.morningzeng.toolbox.ui.view.crypto

import com.github.morningzeng.toolbox.model.Children
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

    protected var encUpperCase = true
    protected var decUpperCase = true

    protected val encryptArea: LanguageTextArea = object : LanguageTextArea(project) {
        override fun defaultRightBarActions(): Array<AnAction> {
            return arrayOf(
                softWrapAction(),
                scrollToEndAction(),
                upperCaseAction {
                    text = if (it) text.uppercase() else text.lowercase()
                    encUpperCase = it
                },
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
                upperCaseAction {
                    text = if (it) text.uppercase() else text.lowercase()
                    decUpperCase = it
                },
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
}