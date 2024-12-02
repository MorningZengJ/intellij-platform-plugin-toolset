package com.github.morningzeng.toolset.utils.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SudokuUtils {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private static final int EMPTY = 0;
    private final int[][] board;

    public SudokuUtils() {
        board = new int[SIZE][SIZE];
    }

    public static void main(String[] args) {
        SudokuUtils generator = new SudokuUtils();
        generator.generate();
        generator.printBoard();
    }

    public int[][] generate() {
        fillDiagonal();
        fillRemaining(0, SUBGRID_SIZE);
        removeDigits(20);
        return board;
    }

    private void fillDiagonal() {
        for (int i = 0; i < SIZE; i += SUBGRID_SIZE) {
            fillSubGrid(i, i);
        }
    }

    private void fillSubGrid(int row, int col) {
        Integer[] num = {
                1, 2, 3, 4, 5, 6, 7, 8, 9
        };
        List<Integer> numbers = Arrays.asList(num);
        Collections.shuffle(numbers);

        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                board[row + i][col + j] = numbers.get(i * SUBGRID_SIZE + j);
            }
        }
    }

    private boolean fillRemaining(int i, int j) {
        if (j >= SIZE && i < SIZE - 1) {
            i++;
            j = 0;
        }
        if (i >= SIZE && j >= SIZE) {
            return true;
        }
        if (i < SUBGRID_SIZE) {
            if (j < SUBGRID_SIZE) {
                j = SUBGRID_SIZE;
            }
        } else if (i < SIZE - SUBGRID_SIZE) {
            if (j == (i / SUBGRID_SIZE) * SUBGRID_SIZE) {
                j += SUBGRID_SIZE;
            }
        } else {
            if (j == SIZE - SUBGRID_SIZE) {
                i++;
                j = 0;
                if (i >= SIZE) {
                    return true;
                }
            }
        }

        for (int num = 1; num <= SIZE; num++) {
            if (isSafe(i, j, num)) {
                board[i][j] = num;
                if (fillRemaining(i, j + 1)) {
                    return true;
                }
                board[i][j] = EMPTY;
            }
        }
        return false;
    }

    private void removeDigits(int count) {
        while (count != 0) {
            int cellId = (int) (Math.random() * SIZE * SIZE);

            int i = (cellId / SIZE);
            int j = cellId % SIZE;

            if (board[i][j] != EMPTY) {
                count--;
                board[i][j] = EMPTY;
            }
        }
    }

    private boolean isSafe(int i, int j, int num) {
        return !usedInRow(i, num) && !usedInCol(j, num) && !usedInBox(i - i % SUBGRID_SIZE, j - j % SUBGRID_SIZE, num);
    }

    private boolean usedInRow(int i, int num) {
        for (int j = 0; j < SIZE; j++) {
            if (board[i][j] == num) {
                return true;
            }
        }
        return false;
    }

    private boolean usedInCol(int j, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[i][j] == num) {
                return true;
            }
        }
        return false;
    }

    private boolean usedInBox(int boxStartRow, int boxStartCol, int num) {
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                if (board[i + boxStartRow][j + boxStartCol] == num) {
                    return true;
                }
            }
        }
        return false;
    }

    public void printBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}