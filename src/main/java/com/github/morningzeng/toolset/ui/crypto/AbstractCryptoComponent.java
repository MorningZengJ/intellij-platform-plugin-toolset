package com.github.morningzeng.toolset.ui.crypto;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.annotations.ScratchConfig;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.model.Children;
import com.github.morningzeng.toolset.model.SymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import lombok.extern.slf4j.Slf4j;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Morning Zeng
 * @since 2024-07-09
 */
@Slf4j
public sealed abstract class AbstractCryptoComponent<T extends Children<T>> extends JBPanel<JBPanelWithEmptyText> permits AbstractSymmetricCryptoComponent, AsymmetricComponent, HashComponent {
    protected final ComboBox<T> cryptoPropComboBox = new ComboBox<>();
    protected final JButton cryptoManageBtn = new JButton(General.Ellipsis);
    protected final LanguageTextArea encryptArea;
    protected final LanguageTextArea decryptArea;
    final Project project;

    public AbstractCryptoComponent(final Project project) {
        this.project = project;
        this.encryptArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.decryptArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.encryptArea.setPlaceholder("Encrypted text content");
        this.decryptArea.setPlaceholder("Decrypted text content");

        List<T> cryptoProps = Collections.emptyList();
        try {
            cryptoProps = this.getCryptoProps();
        } catch (Exception e) {
            log.error("e: ", e);
            Messages.showErrorDialog(e.getMessage(), "Configuration File Is Incorrect");
            final ScratchConfig scratchConfig = SymmetricCryptoProp.class.getAnnotation(ScratchConfig.class);
            ScratchFileUtils.openFile(this.project, scratchConfig.directory(), scratchConfig.value());
        }
        this.reloadCryptoProps(cryptoProps);
        this.setCryptoPropRenderer();
    }

    abstract List<T> getCryptoProps();

    abstract Comparator<? super T> comparator();

    abstract String cryptoPropText(final T t);

    abstract boolean isDirectory(final T t);

    abstract void initLayout();

    abstract void initAction();

    Predicate<T> filterProp() {
        return prop -> true;
    }

    void setCryptoPropRenderer() {
        this.cryptoPropComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            final JBBox box = new JBBox(BoxLayout.X_AXIS);
            if (Objects.isNull(value)) {
                return box;
            }
            final boolean directory = this.isDirectory(value);
            final Icon icon = directory ? IconC.FOLDER_COLOR : IconC.TREE_NODE;
            final JBLabel label = new JBLabel(this.cryptoPropText(value), icon, SwingConstants.LEFT);
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
                .filter(this.filterProp())
                .sorted(this.comparator())
                .<T>mapMulti((prop, consumer) -> {
                    consumer.accept(prop);
                    Optional.ofNullable(prop.getChildren()).ifPresent(
                            ts -> ts.stream()
                                    .filter(this.filterProp())
                                    .sorted(this.comparator())
                                    .forEach(consumer)
                    );
                })
                .forEach(this.cryptoPropComboBox::addItem));
    }

}
