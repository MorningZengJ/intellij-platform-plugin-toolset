package com.github.morningzeng.toolset.support;

import com.intellij.ui.components.JBScrollPane;

import java.awt.Component;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public interface ScrollSupport<T extends Component> {

    static <T extends Component> ScrollSupport<T> getInstance(final T component) {
        return () -> component;
    }

    T getComponent();

    default JBScrollPane scrollPane() {
        final T component = this.getComponent();
        if (component instanceof JBScrollPane) {
            return (JBScrollPane) component;
        }
        return new JBScrollPane(component);
    }

    default JBScrollPane verticalAsNeededScrollPane() {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    default JBScrollPane verticalAlwaysScrollPane() {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    default JBScrollPane horizontalAsNeededScrollPane() {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, JBScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }

    default JBScrollPane horizontalAlwaysScrollPane() {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, JBScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }

    default JBScrollPane asNeedScrollPane() {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    default JBScrollPane alwaysScrollPane() {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    default JBScrollPane scrollPane(final int horizontalScrollBarPolicy, final int verticalScrollBarPolicy) {
        final JBScrollPane scrollPane = this.scrollPane();
        scrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
        scrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
        return scrollPane;
    }

}
