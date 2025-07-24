package com.github.morningzeng.toolbox.config

import com.github.morningzeng.toolbox.enums.JacksonType
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import java.awt.BorderLayout

/**
 * @author Morning Zeng
 * @since 2025-07-24
 */
class PluginConfigurableComponent() : JBPanel<JBPanelWithEmptyText>() {
    var currentFormat: JacksonType = PluginConfig.getInstance().state.cryptoFileFormat

    init {
        val panel = panel {
            row("Crypto config format") {
                comboBox(listOf(JacksonType.JSON, JacksonType.PROPERTIES, JacksonType.YAML))
                    .bindItem(::currentFormat.toNullableProperty())
                    .comment(
                        """
                        <p>Encrypt the text format used by the decryption key configuration.</p> 
                        <p>The format changes will be automatically migrated without affecting</p>
                        <p>the original configuration.</p>
                    """.trimIndent()
                    ).apply {
                        this.component.addItemListener { currentFormat = it.item as JacksonType }
                    }
            }
        }
        add(panel, BorderLayout.LINE_START)
    }

    fun isModified(pluginConfig: PluginConfig): Boolean {
        return currentFormat != pluginConfig.state.cryptoFileFormat
    }

    fun apply(pluginConfig: PluginConfig) {
        pluginConfig.state.cryptoFileFormat = currentFormat
    }

    fun reset(pluginConfig: PluginConfig) {
        currentFormat = pluginConfig.state.cryptoFileFormat
    }
}