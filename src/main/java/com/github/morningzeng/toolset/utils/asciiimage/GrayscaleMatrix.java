package com.github.morningzeng.toolset.utils.asciiimage;


import lombok.Getter;

/**
 * A class that encapsulates a grayscale image. Color values are floats with
 * values between 0.0f and 255.0f.
 */
@Getter
public class GrayscaleMatrix {

    /**
     * Grayscale pixel data. Values are between 0.0f and 255.0f.
     * <p>
     * -- GETTER --
     * <p>
     * Gets a reference to pixel array.
     */
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private final float[] data;
    /**
     * Image width.
     * <p>
     * -- GETTER --
     * <p>
     * Gets the image width.
     */
    private final int width;
    /**
     * Image height.
     * <p>
     * -- GETTER --
     * <p>
     * Gets the image height.
     */
    private final int height;

    /**
     * Creates an empty image with the given dimensions.
     *
     * @param width  image width
     * @param height image height
     */
    public GrayscaleMatrix(final int width, final int height) {
        data = new float[width * height];
        this.width = width;
        this.height = height;
    }

    /**
     * Instantiates a new grayscale matrix from a ARGB bitmap image.
     *
     * @param pixels pixel data in ARGB format
     * @param width  image width
     * @param height image height
     */
    public GrayscaleMatrix(final int[] pixels, final int width, final int height) {
        this(width, height);

        if (width * height != pixels.length) {
            throw new IllegalArgumentException(
                    "Pixels array does not match specified width and height!");
        }

        for (int i = 0; i < data.length; i++) {
            data[i] = convertRGBToGrayscale(pixels[i]);
        }
    }

    /**
     * Creates a new matrix from a sub region.
     *
     * @param source      source matrix
     * @param width       sub region width
     * @param height      subregion height
     * @param startPixelX x coordinate of sub region start
     * @param startPixelY y coordinate of sub region start
     * @return matrix containing the specified sub region
     */
    public static GrayscaleMatrix createFromRegion(
            final GrayscaleMatrix source, final int width, final int height,
            final int startPixelX, final int startPixelY) {
        if (width <= 0 || height <= 0 || width > source.width
                || height > source.height) {
            throw new IllegalArgumentException("Illegal sub region size!");
        }

        GrayscaleMatrix output = new GrayscaleMatrix(width, height);

        for (int i = 0; i < output.data.length; i++) {
            int xOffset = i % width;
            int yOffset = i / width;

            int index = ArrayUtils.convert2DTo1D(startPixelX + xOffset,
                    startPixelY + yOffset, source.width);
            output.data[i] = source.data[index];
        }

        return output;
    }

    /**
     * Convert ARGB color to grayscale float.
     *
     * @param rgbColor ARGB color
     * @return Grayscale float with value between 0.0f and 255.0f.
     */
    private float convertRGBToGrayscale(final int rgbColor) {
        // extract components
        int red = (rgbColor >> 16) & 0xFF;
        int green = (rgbColor >> 8) & 0xFF;
        int blue = rgbColor & 0xFF;

        // convert to grayscale
        return 0.3f * red + 0.59f * green + 0.11f * blue;
    }

}