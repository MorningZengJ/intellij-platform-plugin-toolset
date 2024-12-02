package com.github.morningzeng.toolset.ui.game;

import com.github.morningzeng.toolset.ui.TabSupport;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * @author Morning Zeng
 * @since 2024-11-22
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum GameEnum implements TabSupport {
    SUDOKU("Sudoku", null, "Sudoku") {
        @Override
        public JComponent component(final Project project) {
            return new SudokuComponent(project);
        }
    },
    ;

    private final String title;
    private final Icon icon;
    private final String tips;

}
