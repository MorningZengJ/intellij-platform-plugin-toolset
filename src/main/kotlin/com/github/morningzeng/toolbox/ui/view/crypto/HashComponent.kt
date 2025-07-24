package com.github.morningzeng.toolbox.ui.view.crypto

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.Constants
import com.github.morningzeng.toolbox.enums.CryptoHashEnum
import com.github.morningzeng.toolbox.model.CryptoHash
import com.github.morningzeng.toolbox.ui.dialog.PropertyHashDialog
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.github.morningzeng.toolbox.utils.GridBagUtils.GridBagFill
import com.github.morningzeng.toolbox.utils.HumanUtils.maskSensitive
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import java.awt.event.ItemEvent
import javax.swing.JButton

/**
 * @author Morning Zeng
 * @since 2025-05-22
 */
class HashComponent(
    project: Project
) : AbstractCryptoComponent<CryptoHash>(project) {

    private val cryptoComboBox: ComboBox<CryptoHashEnum> = ComboBox(CryptoHashEnum.entries.toTypedArray())
    private val calculation: JButton = JButton("Calculation", Constants.IconC.DOUBLE_ANGLES_DOWN)

    init {
        initLayout()
        initAction()
    }

    override fun typeReference(): TypeReference<MutableList<CryptoHash>> =
        object : TypeReference<MutableList<CryptoHash>>() {}

    override fun cryptoPropText(t: CryptoHash): String {
        if (t.directory) return t.title
        return "${t.title} - ${t.description} ( ${t.key.maskSensitive()} )"
    }

    override fun isDirectory(t: CryptoHash): Boolean = t.directory

    override fun initLayout() {
        encryptArea.readOnly = true
        cryptoManageBtn.isEnabled = false
        cryptoPropComboBox.isEnabled = false
        GridBagUtils.builder(this).fill(GridBagFill.HORIZONTAL)
            .row { optionRow(it) { cit -> cit.cell().add(cryptoComboBox) } }
            .row { it.fill(GridBagFill.BOTH).cell().weightY(1.0).add(decryptArea.withRightBar()) }
            .row {
                GridBagUtils.builder()
                    .row { r -> r.fill(GridBagFill.HORIZONTAL).cell().add(calculation) }
                    .build()
                    .apply { it.fill(GridBagFill.HORIZONTAL).cell().weightY(0.0).add(this) }
            }
            .row { it.fill(GridBagFill.BOTH).cell().weightY(1.0).add(encryptArea.withRightBar()) }
    }

    override fun initAction() {
        cryptoManageBtn.addActionListener {
            PropertyHashDialog(project, okAfter = { refresh(it) }, selectedAfter = { cryptoPropComboBox.item = it })
                .showAndGet()
        }
        calculation.addActionListener {
            try {
                val crypto = cryptoComboBox.item
                when (crypto) {
                    CryptoHashEnum.HMAC_SHA256, CryptoHashEnum.HMAC_SHA512 -> {
                        cryptoPropComboBox.item.let {
                            crypto.encrypt(decryptArea.text, it.key)
                        }
                    }

                    else -> crypto.encrypt(decryptArea.text)
                }.apply { encryptArea.text = this }
            } catch (e: Exception) {
                Messages.showErrorDialog(project, e.message, "HASH Error")
            }
        }
        cryptoComboBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                e.item.let { it as CryptoHashEnum }
                    .let {
                        when (it) {
                            CryptoHashEnum.HMAC_SHA256, CryptoHashEnum.HMAC_SHA512 -> true

                            else -> false
                        }.apply {
                            cryptoPropComboBox.isEnabled = this
                            cryptoManageBtn.isEnabled = this
                        }
                    }
            }
        }
    }

    private fun refresh(props: MutableList<CryptoHash>) {
        this.cryptoPropComboBox.removeAllItems()
        this.flatProps(props).forEach {
            this.cryptoPropComboBox.addItem(it)
        }
    }
}