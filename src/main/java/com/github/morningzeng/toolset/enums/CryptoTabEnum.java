package com.github.morningzeng.toolset.enums;

import com.github.morningzeng.toolset.ui.AESComponent;
import com.intellij.openapi.project.Project;
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
        public Component component(final Project project) {
            return new JBPanel<JBPanelWithEmptyText>();
        }
    },
    AES("AES", null, "AES") {
        @Override
        public Component component(final Project project) {
            return new AESComponent(project);
        }

    },
    BLOWFISH("Blowfish", null, "Blowfish") {
        @Override
        public Component component(final Project project) {
            return new JBPanel<JBPanelWithEmptyText>();
        }
    },

    ;


    private final String title;
    private final Icon icon;
    private final String tips;

}
