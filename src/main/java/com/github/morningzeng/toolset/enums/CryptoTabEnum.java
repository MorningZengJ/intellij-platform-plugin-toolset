package com.github.morningzeng.toolset.enums;

import com.github.morningzeng.toolset.ui.AESComponent;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import java.awt.Component;

/**
 * @author Morning Zeng
 * @since 2024-05-09
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum CryptoTabEnum implements TabSupport {

    DES("DES", null, "DES") {
        @Override
        public Component component() {
            return new JBPanel<JBPanelWithEmptyText>();
        }
    },
    AES("AES", null, "AES") {
        @Override
        public Component component() {
            return new AESComponent();
        }

    },
    BLOWFISH("Blowfish", null, "Blowfish") {
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
