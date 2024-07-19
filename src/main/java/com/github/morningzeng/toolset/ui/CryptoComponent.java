package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.model.Children;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-07-09
 */
public sealed abstract class CryptoComponent<T extends Children<T>> extends JBPanel<JBPanelWithEmptyText> permits AESComponent, DESComponent, HashComponent {
    protected final ComboBox<T> cryptoPropComboBox = new ComboBox<>();
    protected final JButton cryptoManageBtn = new JButton(General.Ellipsis);
    protected final LanguageTextArea encryptArea;
    protected final LanguageTextArea decryptArea;
    final Project project;

    public CryptoComponent(final Project project) {
        this.project = project;
        this.encryptArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.decryptArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.encryptArea.setPlaceholder("Encrypted text content");
        this.decryptArea.setPlaceholder("Decrypted text content");

        this.reloadCryptoProps(this.getCryptoProps());
        this.setCryptoPropRenderer();
    }

    abstract List<T> getCryptoProps();

    abstract Comparator<? super T> comparator();

    abstract String cryptoPropText(final T t);

    abstract boolean isDirectory(final T t);

    void setCryptoPropRenderer() {
        this.cryptoPropComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            final JBBox box = new JBBox(BoxLayout.X_AXIS);
            if (Objects.isNull(value)) {
                return box;
            }
            final boolean directory = this.isDirectory(value);
            final Icon icon = directory ? IconC.FOLDER_COLOR : IconC.TREE_NODE;
            final JBLabel label = new JBLabel(this.cryptoPropText(value), icon, SwingConstants.LEFT);
            label.setEnabled(!directory);
            if (index == -1) {
                return label;
            }
            if (!directory && !isSelected) {
                box.add(JBBox.createHorizontalStrut(10));
            }
            box.add(label);
            return box;
        });
    }

    protected void reloadCryptoProps(final List<T> props) {
        this.cryptoPropComboBox.removeAllItems();
        Optional.ofNullable(props).ifPresent(cryptoProps -> cryptoProps.stream()
                .sorted(this.comparator())
                .<T>mapMulti((prop, consumer) -> {
                    consumer.accept(prop);
                    Optional.ofNullable(prop.getChildren()).ifPresent(
                            ts -> ts.stream()
                                    .sorted(this.comparator())
                                    .forEach(consumer)
                    );
                })
                .forEach(this.cryptoPropComboBox::addItem));
    }

}
