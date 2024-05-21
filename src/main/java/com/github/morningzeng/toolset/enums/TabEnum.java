package com.github.morningzeng.toolset.enums;

import com.github.morningzeng.toolset.Constants.IconC;
import com.intellij.openapi.project.Project;
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
    CRYPTO("Crypto", null, "Encrypt and Decrypt") {
        @Override
        public Component component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            Arrays.stream(CryptoTabEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
            return tabbedPane;
        }
    },
    CODING("Encoding & Decoding", null, "Encoding and Decoding") {
        @Override
        public Component component(final Project project) {
            final JBTabbedPane tabbedPane = new JBTabbedPane(JBTabbedPane.LEFT);
            Arrays.stream(CodingEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
            return tabbedPane;
        }
    },
    TIMESTAMP("Date & Time", IconC.Time, "Date and Time") {
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