package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.listener.ContentBorderListener;
import com.github.morningzeng.toolset.support.ScrollSupport;
import com.intellij.ui.JBColor;
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
public final class FocusColorTextArea extends JBTextArea implements ScrollSupport<FocusColorTextArea> {

    @Builder.Default
    private final JBColor gained = JBColor.BLUE;
    @Builder.Default
    private final JBColor lost = JBColor.GRAY;
    private Document doc;
    private String text;
    private int row;
    private int column;

    @Override
    public FocusColorTextArea getComponent() {
        return this;
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
