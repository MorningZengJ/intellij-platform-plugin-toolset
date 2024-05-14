package com.github.morningzeng.toolset.listener;

import com.intellij.ui.JBColor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author Morning Zeng
 * @since 2024-05-14
 */
@Builder
@AllArgsConstructor
public class ContentBorderListener extends FocusAdapter {

    @Builder.Default
    private final JBColor gained = JBColor.BLUE;
    @Builder.Default
    private final JBColor lost = JBColor.GRAY;
    private JComponent component;

    @Override
    public void focusGained(final FocusEvent e) {
        this.component.setBorder(BorderFactory.createLineBorder(this.gained, 1));
    }

    @Override
    public void focusLost(final FocusEvent e) {
        this.component.setBorder(BorderFactory.createLineBorder(this.lost, 1));
    }

    public static class ContentBorderListenerBuilder {
        public ContentBorderListener init() {
            final ContentBorderListener build = this.build();
            build.component.setBorder(BorderFactory.createLineBorder(build.lost, 1));
            return build;
        }
    }

}
