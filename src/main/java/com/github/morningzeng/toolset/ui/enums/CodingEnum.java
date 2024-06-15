package com.github.morningzeng.toolset.ui.enums;

import com.github.morningzeng.toolset.ui.Base64Component;
import com.github.morningzeng.toolset.ui.URLComponent;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum CodingEnum implements TabSupport {

    BASE64("Base64", null, "Base64 Encode and Decode") {
        @Override
        public JComponent component(final Project project) {
            return new Base64Component(project);
        }
    },
    URL("URL", null, "URL Encode and Decode") {
        @Override
        public JComponent component(final Project project) {
            return new URLComponent(project);
        }
    },
    ;


    private final String title;
    private final Icon icon;
    private final String tips;

}
