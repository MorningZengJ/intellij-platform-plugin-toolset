package com.github.morningzeng.toolbox.ui.view.crypto

import com.github.morningzeng.toolbox.model.Children
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.intellij.openapi.project.Project
import java.util.stream.Stream

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
abstract class AbstractCryptoComponent<T : Children<T>>(
    project: Project,
) : AbstractCryptoPropComponent<T>(project) {

    protected val encryptArea: LanguageTextArea = LanguageTextArea(project)
        .also { it.setPlaceholder("Encrypted text content") }
    protected val decryptArea: LanguageTextArea = LanguageTextArea(project)
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