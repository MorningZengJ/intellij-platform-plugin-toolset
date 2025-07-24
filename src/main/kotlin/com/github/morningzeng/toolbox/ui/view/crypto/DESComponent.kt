package com.github.morningzeng.toolbox.ui.view.crypto

import com.github.morningzeng.toolbox.enums.CryptoSymmetricEnum
import com.intellij.openapi.project.Project

/**
 * @author Morning Zeng
 * @since 2025-05-22
 */
class DESComponent(
    project: Project
) : AbstractInternationalCryptoSymmetricComponent(project) {
    override fun getType(): String = "DES"

    override fun defaultSelected() {
        cryptoComboBox.selectedItem = CryptoSymmetricEnum.DES_CBC_PKCS5
    }
}