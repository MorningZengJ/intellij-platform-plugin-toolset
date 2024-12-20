package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.ui.coding.CodingEnum;
import com.github.morningzeng.toolset.ui.crypto.CryptoTabEnum;
import com.github.morningzeng.toolset.ui.gadget.GadgetTabEnum;
import com.github.morningzeng.toolset.ui.game.GameEnum;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.util.Arrays;

/**
 * @author Morning Zeng
 * @since 2024-05-11 14:46:41
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum TabEnum implements TabSupport {
    ASCII_IMAGE("Ascii Image", null, "Image convert ascii image", false) {
        @Override
        public JComponent component(final Project project) {
            return new AsciiImageComponent(project);
        }
    },
    CRYPTO("Crypto", null, "Encrypt and Decrypt", true) {
        @Override
        public JComponent component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            Arrays.stream(CryptoTabEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
            return tabbedPane;
        }
    },
    CODING("Encoding & Decoding", null, "Encoding and Decoding", true) {
        @Override
        public JComponent component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            Arrays.stream(CodingEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
            return tabbedPane;
        }
    },
    GADGET("Gadget", null, "Gadget", true) {
        @Override
        public JComponent component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            for (final GadgetTabEnum tab : GadgetTabEnum.values()) {
                tab.putTab(project, tabbedPane);
            }
            return tabbedPane;
        }
    },
    GAME("Game", null, "Game", true) {
        @Override
        public JComponent component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            for (final GameEnum e : GameEnum.values()) {
                e.putTab(project, tabbedPane);
            }
            return tabbedPane;
        }
    },
    HTTP("HTTP", null, "HTTP Client", true) {
        @Override
        public JComponent component(final Project project) {
            return new HttpComponent(project);
        }
    },
    JWT("JWT", null, "Generate and Resolve JWT", true) {
        @Override
        public JComponent component(final Project project) {
            return new JWTComponent(project);
        }
    },
    QRCODE("QRCode", null, "QR Code", true) {
        @Override
        public JComponent component(final Project project) {
            return new QRCodeComponent(project);
        }
    },
    REMINDS("Reminds", null, "You may need to add some reminders, to-dos?", true) {
        @Override
        public JComponent component(final Project project) {
            return new RemindsComponent(project);
        }
    },

    ;

    private final String title;
    private final Icon icon;
    private final String tips;
    private final boolean load;

}