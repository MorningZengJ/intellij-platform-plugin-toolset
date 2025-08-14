package com.github.morningzeng.toolbox.ui.view.crypto

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.Constants.IconC
import com.github.morningzeng.toolbox.enums.DataToBinaryTypeEnum
import com.github.morningzeng.toolbox.model.CryptoSymmetric
import com.github.morningzeng.toolbox.ui.dialog.PropertySymmetricDialog
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.github.morningzeng.toolbox.utils.GridBagUtils.GridBagFill
import com.github.morningzeng.toolbox.utils.HumanUtils.maskSensitive
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.Messages
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
abstract class AbstractCryptoSymmetricComponent(
    project: Project
) : AbstractCryptoComponent<CryptoSymmetric>(project) {
    protected val encryptedContentComboBox: LabeledComponent<ComboBox<DataToBinaryTypeEnum>> = LabeledComponent.create(
        ComboBox(DataToBinaryTypeEnum.entries.toTypedArray()), "Encrypted content type", BorderLayout.WEST
    )
    protected val decryptedContentComboBox: LabeledComponent<ComboBox<DataToBinaryTypeEnum>> = LabeledComponent.create(
        ComboBox(DataToBinaryTypeEnum.entries.toTypedArray()), "Decrypted content type", BorderLayout.WEST
    )
    private val encryptBtn: JButton = JButton("Encrypt", IconC.DOUBLE_ANGLES_DOWN)
    private val decryptBtn: JButton = JButton("Decrypt", IconC.DOUBLE_ANGLES_UP)

    override fun typeReference(): TypeReference<MutableList<CryptoSymmetric>> {
        return object : TypeReference<MutableList<CryptoSymmetric>>() {
        }
    }

    override fun cryptoPropText(t: CryptoSymmetric): String {
        if (t.directory) return t.title
        return "${t.title} - ${t.description} ( ${t.key.maskSensitive()} / ${t.iv.maskSensitive()} )"
    }

    override fun isDirectory(t: CryptoSymmetric): Boolean = t.directory

    override fun initLayout() {
        GridBagUtils.builder(this).fill(GridBagFill.HORIZONTAL)
            .row { optionRow(it) { cit -> cryptoRow(cit) } }
            .row { it.fill(GridBagFill.BOTH).cell().weightY(1.0).add(decryptArea.withRightBar()) }
            .row {
                GridBagUtils.builder()
                    .row { r ->
                        r.fill(GridBagFill.HORIZONTAL)
                            .cell().add(encryptedContentComboBox)
                            .cell().add(encryptBtn)
                            .cell().add(decryptBtn)
                            .cell().add(decryptedContentComboBox)
                    }
                    .build().apply { it.fill(GridBagFill.HORIZONTAL).cell().weightY(0.0).add(this) }
            }
            .row { it.fill(GridBagFill.BOTH).cell().weightY(1.0).add(encryptArea.withRightBar()) }
    }

    override fun initAction() {
        encryptedContentComboBox.component.selectedItem = DataToBinaryTypeEnum.TEXT
        decryptedContentComboBox.component.selectedItem = DataToBinaryTypeEnum.BASE64
        encryptBtn.addActionListener {
            try {
                val item = this.cryptoPropComboBox.item
                item?.let {
                    if (item.directory || item.key.isBlank()) {
                        Messages.showErrorDialog(this, "Please select the correct crypto item")
                        return@addActionListener
                    }
                    this.encryptArea.text = encrypt(item)
                    decHistoryAction.addItem(this.decryptArea.text)
                    encHistoryAction.addItem(this.encryptArea.text)
                }
            } catch (e: Exception) {
                Messages.showErrorDialog(this, e.message)
            }
        }
        decryptBtn.addActionListener {
            try {
                val item = this.cryptoPropComboBox.item
                item?.let {
                    if (item.directory || item.key.isBlank()) {
                        Messages.showErrorDialog(this, "Please select the correct crypto item")
                        return@addActionListener
                    }
                    this.decryptArea.text = decrypt(item)
                    decHistoryAction.addItem(this.decryptArea.text)
                    encHistoryAction.addItem(this.encryptArea.text)
                }
            } catch (e: Exception) {
                Messages.showErrorDialog(this, e.message)
            }
        }
        this.cryptoManageBtn.addActionListener {
            PropertySymmetricDialog(project, okAfter = {
                this.reloadCryptoProps()
            }, selectedAfter = {
                this.cryptoPropComboBox.selectedItem = it
            }).showAndGet()
        }
    }

    abstract fun cryptoRow(row: GridBagUtils.Row<out JComponent>)

    abstract fun encrypt(prop: CryptoSymmetric): String

    abstract fun decrypt(prop: CryptoSymmetric): String

}