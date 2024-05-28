package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.FocusColorTextArea;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.config.LocalConfigFactory.HashCryptoProp;
import com.github.morningzeng.toolset.dialog.HashPropDialog;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.HashCrypto;
import com.github.morningzeng.toolset.utils.StringUtils;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public final class HashComponent extends JBPanel<JBPanelWithEmptyText> {

    final HashCrypto[] cryptos = Arrays.stream(HashCrypto.values())
            .toArray(HashCrypto[]::new);
    final LocalConfigFactory localConfigFactory = LocalConfigFactory.getInstance();

    private final Project project;

    private final ComboBox<HashCryptoProp> cryptoPropComboBox = new ComboBox<>(
            this.localConfigFactory.hashCryptoPropsMap().values().stream()
                    .flatMap(Collection::stream)
                    .sorted(Comparator.comparing(HashCryptoProp::getSorted))
                    .toArray(HashCryptoProp[]::new)
    );
    private final JButton cryptoManageBtn = new JButton(General.Ellipsis);
    private final ComboBox<HashCrypto> cryptoComboBox = new ComboBox<>(this.cryptos);
    private final FocusColorTextArea textArea = FocusColorTextArea.builder()
            .row(5)
            .column(20)
            .focusListener();
    private final JButton calculation = new JButton("Calculation", IconC.DOUBLE_ARROW_DOWN);
    private final FocusColorTextArea encryptTextArea = FocusColorTextArea.builder()
            .row(5)
            .column(20)
            .focusListener();

    {
        this.cryptoPropComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (Objects.isNull(value)) {
                return new JLabel();
            }
            final String template = "%s - %s ( %s )";
            return new JLabel(template.formatted(value.getTitle(), value.getDesc(), StringUtils.maskSensitive(value.getKey())));
        });
    }

    public HashComponent(final Project project) {
        this.project = project;

        this.setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.cryptoPropComboBox)
                .newCell().weightX(0).add(this.cryptoManageBtn)
                .newCell().add(this.cryptoComboBox)
                .newRow().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(this.textArea.scrollPane())
                .newRow().weightY(0).add(this.calculation)
                .newRow().weightY(1).gridWidth(3).add(this.encryptTextArea.scrollPane());

        this.cryptoManageBtn.setEnabled(false);
        this.cryptoPropComboBox.setEnabled(false);
        this.initEvent();
    }

    void initEvent() {
        this.cryptoManageBtn.addActionListener(e -> {
            final HashPropDialog dialog = new HashPropDialog(this.project);
            dialog.showAndGet();
            this.refresh();
        });
        this.calculation.addActionListener(e -> {
            try {
                final HashCrypto crypto = this.cryptoComboBox.getItem();
                String enc;
                if (crypto == HashCrypto.HMAC) {
                    final HashCryptoProp prop = this.cryptoPropComboBox.getItem();
                    enc = crypto.enc(this.textArea.getText(), prop.getKey());
                } else {
                    enc = crypto.enc(this.textArea.getText());
                }
                this.encryptTextArea.setText(enc);
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

    void refresh() {
        this.cryptoPropComboBox.removeAllItems();
        this.localConfigFactory.hashCryptoPropsMap().values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(HashCryptoProp::getSorted))
                .forEach(this.cryptoPropComboBox::addItem);
    }

}
