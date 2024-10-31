package com.github.morningzeng.toolset.ui.crypto;

import com.github.morningzeng.toolset.model.SymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.GridBagUtils.Row;
import com.github.morningzeng.toolset.utils.SymmetricCrypto;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Morning Zeng
 * @since 2024-10-29
 */
public sealed abstract class AbstractInternationalSymmetricCryptoComponent extends AbstractSymmetricCryptoComponent permits AESComponent, BlowfishComponent, DESComponent {

    final SymmetricCrypto[] cryptos = Arrays.stream(SymmetricCrypto.values())
            .filter(crypto -> getType().equals(crypto.getType()))
            .toArray(SymmetricCrypto[]::new);
    /**
     * Represents a combo box for selecting the type of symmetric encryption algorithm.
     * <p>
     * This variable is an instance of the ComboBox class.
     * It is declared as 'private final', indicating that it cannot be modified or reassigned once initialized.
     * The generic type parameter is 'SymmetricCrypto', representing the type of items contained in the combo box.
     * <p>
     * The combo box is initialized with the crypto items,
     * which are options for selecting a symmetric encryption algorithm.
     * <p>
     * This combo box is used in the AESComponent class to allow the user to choose the encryption algorithm.
     *
     * @since <version>
     */
    private final ComboBox<SymmetricCrypto> cryptoComboBox = new ComboBox<>(this.cryptos);

    public AbstractInternationalSymmetricCryptoComponent(final Project project) {
        super(project);
        this.cryptoComboBox.setSelectedItem(SymmetricCrypto.AES_CBC_PKCS5);
        this.initLayout();
        this.initAction();
    }

    @NotNull
    abstract String getType();

    @Override
    void cryptoRow(final Row<AbstractSymmetricCryptoComponent> row) {
        row.newCell().add(this.cryptoComboBox);
    }

    @Override
    String enc(final SymmetricCryptoProp cryptoProp) {
        return this.cryptoComboBox.getItem().crypto(cryptoProp.getKey(), cryptoProp.keyType(), cryptoProp.getIv(), cryptoProp.ivType()).enc(this.decryptArea.getText());
    }

    @Override
    String dec(final SymmetricCryptoProp cryptoProp) {
        return this.cryptoComboBox.getItem().crypto(cryptoProp.getKey(), cryptoProp.keyType(), cryptoProp.getIv(), cryptoProp.ivType()).dec(this.encryptArea.getText());
    }
}
