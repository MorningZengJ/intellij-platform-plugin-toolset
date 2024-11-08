package com.github.morningzeng.toolset.ui.crypto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.dialog.SymmetricPropDialog;
import com.github.morningzeng.toolset.model.SymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.github.morningzeng.toolset.utils.StringUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;

import javax.swing.JButton;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-10-29
 */
public sealed abstract class AbstractSymmetricCryptoComponent extends AbstractCryptoComponent<SymmetricCryptoProp> permits AbstractInternationalSymmetricCryptoComponent, SM4Component {

    private final JButton encryptBtn = new JButton("Encrypt", IconC.DOUBLE_ANGLES_DOWN);
    private final JButton decryptBtn = new JButton("Decrypt", IconC.DOUBLE_ANGLES_UP);

    public AbstractSymmetricCryptoComponent(final Project project) {
        super(project);
    }

    @Override
    protected TypeReference<List<SymmetricCryptoProp>> typeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    protected Comparator<? super SymmetricCryptoProp> comparator() {
        return Comparator.comparing(SymmetricCryptoProp::getSorted);
    }

    @Override
    protected String cryptoPropText(final SymmetricCryptoProp prop) {
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
    protected boolean isDirectory(final SymmetricCryptoProp prop) {
        return prop.isDirectory();
    }

    /**
     * Initializes the layout of the AESComponent class.
     * <p>
     * This method sets up the GridBagLayout for the AESComponent and adds various components to it.
     */
    protected void initLayout() {
        GridBagUtils.builder(this)
                .newRow(row -> {
                    row.fill(GridBagFill.HORIZONTAL)
                            .newCell().weightX(1).add(this.cryptoPropComboBox)
                            .newCell().weightX(0).add(this.cryptoManageBtn);
                    this.cryptoRow(row);
                })
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightY(1).gridWidth(3).add(this.decryptArea.withRightBar()))
                .newRow(row -> {
                    final JBPanel<JBPanelWithEmptyText> btnPanel = GridBagUtils.builder()
                            .newRow(_row -> _row.fill(GridBagFill.HORIZONTAL)
                                    .newCell().add(this.encryptBtn)
                                    .newCell().add(this.decryptBtn))
                            .build();
                    row.fill(GridBagFill.HORIZONTAL)
                            .newCell().weightY(0).gridWidth(3).add(btnPanel);
                })
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightY(1).gridWidth(3).add(this.encryptArea.withRightBar()));
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
    protected void initAction() {
        this.encryptBtn.addActionListener(e -> {
            try {
                final SymmetricCryptoProp cryptoProp = this.cryptoPropComboBox.getItem();
                if (cryptoProp.isDirectory() || Objects.isNull(cryptoProp.getKey()) || Objects.isNull(cryptoProp.getIv())) {
                    Messages.showErrorDialog(project, "Please select a crypto key", "Error");
                    return;
                }
                this.encryptArea.setText(this.enc(cryptoProp));
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
                this.decryptArea.setText(this.dec(cryptoProp));
            } catch (Exception ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Decrypt Error", Messages.getErrorIcon());
            }
        });
        this.cryptoManageBtn.addActionListener(e -> {
            final SymmetricPropDialog dialog = new SymmetricPropDialog(this.project, this::reloadCryptoProps);
            dialog.showAndGet();
        });
    }

    abstract void cryptoRow(final GridBagUtils.Row<AbstractSymmetricCryptoComponent> row);

    abstract String enc(final SymmetricCryptoProp cryptoProp);

    abstract String dec(final SymmetricCryptoProp cryptoProp);
}
