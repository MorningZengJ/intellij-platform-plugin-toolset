package com.github.morningzeng.toolset.ui.enums;

import com.github.morningzeng.toolset.Constants.IconC;
import com.github.morningzeng.toolset.ui.gadget.DateTimestampComponent;
import com.github.morningzeng.toolset.ui.gadget.UUIDComponent;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * @author Morning Zeng
 * @since 2024-05-09
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum GadgetTabEnum implements TabSupport {
    TIMESTAMP("Datetime", IconC.CLOCK_COLOR, "Date and Time") {
        @Override
        public JComponent component(final Project project) {
            return new DateTimestampComponent(project);
        }
    },
    UUID("UUID", null, "UUID") {
        @Override
        public JComponent component(final Project project) {
            return new UUIDComponent(project);
        }
    };


    private final String title;
    private final Icon icon;
    private final String tips;

}
