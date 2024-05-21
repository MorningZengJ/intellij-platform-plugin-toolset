package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.listener.ContentBorderListener;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.swing.text.Document;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FocusColorTextArea extends JBTextArea {

    @Builder.Default
    private final JBColor gained = JBColor.BLUE;
    @Builder.Default
    private final JBColor lost = JBColor.GRAY;
    private Document doc;
    private String text;
    private int row;
    private int column;

    public JBScrollPane scrollPane() {
        return this.scrollPane(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    public JBScrollPane scrollPane(final int horizontalScrollBarPolicy, final int verticalScrollBarPolicy) {
        final JBScrollPane scrollPane = new JBScrollPane(this);
        scrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
        scrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
        return scrollPane;
    }

    public static class FocusColorTextAreaBuilder {

        public FocusColorTextArea focusListener() {
            final FocusColorTextArea textArea = this.build();
            final ContentBorderListener listener = ContentBorderListener.builder()
                    .gained(textArea.gained)
                    .lost(textArea.lost)
                    .component(textArea)
                    .init();
            textArea.addFocusListener(listener);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            return textArea;
        }

    }

}
