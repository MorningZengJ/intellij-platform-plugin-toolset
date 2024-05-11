package com.github.morningzeng.toolset.enums;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import java.awt.Component;

/**
 * @author Morning Zeng
 * @since 2024-05-09
 */
public sealed interface TabSupport permits TabEnum, CryptoTabEnum {

    String title();

    Icon icon();

    String tips();

    Component component();

    default void putTab(JTabbedPane tabbedPane) {
        tabbedPane.addTab(this.title(), this.icon(), this.component(), this.tips());
    }

}
