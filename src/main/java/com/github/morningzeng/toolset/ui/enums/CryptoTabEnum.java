package com.github.morningzeng.toolset.ui.enums;

import com.github.morningzeng.toolset.ui.AESComponent;
import com.github.morningzeng.toolset.ui.DESComponent;
import com.github.morningzeng.toolset.ui.HashComponent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * @author Morning Zeng
 * @since 2024-05-09
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum CryptoTabEnum implements TabSupport {
    HASH("Hash", null, "Hash") {
        @Override
        public JComponent component(final Project project) {
            return new HashComponent(project);
        }
    },
    DES("DES", null, "DES") {
        @Override
        public JComponent component(final Project project) {
            return new DESComponent(project);
        }
    },
    AES("AES", null, "AES") {
        @Override
        public JComponent component(final Project project) {
            return new AESComponent(project);
        }

    },
    BLOWFISH("Blowfish", null, "Blowfish") {
        @Override
        public JComponent component(final Project project) {
            return new JBPanel<JBPanelWithEmptyText>();
        }
    },

    ;


    private final String title;
    private final Icon icon;
    private final String tips;

}
