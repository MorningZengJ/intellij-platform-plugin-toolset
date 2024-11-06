package com.github.morningzeng.toolset.ui.enums;

import com.intellij.openapi.project.Project;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

/**
 * @author Morning Zeng
 * @since 2024-05-09
 */
public sealed interface TabSupport permits CodingEnum, CryptoTabEnum, GadgetTabEnum, TabEnum {

    String title();

    Icon icon();

    String tips();

    JComponent component(final Project project);

    default void putTab(final Project project, JTabbedPane tabbedPane) {
        tabbedPane.addTab(this.title(), this.icon(), this.component(project), this.tips());
    }

}
