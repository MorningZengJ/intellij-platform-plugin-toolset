package com.github.morningzeng.toolbox.annotations

import com.github.morningzeng.toolbox.enums.JacksonType

/**
 * @author Morning Zeng
 * @since 2025-05-16
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScratchConfig(
    val value: String,
    val directory: String = "",
    val outputType: JacksonType = JacksonType.YAML
)