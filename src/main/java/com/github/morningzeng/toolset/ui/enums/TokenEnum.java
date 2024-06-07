package com.github.morningzeng.toolset.ui.enums;

import com.github.morningzeng.toolset.ui.JWTComponent;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * @author Morning Zeng
 * @since 2024-05-29
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum TokenEnum implements TabSupport {

    JWT("JWT", null, "JSON Web Tokens") {
        @Override
        public JComponent component(final Project project) {
            return new JWTComponent(project);
        }
    };

    private final String title;
    private final Icon icon;
    private final String tips;

}
