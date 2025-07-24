package com.github.morningzeng.toolbox.utils

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
object HumanUtils {

    fun String.maskSensitive(): String {
        val regex = Regex("(?<=.{3}).+(?=.{4})")
        return this.replace(regex, "****")
    }

}