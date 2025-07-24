package com.github.morningzeng.toolbox.utils

import com.intellij.openapi.ui.LabeledComponent
import java.awt.Dimension
import javax.swing.JComponent

/**
 * @author Morning Zeng
 * @since 2025-05-22
 */
object ExpandMethodUtils {

    fun <T : JComponent> LabeledComponent<T>.labelWidth(width: Int) {
        val dimension = Dimension(width, label.height)
        label.size = dimension
        label.preferredSize = dimension
        label.minimumSize = dimension
        label.maximumSize = dimension
    }

}