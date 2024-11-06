package com.github.morningzeng.toolset.ui.crypto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.dialog.AsymmetricPropDialog;
import com.github.morningzeng.toolset.model.AsymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.AsymmetricCrypto;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;

import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Morning Zeng
 * @since 2024-10-31
 */
public final class AsymmetricComponent extends AbstractCryptoComponent<AsymmetricCryptoProp> {

    private final ComboBox<AsymmetricCrypto> cryptoComboBox = new ComboBox<>(AsymmetricCrypto.values());
    private final JButton encryptBtn = new JButton("Encrypt", IconC.DOUBLE_ANGLES_DOWN);
    private final JButton decryptBtn = new JButton("Decrypt", IconC.DOUBLE_ANGLES_UP);
    private final JButton signBtn = new JButton("Sign", IconC.SIGNATURE);
    private final JButton verifyBtn = new JButton("Verify", IconC.SECURITY);

    public AsymmetricComponent(final Project project) {
        super(project);
        this.initLayout();
        this.initAction();
        super.reloadCryptoProps();
    }

    @Override
    protected TypeReference<List<AsymmetricCryptoProp>> typeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    protected Comparator<? super AsymmetricCryptoProp> comparator() {
        return Comparator.comparing(AsymmetricCryptoProp::getSorted);
    }

    @Override
    protected String cryptoPropText(final AsymmetricCryptoProp prop) {
        if (prop.isDirectory()) {
            return prop.getTitle();
        }
        final String template = "%s - %s ( %s )";
        return template.formatted(
                prop.getTitle(), prop.getDescription(),
                prop.getIsPublicKey() ? "Public Key" : "Private Key"
        );
    }

    @Override
    protected boolean isDirectory(final AsymmetricCryptoProp prop) {
        return prop.isDirectory();
    }

    @Override
    protected void initLayout() {
        this.setLayout(new GridBagLayout());
        GridBagUtils.builder(this)
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().weightX(1).add(this.cryptoPropComboBox)
                        .newCell().weightX(0).add(this.cryptoManageBtn)
                        .newCell().add(this.cryptoComboBox))
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightY(1).gridWidth(3).add(this.decryptArea))
                .newRow(row -> {
                    final JBPanel<JBPanelWithEmptyText> btnPanel = GridBagUtils.builder()
                            .newRow(_row -> _row.fill(GridBagFill.HORIZONTAL)
                                    .newCell().add(this.encryptBtn)
                                    .newCell().add(this.decryptBtn)
                                    .newCell().add(this.signBtn)
                                    .newCell().add(this.verifyBtn))
                            .build();
                    row.fill(GridBagFill.HORIZONTAL)
                            .newCell().weightY(0).gridWidth(3).add(btnPanel);
                })
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightY(1).gridWidth(3).add(this.encryptArea));
    }

    @Override
    protected void initAction() {
        this.encryptBtn.addActionListener(e -> {
            try {
                final AsymmetricCryptoProp prop = this.cryptoPropComboBox.getItem();
                if (prop.isDirectory() || Objects.isNull(prop.getKey())) {
                    Messages.showErrorDialog(project, "Please select a crypto key", "Error");
                    return;
                }
                final AsymmetricCrypto crypto = this.cryptoComboBox.getItem();
                final String enc = prop.crypto(project, crypto).enc(this.decryptArea.getText());
                this.encryptArea.setText(enc);
            } catch (Exception ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Encrypt Error", Messages.getErrorIcon());
            }
        });
        // 不使用自动格式化
        this.decryptArea.autoReformat(false);
        this.decryptBtn.addActionListener(e -> {
            try {
                final AsymmetricCryptoProp prop = this.cryptoPropComboBox.getItem();
                if (prop.isDirectory() || Objects.isNull(prop.getKey())) {
                    Messages.showErrorDialog(project, "Please select a crypto key", "Error");
                    return;
                }
                final AsymmetricCrypto crypto = this.cryptoComboBox.getItem();
                final String dec = prop.crypto(project, crypto).dec(this.encryptArea.getText());
                this.decryptArea.setText(dec);
            } catch (Exception ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Encrypt Error", Messages.getErrorIcon());
            }
        });
        this.signBtn.addActionListener(e -> {
            try {
                final AsymmetricCryptoProp prop = this.cryptoPropComboBox.getItem();
                if (prop.isDirectory() || Objects.isNull(prop.getKey()) || prop.getIsPublicKey()) {
                    Messages.showErrorDialog(project, "Please select a private key", "Error");
                    return;
                }
                final AsymmetricCrypto crypto = this.cryptoComboBox.getItem();
                final String sign = crypto.privateKey(prop.getKey()).sign(this.decryptArea.getText());
                this.encryptArea.setText(sign);
            } catch (Exception ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Encrypt Error", Messages.getErrorIcon());
            }
        });
        this.verifyBtn.addActionListener(e -> {
            try {
                final AsymmetricCryptoProp prop = this.cryptoPropComboBox.getItem();
                if (prop.isDirectory() || Objects.isNull(prop.getKey()) || !prop.getIsPublicKey()) {
                    Messages.showErrorDialog(project, "Please select a public key", "Error");
                    return;
                }
                final AsymmetricCrypto crypto = this.cryptoComboBox.getItem();
                final boolean verify = crypto.publicKey(prop.getKey()).verify(this.decryptArea.getText(), this.encryptArea.getText());
                if (verify) {
                    Messages.showInfoMessage("Signature verification passed", "Signature Verification");
                } else {
                    Messages.showWarningDialog("Signature verification failed", "Signature Verification");
                }
            } catch (Exception ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Encrypt Error", Messages.getErrorIcon());
            }
        });
        this.cryptoComboBox.addItemListener(e -> super.reloadCryptoProps());
        this.cryptoManageBtn.addActionListener(e -> {
            final AsymmetricPropDialog dialog = new AsymmetricPropDialog(this.cryptoComboBox.getItem(), this.project, this::reloadCryptoProps);
            dialog.showAndGet();
        });
    }

    @Override
    protected Predicate<AsymmetricCryptoProp> filterProp() {
        return prop -> {
            if (Objects.isNull(this.cryptoComboBox)) {
                return false;
            }
            final AsymmetricCrypto crypto = this.cryptoComboBox.getItem();
            return crypto.equals(prop.getCrypto());
        };
    }
}
