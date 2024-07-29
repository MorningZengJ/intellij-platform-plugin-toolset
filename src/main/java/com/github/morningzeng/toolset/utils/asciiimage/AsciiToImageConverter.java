package com.github.morningzeng.toolset.utils.asciiimage;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map.Entry;

/**
 * Converts ascii art to a BufferedImage.
 */
public class AsciiToImageConverter extends AsciiConverter<BufferedImage> {

    /**
     * Instantiates a new ascii to image converter.
     *
     * @param characterCache       the character cache
     * @param characterFitStrategy the character fit strategy
     */
    public AsciiToImageConverter(final AsciiImgCache characterCache, final BestCharacterFitStrategy characterFitStrategy) {
        super(characterCache, characterFitStrategy);
    }

    /**
     * Copy image data over the source pixels image.
     *
     * @see AsciiConverter#addCharacterToOutput(Entry, int[], int, int, int)
     */
    @Override
    public void addCharacterToOutput(final Entry<Character, GrayscaleMatrix> characterEntry,
                                     final int[] sourceImagePixels, final int tileX, final int tileY, final int imageWidth) {
        int startCoordinateX = tileX * characterCache.getCharacterImageSize().width;
        int startCoordinateY = tileY * characterCache.getCharacterImageSize().height;

        // copy winner character
        for (int i = 0; i < characterEntry.getValue().getData().length; i++) {
            int xOffset = i % characterCache.getCharacterImageSize().width;
            int yOffset = i / characterCache.getCharacterImageSize().width;

            int component = (int) characterEntry.getValue().getData()[i];
            final int idx = ArrayUtils.convert2DTo1D(startCoordinateX
                    + xOffset, startCoordinateY + yOffset, imageWidth);
            sourceImagePixels[idx] = new JBColor(new Color(component, component, component), new Color(component, component, component)).getRGB();
        }

    }

    /**
     * Write pixels to output image.
     *
     * @see AsciiConverter#finalizeOutput(int[], int, int)
     */
    @Override
    protected void finalizeOutput(final int[] sourceImagePixels, final int imageWidth, final int imageHeight) {
        output.setRGB(0, 0, imageWidth, imageHeight, sourceImagePixels, 0, imageWidth);

    }

    /**
     * Create an empty buffered image.
     */
    @Override
    protected BufferedImage initializeOutput(final int imageWidth, final int imageHeight) {
        return ImageUtil.createImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
    }

}