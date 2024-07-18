package com.github.morningzeng.toolset.component;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBRadioButton;

import javax.swing.ButtonGroup;
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
    private final ButtonGroup group;

    @SafeVarargs
    public RadioBar(final T defaultCheck, final T... radios) {
        this(defaultCheck, Arrays.asList(radios));
    }

    public RadioBar(final T defaultCheck, final List<T> radios) {
        this.group = new ButtonGroup();
        this.radioMap = radios.stream().collect(
                Collectors.toMap(Function.identity(), item -> {
                    final JBRadioButton radio = new JBRadioButton(item.toString(), item.equals(defaultCheck));
                    this.add(radio);
                    this.group.add(radio);
                    return radio;
                })
        );
        this.radioMap.forEach((t, radio) -> radio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                radioMap.values().stream()
                        .filter(Predicate.not(radio::equals))
                        .forEach(item -> item.setSelected(false));
            }
        }));
    }

    public void addItemListener(final Consumer<? super T> action) {
        this.radioMap.keySet().forEach(t -> this.addItemListener(t, action));
    }

    public void addItemListener(final T t, final Consumer<? super T> action) {
        this.radioMap.get(t).addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                action.accept(t);
            }
        });
    }

    public void addItemListener(final Consumer<? super T> action, Predicate<T> predicate) {
        this.radioMap.keySet().stream()
                .filter(predicate)
                .forEach(t -> this.addItemListener(t, action));
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        this.radioMap.values().forEach(radio -> radio.setEnabled(enabled));
    }
}
