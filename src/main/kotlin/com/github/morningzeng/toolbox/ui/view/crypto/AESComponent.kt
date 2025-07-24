package com.github.morningzeng.toolbox.ui.view.crypto

import com.intellij.openapi.project.Project

/**
 * @author Morning Zeng
 * @since 2025-05-16
 */
class AESComponent(
    project: Project
) : AbstractInternationalCryptoSymmetricComponent(project) {

    override fun getType(): String = "AES"

}