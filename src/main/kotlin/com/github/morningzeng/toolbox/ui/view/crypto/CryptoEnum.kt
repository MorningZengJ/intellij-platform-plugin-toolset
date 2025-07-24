package com.github.morningzeng.toolbox.ui.view.crypto

import com.github.morningzeng.toolbox.ui.TabSupport
import com.intellij.openapi.wm.ToolWindow
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author Morning zeng
 * @since 2025-05-16
 */
enum class CryptoEnum(
    override val title: String,
    override val icon: Icon?,
    override val tips: String
) : TabSupport {
    AES("AES", null, "AES") {
        override fun component(toolWindow: ToolWindow): JComponent = AESComponent(toolWindow.project)
    },
    ASYMMETRIC("Asymmetric", null, "Asymmetric") {
        override fun component(toolWindow: ToolWindow): JComponent = AsymmetricComponent(toolWindow.project)
    },
    BLOWFISH("Blowfish", null, "Blowfish") {
        override fun component(toolWindow: ToolWindow): JComponent = BlowfishComponent(toolWindow.project)
    },
    DES("DES", null, "DES") {
        override fun component(toolWindow: ToolWindow): JComponent = DESComponent(toolWindow.project)
    },
    HASH("HASH", null, "HASH") {
        override fun component(toolWindow: ToolWindow): JComponent = HashComponent(toolWindow.project)
    },
    SM4("SM4", null, "SM4") {
        override fun component(toolWindow: ToolWindow): JComponent = SM4Component(toolWindow.project)
    }
    ;
}