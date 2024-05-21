package com.github.morningzeng.toolset;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * @author Morning Zeng
 * @since 2024-05-14
 */
public interface Constants {

    interface IconC {
        Icon DOUBLE_ARROW_DOWN = IconLoader.getIcon("/images/svg/keyboard_double_arrow_down_24dp.svg", IconC.class.getClassLoader());
        Icon DOUBLE_ARROW_UP = IconLoader.getIcon("/images/svg/keyboard_double_arrow_up_24dp.svg", IconC.class.getClassLoader());

        Icon BOX = IconLoader.getIcon("/images/svg/box.svg", IconC.class.getClassLoader());
        Icon Time = IconLoader.getIcon("/images/svg/schedule_24dp_FILL0_wght400_GRAD0_opsz24.svg", IconC.class.getClassLoader());

    }
}
