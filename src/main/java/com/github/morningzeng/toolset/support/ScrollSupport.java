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

    default JBScrollPane verticalAsNeededScrollPane() {
        return this.verticalAsNeededScrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    default JBScrollPane verticalAlwaysScrollPane() {
        return this.verticalAsNeededScrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    default JBScrollPane horizontalPolicy(final int horizontalScrollBarPolicy) {
        final JBScrollPane scrollPane = new JBScrollPane(this.getComponent());
        scrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
        return scrollPane;
    }

    default JBScrollPane verticalPolicy(final int verticalScrollBarPolicy) {
        final JBScrollPane scrollPane = new JBScrollPane(this.getComponent());
        scrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
        return scrollPane;
    }

    default JBScrollPane verticalAsNeededScrollPane(final int horizontalScrollBarPolicy, final int verticalScrollBarPolicy) {
        final JBScrollPane scrollPane = new JBScrollPane(this.getComponent());
        scrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
        scrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
        return scrollPane;
    }

}
