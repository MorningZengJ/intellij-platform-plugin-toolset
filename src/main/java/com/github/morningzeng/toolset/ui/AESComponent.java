package com.github.morningzeng.toolset.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.annotations.ScratchConfig;
import com.github.morningzeng.toolset.component.AbstractComponent.HorizontalDoubleButton;
import com.github.morningzeng.toolset.dialog.SymmetricPropDialog;
import com.github.morningzeng.toolset.model.SymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.github.morningzeng.toolset.utils.StringUtils;
import com.github.morningzeng.toolset.utils.SymmetricCrypto;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.GridBag;

import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-05-11
 */
public final class AESComponent extends CryptoComponent<SymmetricCryptoProp> {

    private final static String TYPE = "AES";
    final SymmetricCrypto[] cryptos = Arrays.stream(SymmetricCrypto.values())
            .filter(crypto -> TYPE.equals(crypto.getType()))
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
    private final JButton encryptBtn = new JButton("Encrypt", IconC.DOUBLE_ARROW_DOWN);
    private final JButton decryptBtn = new JButton("Decrypt", IconC.DOUBLE_ARROW_UP);


    /**
     * Initializes a new instance of the AESComponent class.
     * <p>
     * This constructor initializes the layout and action listeners for the AESComponent.
     * It calls the initLayout() and initAction() methods to set up the UI components and their actions.
     */
    public AESComponent(final Project project) {
        super(project);

        this.cryptoComboBox.setSelectedItem(SymmetricCrypto.AES_CBC_PKCS5);
        this.initLayout();
        this.initAction();
    }

    @Override
    List<SymmetricCryptoProp> getCryptoProps() {
        try {
            return ScratchFileUtils.read(new TypeReference<>() {
            });
        } catch (Exception e) {
            Messages.showErrorDialog(e.getMessage(), "Configuration File Is Incorrect");
            final ScratchConfig scratchConfig = SymmetricCryptoProp.class.getAnnotation(ScratchConfig.class);
            ScratchFileUtils.openFile(this.project, scratchConfig.directory(), scratchConfig.value());
        }
        return Collections.emptyList();
    }

    @Override
    Comparator<? super SymmetricCryptoProp> comparator() {
        return Comparator.comparing(SymmetricCryptoProp::getSorted);
    }

    @Override
    String cryptoPropText(final SymmetricCryptoProp prop) {
        if (prop.isDirectory()) {
            return prop.getTitle();
        }
        final String template = "%s - %s ( %s [ %s ] / %s [ %s ] )";
        return template.formatted(
                prop.getTitle(), prop.getDescription(),
                StringUtils.maskSensitive(prop.getKey()), prop.keyType(),
                StringUtils.maskSensitive(prop.getIv()), prop.ivType()
        );
    }

    @Override
    boolean isDirectory(final SymmetricCryptoProp prop) {
        return prop.isDirectory();
    }

    /**
     * Initializes the layout of the AESComponent class.
     * <p>
     * This method sets up the GridBagLayout for the AESComponent and adds various components to it.
     */
    private void initLayout() {
        this.setLayout(new GridBagLayout());

        final HorizontalDoubleButton buttonBar = new HorizontalDoubleButton(this.encryptBtn, this.decryptBtn);

        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.cryptoPropComboBox)
                .newCell().weightX(0).add(this.cryptoManageBtn)
                .newCell().add(this.cryptoComboBox)
                .newRow().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(this.decryptArea)
                .newRow().weightY(0).add(buttonBar)
                .newRow().weightY(1).gridWidth(3).add(this.encryptArea);
    }

    /**
     * Initializes the actions for the AESComponent class.
     * <p>
     * This method sets up the ActionListener for the encryption button and the decryption button.
     * When the encryption button is clicked, it retrieves the selected cryptography algorithm from the combo box,
     * retrieves the key and IV from the corresponding text fields, and encrypts the text from the decryption area using the selected algorithm.
     * The encrypted text is then set in the encryption area.
     * <p>
     * When the decryption button is clicked, it retrieves the selected cryptography algorithm from the combo box,
     * retrieves the key and IV from the corresponding text fields, and decrypts the text from the encryption area using the selected algorithm.
     * The decrypted text is then set in the decryption area.
     * <p>
     * If any exception occurs during encryption or decryption, an error message dialog is displayed.
     */
    private void initAction() {
        this.encryptBtn.addActionListener(e -> {
            try {
                final SymmetricCryptoProp cryptoProp = this.cryptoPropComboBox.getItem();
                if (cryptoProp.isDirectory() || Objects.isNull(cryptoProp.getKey()) || Objects.isNull(cryptoProp.getIv())) {
                    Messages.showErrorDialog(project, "Please select a crypto key", "Error");
                    return;
                }
                final String enc = this.cryptoComboBox.getItem().crypto(cryptoProp.getKey(), cryptoProp.keyType(), cryptoProp.getIv(), cryptoProp.ivType()).enc(this.decryptArea.getText());
                this.encryptArea.setText(enc);
            } catch (Exception ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Encrypt Error", Messages.getErrorIcon());
            }
        });
        // 不使用自动格式化
        this.decryptArea.autoReformat(false);
        this.decryptBtn.addActionListener(e -> {
            try {
                final SymmetricCryptoProp cryptoProp = this.cryptoPropComboBox.getItem();
                if (cryptoProp.isDirectory() || Objects.isNull(cryptoProp.getKey()) || Objects.isNull(cryptoProp.getIv())) {
                    Messages.showErrorDialog(project, "Please select a crypto key", "Error");
                    return;
                }
                final String dec = this.cryptoComboBox.getItem().crypto(cryptoProp.getKey(), cryptoProp.keyType(), cryptoProp.getIv(), cryptoProp.ivType()).dec(this.encryptArea.getText());
                this.decryptArea.setText(dec);
            } catch (Exception ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Decrypt Error", Messages.getErrorIcon());
            }
        });
        this.cryptoManageBtn.addActionListener(e -> {
            final SymmetricPropDialog dialog = new SymmetricPropDialog(this.project, this::reloadCryptoProps);
            dialog.showAndGet();
        });
    }

}
