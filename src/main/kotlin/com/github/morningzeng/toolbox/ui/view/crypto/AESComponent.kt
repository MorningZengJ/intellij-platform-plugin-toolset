package com.github.morningzeng.toolbox.ui.view.crypto

import com.github.morningzeng.toolbox.enums.CryptoSymmetricEnum
import com.intellij.openapi.project.Project

/**
 * @author Morning Zeng
 * @since 2025-05-16
 */
class AESComponent(
    project: Project
) : AbstractInternationalCryptoSymmetricComponent(project) {

    override fun getType(): String = "AES"

    override fun defaultSelected() {
        cryptoComboBox.selectedItem = CryptoSymmetricEnum.AES_CBC_PKCS5
    }
}