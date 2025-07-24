package com.github.morningzeng.toolbox.ui.view.crypto

import com.github.morningzeng.toolbox.enums.CryptoSymmetricEnum
import com.github.morningzeng.toolbox.model.CryptoSymmetric
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox

/**
 * @author Morning Zeng
 * @since 2025-05-20
 */
abstract class AbstractInternationalCryptoSymmetricComponent(
    project: Project
) : AbstractCryptoSymmetricComponent(project) {

    private val cryptoComboBox: ComboBox<CryptoSymmetricEnum> =
        CryptoSymmetricEnum.entries.filter { getType() == it.type }.toTypedArray().let {
            ComboBox(it).apply { selectedItem = CryptoSymmetricEnum.AES_CBC_PKCS5 }
        }

    init {
        initLayout()
        initAction()
    }

    abstract fun getType(): String

    override fun cryptoRow(row: GridBagUtils.Row<AbstractCryptoSymmetricComponent>) {
        row.cell().add(cryptoComboBox)
    }

    override fun encrypt(prop: CryptoSymmetric): String {
        val data = encryptedContentComboBox.component.item.bytes(decryptArea.text)
        return cryptoComboBox.item?.crypto(prop.key, prop.keyType, prop.iv, prop.ivType)?.encrypt(data) ?: ""
    }

    override fun decrypt(prop: CryptoSymmetric): String {
        val data = decryptedContentComboBox.component.item.bytes(encryptArea.text)
        return cryptoComboBox.item?.crypto(prop.key, prop.keyType, prop.iv, prop.ivType)?.decrypt(data) ?: ""
    }

}