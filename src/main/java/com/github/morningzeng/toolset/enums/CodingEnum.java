package com.github.morningzeng.toolset.enums;

import com.github.morningzeng.toolset.ui.Base64Component;
import com.github.morningzeng.toolset.ui.URLComponent;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import java.awt.Component;

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
        public Component component(final Project project) {
            return new Base64Component();
        }
    },
    URL("URL", null, "URL Encode and Decode") {
        @Override
        public Component component(final Project project) {
            return new URLComponent();
        }
    },
    ;


    private final String title;
    private final Icon icon;
    private final String tips;

}
