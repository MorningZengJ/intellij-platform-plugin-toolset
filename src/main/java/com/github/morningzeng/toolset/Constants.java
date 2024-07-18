package com.github.morningzeng.toolset;

import com.google.common.net.HttpHeaders;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ReflectionUtil;

import javax.swing.Icon;
import java.time.format.DateTimeFormatter;
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

    interface DateFormatter {
        DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    interface IconC {

        ClassLoader CLASS_LOADER = IconC.class.getClassLoader();
        Icon DOUBLE_ARROW_DOWN = IconLoader.getIcon("/images/svg/keyboard_double_arrow_down_24dp.svg", CLASS_LOADER);
        Icon DOUBLE_ARROW_UP = IconLoader.getIcon("/images/svg/keyboard_double_arrow_up_24dp.svg", CLASS_LOADER);

        Icon ADD_GREEN = IconLoader.getIcon("/images/svg/add_drawer.svg", CLASS_LOADER);
        Icon REMOVE_RED = IconLoader.getIcon("/images/svg/remove.svg", CLASS_LOADER);

        Icon BOX = IconLoader.getIcon("/images/svg/box.svg", CLASS_LOADER);
        Icon Time = IconLoader.getIcon("/images/svg/clock_color.svg", CLASS_LOADER);

        Icon SAVE = IconLoader.getIcon("/images/svg/save.svg", CLASS_LOADER);
        Icon SAVE_ALL = IconLoader.getIcon("/images/svg/save_all.svg", CLASS_LOADER);

        Icon FOLDER_COLOR = IconLoader.getIcon("/images/svg/folder_color.svg", CLASS_LOADER);
        Icon TREE_NODE = IconLoader.getIcon("/images/svg/tree_node.svg", CLASS_LOADER);

        Icon CLOCK_COLOR = IconLoader.getIcon("/images/svg/clock_color.svg", CLASS_LOADER);

        interface HttpMethod {
            Icon GET = IconLoader.getIcon("/images/svg/http_method/get.svg", CLASS_LOADER);
            Icon POST = IconLoader.getIcon("/images/svg/http_method/post.svg", CLASS_LOADER);
            Icon PUT = IconLoader.getIcon("/images/svg/http_method/put.svg", CLASS_LOADER);
            Icon PATCH = IconLoader.getIcon("/images/svg/http_method/patch.svg", CLASS_LOADER);
            Icon HTTP_DELETE = IconLoader.getIcon("/images/svg/http_method/delete.svg", CLASS_LOADER);
            Icon HEAD = IconLoader.getIcon("/images/svg/http_method/head.svg", CLASS_LOADER);
            Icon OPTIONS = IconLoader.getIcon("/images/svg/http_method/options.svg", CLASS_LOADER);
            Icon TRACE = IconLoader.getIcon("/images/svg/http_method/trace.svg", CLASS_LOADER);
        }

    }
}
