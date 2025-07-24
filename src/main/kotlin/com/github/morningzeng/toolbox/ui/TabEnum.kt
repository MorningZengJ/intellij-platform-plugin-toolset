package com.github.morningzeng.toolbox.ui

import com.github.morningzeng.toolbox.ui.view.coding.CodingEnum
import com.github.morningzeng.toolbox.ui.view.crypto.CryptoEnum
import com.github.morningzeng.toolbox.ui.view.gadget.GadgetEnum
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBTabbedPane
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author Morning Zeng
 * @since 2025-05-15
 */
enum class TabEnum(
    override val title: String,
    override val icon: Icon? = null,
    override val tips: String,
    val load: Boolean
) : TabSupport {

    CRYPTO("Crypto", null, "Encrypt and Decrypt", true) {
        override fun component(toolWindow: ToolWindow): JComponent {
            return JBTabbedPane(JBTabbedPane.LEFT).apply {
                CryptoEnum.entries.forEach {
                    it.putTab(this, toolWindow)
                }
            }
        }
    },
    CODING("Encoding & Decoding", null, "Encoding and Decoding", true) {
        override fun component(toolWindow: ToolWindow): JComponent {
            return JBTabbedPane(JBTabbedPane.LEFT).apply {
                CodingEnum.entries.forEach {
                    it.putTab(this, toolWindow)
                }
            }
        }
    },
    GADGET("Gadget", null, "Gadget", true) {
        override fun component(toolWindow: ToolWindow): JComponent {
            return JBTabbedPane(JBTabbedPane.LEFT).apply {
                GadgetEnum.entries.forEach {
                    it.putTab(this, toolWindow)
                }
            }
        }
    },

}