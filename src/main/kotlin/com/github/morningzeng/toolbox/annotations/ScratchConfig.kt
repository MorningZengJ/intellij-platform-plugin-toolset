package com.github.morningzeng.toolbox.annotations

/**
 * @author Morning Zeng
 * @since 2025-05-16
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScratchConfig(
    val value: String,
    val directory: String = "",
)