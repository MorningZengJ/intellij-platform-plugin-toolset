package com.github.morningzeng.toolset.component;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;

import javax.swing.BoxLayout;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-07-16
 */
public final class CheckBoxBar<T> extends JBPanel<JBPanelWithEmptyText> {

    private final Map<T, JBCheckBox> checkBoxMap;

    @SafeVarargs
    public CheckBoxBar(final T... items) {
        this(false, items);
    }

    @SafeVarargs
    public CheckBoxBar(final boolean vertical, final T... items) {
        this(vertical, Arrays.stream(items).collect(Collectors.toMap(Function.identity(), item -> false)));
    }

    public CheckBoxBar(final Map<T, Boolean> itemMap) {
        this(false, itemMap);
    }

    public CheckBoxBar(final boolean vertical, final Map<T, Boolean> itemMap) {
        this.setLayout(new BoxLayout(this, vertical ? BoxLayout.Y_AXIS : BoxLayout.LINE_AXIS));
        this.checkBoxMap = itemMap.entrySet().stream().collect(
                Collectors.toUnmodifiableMap(Entry::getKey, entry -> {
                    final JBCheckBox checkBox = new JBCheckBox(String.valueOf(entry.getKey()), entry.getValue());
                    this.add(checkBox);
                    return checkBox;
                })
        );
    }

    public Set<T> getCheckedItems() {
        return checkBoxMap.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        this.checkBoxMap.values().forEach(checkBox -> checkBox.setEnabled(enabled));
    }

    public void addChangeListener(final BiConsumer<? super T, Boolean> action) {
        this.checkBoxMap.forEach((t, checkBox) -> action.accept(t, checkBox.isSelected()));
    }
}
