package com.github.morningzeng.toolset.enums;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTabbedPane;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import java.awt.Component;
import java.util.Arrays;

/**
 * @author Morning Zeng
 * @since 2024-05-11 14:46:41
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum TabEnum implements TabSupport {
    SYMMETRIC_CRYPTO("Symmetric Crypto", null, "Crypto Encrypt and Decrypt") {
        @Override
        public Component component() {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            Arrays.stream(CryptoTabEnum.values()).forEach(tab -> tab.putTab(tabbedPane));
            return tabbedPane;
        }
    },
    BASE64("Encoding & Decoding", null, "Encoding and Decoding") {
        @Override
        public Component component() {
            return new JBPanel<JBPanelWithEmptyText>();
        }
    },

    ;

    private final String title;
    private final Icon icon;
    private final String tips;

}