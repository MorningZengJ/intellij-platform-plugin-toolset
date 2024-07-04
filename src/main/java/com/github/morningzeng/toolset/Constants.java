package com.github.morningzeng.toolset;

import com.google.common.net.HttpHeaders;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ReflectionUtil;

import javax.swing.Icon;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-05-14
 */
public interface Constants {

    String COLON = ":";
    String COLON_WITH_SPACE = ": ";

    String DOT = ".";

    String EQUALS_SIGN = "=";

    interface CompletionItem {

        Collection<String> HTTP_HEADERS = ReflectionUtil.collectFields(HttpHeaders.class).stream()
                .<String>mapMulti((field, consumer) -> {
                    final String value = ReflectionUtil.getFieldValue(field, null);
                    Optional.ofNullable(value).map(head -> head.concat(": ")).ifPresent(consumer);
                })
                .sorted(String::compareToIgnoreCase)
                .toList();

    }

    interface IconC {
        ClassLoader CLASS_LOADER = IconC.class.getClassLoader();
        Icon DOUBLE_ARROW_DOWN = IconLoader.getIcon("/images/svg/keyboard_double_arrow_down_24dp.svg", CLASS_LOADER);
        Icon DOUBLE_ARROW_UP = IconLoader.getIcon("/images/svg/keyboard_double_arrow_up_24dp.svg", CLASS_LOADER);

        Icon ADD_GREEN = IconLoader.getIcon("/images/svg/add_24dp_FILL0_wght400_GRAD0_opsz24.svg", CLASS_LOADER);
        Icon REMOVE_RED = IconLoader.getIcon("/images/svg/remove_24dp_FILL0_wght400_GRAD0_opsz24.svg", CLASS_LOADER);

        Icon BOX = IconLoader.getIcon("/images/svg/box.svg", CLASS_LOADER);
        Icon Time = IconLoader.getIcon("/images/svg/schedule_24dp_FILL0_wght400_GRAD0_opsz24.svg", CLASS_LOADER);

    }
}
