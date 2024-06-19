package com.github.morningzeng.toolset.ui.enums;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.ui.HttpComponent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
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
    CODING("Encoding & Decoding", null, "Encoding and Decoding", true) {
        @Override
        public JComponent component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            Arrays.stream(CodingEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
            return tabbedPane;
        }
    },
    TIMESTAMP("Date & Time", IconC.Time, "Date and Time", false) {
        @Override
        public JComponent component(final Project project) {
            return new JBPanel<JBPanelWithEmptyText>();
        }
    },
    HTTP("HTTP", null, "HTTP Client", true) {
        @Override
        public JComponent component(final Project project) {
            return new HttpComponent(project);
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