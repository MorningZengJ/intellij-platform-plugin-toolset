package com.github.morningzeng.toolbox.ui.view.crypto

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.Constants
import com.github.morningzeng.toolbox.enums.CryptoAsymmetricEnum
import com.github.morningzeng.toolbox.model.CryptoAsymmetric
import com.github.morningzeng.toolbox.ui.dialog.PropertyAsymmetricDialog
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.github.morningzeng.toolbox.utils.GridBagUtils.GridBagFill
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import java.util.*
import java.util.function.Predicate
import javax.swing.JButton

/**
 * @author Morning Zeng
 * @since 2025-05-23
 */
class AsymmetricComponent(
    project: Project
) : AbstractCryptoComponent<CryptoAsymmetric>(project) {

    private val cryptoComboBox: ComboBox<CryptoAsymmetricEnum> = ComboBox(CryptoAsymmetricEnum.entries.toTypedArray())
    private val encryptBtn = JButton("Encrypt", Constants.IconC.DOUBLE_ANGLES_DOWN)
    private val decryptBtn = JButton("Decrypt", Constants.IconC.DOUBLE_ANGLES_UP)
    private val signBtn = JButton("Sign", Constants.IconC.SIGNATURE)
    private val verifyBtn = JButton("Verify", Constants.IconC.SECURITY)

    init {
        initLayout()
        initAction()
        reloadCryptoProps()
    }

    override fun typeReference(): TypeReference<MutableList<CryptoAsymmetric>> {
        return object : TypeReference<MutableList<CryptoAsymmetric>>() {}
    }

    override fun cryptoPropText(t: CryptoAsymmetric): String {
        if (t.directory) return t.title
        return "${t.title} - ${t.description} ( ${if (t.isPublicKey) "Public Key" else "Private Key"} )"
    }

    override fun isDirectory(t: CryptoAsymmetric): Boolean = t.directory

    override fun initLayout() {
        GridBagUtils.builder(this).fill(GridBagFill.HORIZONTAL)
            .row { optionRow(it) { cit -> cit.cell().add(cryptoComboBox) } }
            .row { it.fill(GridBagFill.BOTH).cell().weightY(1.0).add(encryptArea.withRightBar()) }
            .row {
                GridBagUtils.builder()
                    .row { r ->
                        r.fill(GridBagFill.HORIZONTAL)
                            .cell().add(encryptBtn)
                            .cell().add(decryptBtn)
                            .cell().add(signBtn)
                            .cell().add(verifyBtn)
                    }
                    .build()
                    .apply { it.fill(GridBagFill.HORIZONTAL).cell().weightY(0.0).add(this) }
            }
            .row { it.fill(GridBagFill.BOTH).cell().weightY(1.0).add(decryptArea.withRightBar()) }
    }

    override fun initAction() {
        setButtonsVisible()
        encryptBtn.addActionListener { e ->
            try {
                this.cryptoPropComboBox.item?.apply {
                    if (directory) {
                        throw IllegalArgumentException("Please select a crypto key")
                    }
                    cryptoComboBox.item?.let {
                        encryptArea.text = crypto().encrypt(decryptArea.text)
                    }
                }
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, ex.message, "Encrypt Error")
            }
        }
        decryptBtn.addActionListener { e ->
            try {
                this.cryptoPropComboBox.item?.apply {
                    if (directory) {
                        throw IllegalArgumentException("Please select a crypto key")
                    }
                    cryptoComboBox.item?.let {
                        decryptArea.text = crypto().decrypt(encryptArea.text)
                    }
                }
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, ex.message, "Decrypt Error")
            }
        }
        signBtn.addActionListener { e ->
            try {
                this.cryptoPropComboBox.item?.apply {
                    if (directory) {
                        throw IllegalArgumentException("Please select a crypto key")
                    }
                    cryptoComboBox.item?.let {
                        encryptArea.text = it.privateKey(key).sign(decryptArea.text)
                    }
                }
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, ex.message, "Encrypt Error")
            }
        }
        verifyBtn.addActionListener { e ->
            try {
                this.cryptoPropComboBox.item?.apply {
                    if (directory) {
                        throw IllegalArgumentException("Please select a crypto key")
                    }
                    cryptoComboBox.item?.let {
                        val verify = it.publicKey(key).verify(decryptArea.text, encryptArea.text)
                        if (verify) {
                            Messages.showInfoMessage("Signature verification passed", "Signature Verification")
                        } else {
                            Messages.showWarningDialog("Signature verification failed", "Signature Verification")
                        }
                    }
                }
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, ex.message, "Encrypt Error")
            }
        }
        cryptoComboBox.addItemListener {
            reloadCryptoProps()
            setButtonsVisible()
        }
        cryptoPropComboBox.addItemListener { setButtonsVisible() }
        cryptoManageBtn.addActionListener {
            PropertyAsymmetricDialog(cryptoComboBox.item, project, okAfter = {
                reloadCryptoProps()
            }, selectedAfter = {
                cryptoPropComboBox.selectedItem = it
            }).showAndGet()
        }
    }

    override fun filterProp(): Predicate<CryptoAsymmetric> {
        return Predicate {
            Optional.ofNullable(cryptoComboBox)
                .map { crypto -> crypto.item == it.crypto }
                .orElse(false)
        }
    }

    private fun setButtonsVisible() {
        cryptoPropComboBox.item?.let {
            if (!it.directory) {
                encryptBtn.isEnabled = true
                decryptBtn.isEnabled = true
                signBtn.isEnabled = true
                verifyBtn.isEnabled = true
                cryptoComboBox.item?.apply {
                    if (it.isPublicKey) {
                        encryptBtn.isVisible = publicEncPrivateDec
                        decryptBtn.isVisible = privateEncPublicDec
                    } else {
                        encryptBtn.isVisible = privateEncPublicDec
                        decryptBtn.isVisible = publicEncPrivateDec
                    }
                }
            } else {
                encryptBtn.isEnabled = false
                decryptBtn.isEnabled = false
                signBtn.isEnabled = false
                verifyBtn.isEnabled = false
            }
        }
    }
}