package com.github.morningzeng.toolbox.config

import com.github.morningzeng.toolbox.enums.JacksonType
import com.intellij.openapi.components.*

/**
 * @author Morning Zeng
 * @since 2025-07-24
 */
@State(
    name = "ToolsetConfig", storages = [Storage("ToolsetConfig.xml")]
)
@Service
class PluginConfig : PersistentStateComponent<PluginConfig.State> {
    private var state = State()

    companion object {
        fun getInstance(): PluginConfig = service()
    }

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }


    data class State(
        var cryptoFileFormat: JacksonType = JacksonType.YAML
    )

}