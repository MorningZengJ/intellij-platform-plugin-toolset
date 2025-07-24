package com.github.morningzeng.toolbox.ui.view.crypto

import cn.hutool.core.util.HexUtil
import cn.hutool.crypto.Mode
import cn.hutool.crypto.Padding
import cn.hutool.crypto.symmetric.SM4
import com.github.morningzeng.toolbox.model.CryptoSymmetric
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox

/**
 * @author Morning Zeng
 * @since 2025-05-22
 */
class SM4Component(
    project: Project
) : AbstractCryptoSymmetricComponent(project) {

    private val modeComboBox: ComboBox<Mode> = ComboBox(Mode.entries.toTypedArray())
    private val paddingComboBox: ComboBox<Padding> = ComboBox(Padding.entries.toTypedArray())

    init {
        initLayout()
        initAction()
    }

    override fun cryptoRow(row: GridBagUtils.Row<AbstractCryptoSymmetricComponent>) {
        GridBagUtils.builder()
            .row {
                it.fill(GridBagUtils.GridBagFill.HORIZONTAL)
                    .cell().add(modeComboBox)
                    .cell().add(paddingComboBox)
            }
            .build()
            .apply { row.cell().add(this) }
    }

    override fun encrypt(prop: CryptoSymmetric): String {
        return sm4(prop).encryptHex(encryptedContentComboBox.component.item.bytes(decryptArea.text))
    }

    override fun decrypt(prop: CryptoSymmetric): String {
        return sm4(prop).decryptStr(decryptedContentComboBox.component.item.bytes(encryptArea.text))
    }

    private fun sm4(prop: CryptoSymmetric): SM4 {
        val mode = modeComboBox.item
        val padding = paddingComboBox.item
        val key = HexUtil.decodeHex(prop.key)
        val iv = HexUtil.decodeHex(prop.iv)
        return SM4(mode, padding, key, iv)
    }

}