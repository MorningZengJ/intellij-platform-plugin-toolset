package com.github.morningzeng.toolset.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.annotations.ScratchConfig;
import com.github.morningzeng.toolset.model.SymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.ScratchFileUtils;
import com.intellij.icons.AllIcons.General;
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
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-11-06
 */
@Slf4j
public abstract class AbstractCryptoPropComponent<T> extends JBPanel<JBPanelWithEmptyText> {

    protected final ComboBox<T> cryptoPropComboBox = new ComboBox<>();
    protected final JButton cryptoManageBtn = new JButton(General.Ellipsis);
    protected final Project project;

    public AbstractCryptoPropComponent(final Project project) {
        this.project = project;
        this.reloadCryptoProps();
        this.setCryptoPropRenderer();
    }

    protected abstract TypeReference<List<T>> typeReference();

    protected abstract Comparator<? super T> comparator();

    protected abstract String cryptoPropText(final T t);

    protected abstract boolean isDirectory(final T t);

    protected abstract void initLayout();

    protected abstract void initAction();

    protected Predicate<T> filterProp() {
        return prop -> true;
    }

    protected void setCryptoPropRenderer() {
        this.cryptoPropComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            final JBBox box = new JBBox(BoxLayout.X_AXIS);
            if (Objects.isNull(value)) {
                return box;
            }
            final boolean directory = this.isDirectory(value);
            final JComponent component = this.propComponent(value);
            if (index == -1) {
                return component;
            }
            if (!directory && !isSelected) {
                box.add(JBBox.createHorizontalStrut(10));
            }
            box.add(component);
            return box;
        });
    }

    protected JComponent propComponent(final T prop) {
        final Icon icon = this.isDirectory(prop) ? IconC.FOLDER_COLOR : IconC.TREE_NODE;
        return new JBLabel(this.cryptoPropText(prop), icon, SwingConstants.LEFT);
    }

    protected abstract Stream<T> flatProps(final List<T> props);

    protected void reloadCryptoProps() {
        try {
            final List<T> cryptoProps = Optional.ofNullable(ScratchFileUtils.read(this.typeReference()))
                    .orElse(Collections.emptyList());
            this.reloadCryptoProps(cryptoProps);
        } catch (Exception e) {
            Messages.showErrorDialog(e.getMessage(), "Configuration File Is Incorrect");
            final ScratchConfig scratchConfig = SymmetricCryptoProp.class.getAnnotation(ScratchConfig.class);
            ScratchFileUtils.openFile(this.project, scratchConfig.directory(), scratchConfig.outputType().fullName(scratchConfig.value()));
        }
    }

    protected void reloadCryptoProps(final List<T> cryptoProps) {
        this.cryptoPropComboBox.removeAllItems();
        this.flatProps(cryptoProps)
                .filter(this.filterProp())
                .sorted(this.comparator())
                .forEach(this.cryptoPropComboBox::addItem);
    }


}
