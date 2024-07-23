package com.github.morningzeng.toolset.ui.enums;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.ui.DateTimestamp;
import com.github.morningzeng.toolset.ui.HttpComponent;
import com.github.morningzeng.toolset.ui.QRCodeComponent;
import com.github.morningzeng.toolset.ui.RemindsComponent;
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
    CRYPTO("Crypto", null, "Encrypt and Decrypt", true) {
        @Override
        public JComponent component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            Arrays.stream(CryptoTabEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
            return tabbedPane;
        }
    },
    TIMESTAMP("Datetime", IconC.CLOCK_COLOR, "Date and Time", true) {
        @Override
        public JComponent component(final Project project) {
            return new DateTimestamp(project);
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
    HTTP("HTTP", null, "HTTP Client", true) {
        @Override
        public JComponent component(final Project project) {
            return new HttpComponent(project);
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
    TOKEN("Token", null, "Generate and Resolve JWT", false) {
        @Override
        public JComponent component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            Arrays.stream(TokenEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
            return tabbedPane;
        }
    },

    ;

    private final String title;
    private final Icon icon;
    private final String tips;
    private final boolean load;

}