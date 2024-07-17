package com.github.morningzeng.toolset.component;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBRadioButton;

import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-07-17
 */
public final class RadioBar<T> extends JBPanel<JBPanelWithEmptyText> {

    private final Map<T, JBRadioButton> radioMap;
    private volatile T selectedItem;

    @SafeVarargs
    public RadioBar(final T defaultCheck, final T... radios) {
        this(defaultCheck, Arrays.asList(radios));
    }

    public RadioBar(final T defaultCheck, final List<T> radios) {
        this.radioMap = radios.stream().collect(
                Collectors.toMap(Function.identity(), item -> {
                    final JBRadioButton radio = new JBRadioButton(item.toString(), item.equals(defaultCheck));
                    this.add(radio);
                    return radio;
                })
        );
        this.radioMap.forEach((t, radio) -> radio.addItemListener(e -> {
            final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            if (selected) {
                this.selectedItem = t;
                radioMap.values().stream()
                        .filter(Predicate.not(radio::equals))
                        .forEach(item -> item.setSelected(false));
            } else {
                this.selectedItem = null;
            }
        }));
    }

    public void addChangeListener(final Consumer<? super T> action) {
        this.radioMap.values().forEach(radio -> radio.addChangeListener(e -> action.accept(this.selectedItem)));
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        this.radioMap.values().forEach(radio -> radio.setEnabled(enabled));
    }
}
