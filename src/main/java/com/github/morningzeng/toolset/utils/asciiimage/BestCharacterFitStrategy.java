package com.github.morningzeng.toolset.utils.asciiimage;

/**
 * Encapsulates the algorith for choosing best fit character.
 */
public interface BestCharacterFitStrategy {

    /**
     * Returns the error between the character and tile matrices. The character
     * with miniMun error wins.
     *
     * @param character the character
     * @param tile      the tile
     * @return error. Less values mean better fit. Least value character will be
     * chosen as best fit.
     */
    float calculateError(final GrayscaleMatrix character, final GrayscaleMatrix tile);
}