package com.github.morningzeng.toolset.ui.enums;

import com.github.morningzeng.toolset.ui.crypto.AESComponent;
import com.github.morningzeng.toolset.ui.crypto.BlowfishComponent;
import com.github.morningzeng.toolset.ui.crypto.DESComponent;
import com.github.morningzeng.toolset.ui.crypto.HashComponent;
import com.github.morningzeng.toolset.ui.crypto.SM4Component;
import com.intellij.openapi.project.Project;
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
    AES("AES", null, "AES") {
        @Override
        public JComponent component(final Project project) {
            return new AESComponent(project);
        }

    },
    BLOWFISH("Blowfish", null, "Blowfish") {
        @Override
        public JComponent component(final Project project) {
            return new BlowfishComponent(project);
        }
    },
    DES("DES", null, "DES") {
        @Override
        public JComponent component(final Project project) {
            return new DESComponent(project);
        }
    },
    HASH("Hash", null, "Hash") {
        @Override
        public JComponent component(final Project project) {
            return new HashComponent(project);
        }
    },
    SM("SM4", null, "SM4") {
        @Override
        public JComponent component(final Project project) {
            return new SM4Component(project);
        }
    },

    ;


    private final String title;
    private final Icon icon;
    private final String tips;

}
