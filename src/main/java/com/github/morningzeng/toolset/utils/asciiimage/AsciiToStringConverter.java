package com.github.morningzeng.toolset.utils.asciiimage;

import java.util.Map.Entry;

/**
 * Converts ascii art to String.
 */
public class AsciiToStringConverter extends AsciiConverter<StringBuffer> {

    /**
     * Instantiates a new ascii to string converter.
     *
     * @param characterCache       the character cache
     * @param characterFitStrategy the character fit strategy
     */
    public AsciiToStringConverter(final AsciiImgCache characterCache, final BestCharacterFitStrategy characterFitStrategy) {
        super(characterCache, characterFitStrategy);
    }

    /**
     * Append choosen character to StringBuffer.
     *
     * @see AsciiConverter#addCharacterToOutput(Entry, int[], int, int, int)
     */
    @Override
    public void addCharacterToOutput(final Entry<Character, GrayscaleMatrix> characterEntry,
                                     final int[] sourceImagePixels, final int tileX, final int tileY, final int imageWidth) {

        output.append(characterEntry.getKey());

        // append new line at the end of the row
        if ((tileX + 1) * characterCache.getCharacterImageSize().getWidth() == imageWidth) {
            output.append(System.lineSeparator());
        }

    }

    /**
     * @see AsciiConverter#finalizeOutput(int[], int, int)
     */
    @Override
    protected void finalizeOutput(final int[] sourceImagePixels, final int imageWidth, int imageHeight) {

    }

    /**
     * Creates an empty string buffer;
     *
     * @see AsciiConverter#initializeOutput(int, int)
     */
    @Override
    protected StringBuffer initializeOutput(final int imageWidth, final int imageHeight) {
        return new StringBuffer();
    }

}