package com.github.morningzeng.toolset.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.dialog.HashPropDialog;
import com.github.morningzeng.toolset.model.HashCryptoProp;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.HashCrypto;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.github.morningzeng.toolset.utils.StringUtils;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.GridBag;

import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public final class HashComponent extends CryptoComponent<HashCryptoProp> {

    final HashCrypto[] cryptos = Arrays.stream(HashCrypto.values())
            .toArray(HashCrypto[]::new);
    private final JButton cryptoManageBtn = new JButton(General.Ellipsis);
    private final ComboBox<HashCrypto> cryptoComboBox = new ComboBox<>(this.cryptos);
    private final JButton calculation = new JButton("Calculation", IconC.DOUBLE_ARROW_DOWN);

    public HashComponent(final Project project) {
        super(project);

        this.setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.cryptoPropComboBox)
                .newCell().weightX(0).add(this.cryptoManageBtn)
                .newCell().add(this.cryptoComboBox)
                .newRow().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(this.decryptArea)
                .newRow().weightY(0).add(this.calculation)
                .newRow().weightY(1).gridWidth(3).add(this.encryptArea);

        this.cryptoManageBtn.setEnabled(false);
        this.cryptoPropComboBox.setEnabled(false);
        this.initEvent();
    }

    @Override
    List<HashCryptoProp> getCryptoProps() {
        return ScratchFileUtils.read(new TypeReference<>() {
        });
    }

    @Override
    Comparator<? super HashCryptoProp> comparator() {
        return Comparator.comparing(HashCryptoProp::getSorted);
    }

    @Override
    String cryptoPropText(final HashCryptoProp prop) {
        if (prop.isDirectory()) {
            return prop.getTitle();
        }
        final String template = "%s - %s ( %s )";
        return template.formatted(prop.getTitle(), prop.getDescription(), StringUtils.maskSensitive(prop.getKey()));
    }

    @Override
    boolean isDirectory(final HashCryptoProp prop) {
        return prop.isDirectory();
    }

    void initEvent() {
        this.cryptoManageBtn.addActionListener(e -> {
            final HashPropDialog dialog = new HashPropDialog(this.project, this::refresh);
            dialog.showAndGet();
        });
        this.calculation.addActionListener(e -> {
            try {
                final HashCrypto crypto = this.cryptoComboBox.getItem();
                String enc;
                if (crypto == HashCrypto.HMAC) {
                    final HashCryptoProp prop = this.cryptoPropComboBox.getItem();
                    enc = crypto.enc(this.decryptArea.getText(), prop.getKey());
                } else {
                    enc = crypto.enc(this.decryptArea.getText());
                }
                this.encryptArea.setText(enc);
            } catch (Exception ex) {
                Messages.showMessageDialog(this.project, ex.getMessage(), "Encrypt Error", Messages.getErrorIcon());
            }
        });
        this.cryptoComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final HashCrypto selected = (HashCrypto) e.getItem();
                if (selected != HashCrypto.HMAC) {
                    this.cryptoPropComboBox.setEnabled(false);
                    this.cryptoManageBtn.setEnabled(false);
                } else {
                    this.cryptoPropComboBox.setEnabled(true);
                    this.cryptoManageBtn.setEnabled(true);
                }
            }
        });
    }

    void refresh(final List<HashCryptoProp> props) {
        this.cryptoPropComboBox.removeAllItems();
        Optional.ofNullable(props).ifPresent(hashCryptoProps -> hashCryptoProps.stream()
                .sorted(Comparator.comparing(HashCryptoProp::getSorted))
                .<HashCryptoProp>mapMulti((prop, consumer) -> {
                    consumer.accept(prop);
                    prop.getChildren().stream()
                            .sorted(Comparator.comparing(HashCryptoProp::getSorted))
                            .forEach(consumer);
                })
                .forEach(this.cryptoPropComboBox::addItem));
    }

}
