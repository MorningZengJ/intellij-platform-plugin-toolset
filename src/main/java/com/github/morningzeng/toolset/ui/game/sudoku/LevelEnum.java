package com.github.morningzeng.toolset.ui.game.sudoku;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Morning Zeng
 * @since 2024-12-20
 */
@Getter
@AllArgsConstructor
enum LevelEnum {
    EASY("☆", 20),
    MEDIUM("☆☆", 30),
    HARD("☆☆☆", 40),
    SUPER_DIFFICULT("☆☆☆☆", 50),
    EXTREME_DIFFICULT("☆☆☆☆☆", 60),
    ;

    private final String symbol;
    private final int factor;
}
