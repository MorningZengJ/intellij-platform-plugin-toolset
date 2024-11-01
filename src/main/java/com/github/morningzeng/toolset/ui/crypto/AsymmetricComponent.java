package com.github.morningzeng.toolset.ui.crypto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.AbstractComponent.HorizontalDoubleButton;
import com.github.morningzeng.toolset.dialog.AsymmetricPropDialog;
import com.github.morningzeng.toolset.model.AsymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.AsymmetricCrypto;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;

import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-10-31
 */
public final class AsymmetricComponent extends AbstractCryptoComponent<AsymmetricCryptoProp> {

    private final ComboBox<AsymmetricCrypto> cryptoComboBox = new ComboBox<>(AsymmetricCrypto.values());
    private final JButton encryptBtn = new JButton("Encrypt", IconC.DOUBLE_ANGLES_DOWN);
    private final JButton decryptBtn = new JButton("Decrypt", IconC.DOUBLE_ANGLES_UP);

    public AsymmetricComponent(final Project project) {
        super(project);
        this.initLayout();
        this.initAction();
    }

    @Override
    List<AsymmetricCryptoProp> getCryptoProps() {
        return ScratchFileUtils.read(new TypeReference<>() {
        });
    }

    @Override
    Comparator<? super AsymmetricCryptoProp> comparator() {
        return Comparator.comparing(AsymmetricCryptoProp::getSorted);
    }

    @Override
    String cryptoPropText(final AsymmetricCryptoProp prop) {
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
    boolean isDirectory(final AsymmetricCryptoProp prop) {
        return prop.isDirectory();
    }

    @Override
    void initLayout() {
        final HorizontalDoubleButton buttonBar = new HorizontalDoubleButton(this.encryptBtn, this.decryptBtn);
        this.setLayout(new GridBagLayout());
        GridBagUtils.builder(this)
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().weightX(1).add(this.cryptoPropComboBox)
                        .newCell().weightX(0).add(this.cryptoManageBtn)
                        .newCell().add(this.cryptoComboBox))
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightY(1).gridWidth(3).add(this.decryptArea))
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().weightY(0).add(buttonBar))
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightY(1).gridWidth(3).add(this.encryptArea));
    }

    @Override
    void initAction() {
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
        this.cryptoManageBtn.addActionListener(e -> {
            final AsymmetricPropDialog dialog = new AsymmetricPropDialog(this.cryptoComboBox.getItem(), this.project, this::reloadCryptoProps);
            dialog.showAndGet();
        });
    }
}
