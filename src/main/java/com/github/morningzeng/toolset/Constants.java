package com.github.morningzeng.toolset;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * @author Morning Zeng
 * @since 2024-05-14
 */
public interface Constants {

    interface IconC {
        Icon BOX = IconLoader.getIcon("/images/svg/box.svg", IconC.class.getClassLoader());
    }
}
