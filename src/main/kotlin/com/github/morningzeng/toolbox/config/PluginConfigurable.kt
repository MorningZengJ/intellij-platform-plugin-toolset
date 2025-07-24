package com.github.morningzeng.toolbox.config

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.MyBundle
import com.github.morningzeng.toolbox.annotations.ScratchConfig
import com.github.morningzeng.toolbox.model.Children
import com.github.morningzeng.toolbox.model.CryptoAsymmetric
import com.github.morningzeng.toolbox.model.CryptoHash
import com.github.morningzeng.toolbox.model.CryptoSymmetric
import com.github.morningzeng.toolbox.utils.ScratchFileUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.NlsContexts
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import javax.swing.JComponent

/**
 * @author Morning Zeng
 * @since 2025-07-24
 */
class PluginConfigurable : Configurable {
    private var pluginConfigurableComponent: PluginConfigurableComponent? = null

    override fun getDisplayName(): @NlsContexts.ConfigurableName String? =
        MyBundle.message("application.configurable.display-name")

    override fun createComponent(): JComponent? {
        return PluginConfigurableComponent().apply { pluginConfigurableComponent = this }
    }

    override fun isModified(): Boolean {
        return pluginConfigurableComponent?.isModified(PluginConfig.getInstance()) ?: false
    }

    override fun apply() {
        val config = PluginConfig.getInstance()
        val old = config.state.cryptoFileFormat
        pluginConfigurableComponent?.apply(config)
        if (old != config.state.cryptoFileFormat) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Save config") {
                override fun run(indicator: ProgressIndicator) {
                    val tasks = listOf(
                        object : TypeReference<List<CryptoSymmetric>>() {},
                        object : TypeReference<List<CryptoAsymmetric>>() {},
                        object : TypeReference<List<CryptoHash>>() {},
                    )
                    ApplicationManager.getApplication().invokeAndWait {
                        tasks.forEachIndexed { index, value ->
                            try {
                                val parameterizedType = value.type as ParameterizedType
                                val typeArgument = parameterizedType.actualTypeArguments[0]
                                val clazz = if (typeArgument is WildcardType) {
                                    typeArgument.upperBounds[0] as Class<*>
                                } else {
                                    typeArgument as Class<*>
                                }
                                val scratchConfig = clazz.getAnnotation(ScratchConfig::class.java)
                                indicator.text = "Converting ${scratchConfig!!.value} ..."
                                val content: List<Children<*>>? = ScratchFileUtils.read(value, old)
                                if (content == null) return@forEachIndexed
                                ScratchFileUtils.write(
                                    content,
                                    value as TypeReference<List<Children<*>>>,
                                    config.state.cryptoFileFormat
                                )
                            } finally {
                                indicator.fraction = (index + 1).div(tasks.size.toDouble())
                            }
                        }
                    }
                }
            })
        }
    }

    override fun reset() {
        pluginConfigurableComponent?.reset(PluginConfig.getInstance())
    }

    override fun disposeUIResources() {
        pluginConfigurableComponent = null
    }
}