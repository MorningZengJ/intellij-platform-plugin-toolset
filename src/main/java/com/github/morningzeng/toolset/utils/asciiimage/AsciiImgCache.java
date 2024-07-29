package com.github.morningzeng.toolset.utils.asciiimage;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Character cache that keeps a map of precalculated pixel data of each
 * character that is eligible for ascii art.
 */
public class AsciiImgCache implements Iterable<Entry<Character, GrayscaleMatrix>> {

    /**
     * Some empirically chosen characters that give good results.
     */
    private static final char[] defaultCharacters = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. ".toCharArray();
    /**
     * A map of characters to their bitmaps.
     */
    protected final Map<Character, GrayscaleMatrix> imageCache;
    /**
     * Dimension of character image data.
     * <p>
     * -- GETTER --
     * <p>
     * Gets the character image dimensions.
     * <p>
     * character image dimensions
     */
    @Getter
    private final Dimension characterImageSize;

    /**
     * Instantiates a new ascii img cache.
     *
     * @param characterImageSize the character image size
     * @param imageCache         the image cache
     */
    private AsciiImgCache(final Dimension characterImageSize, final Map<Character, GrayscaleMatrix> imageCache) {
        this.characterImageSize = characterImageSize;
        this.imageCache = imageCache;
    }

    /**
     * Calculate character rectangle for the given font metrics.
     *
     * @return the rectangle
     */
    private static Dimension calculateCharacterRectangle(final Font font, final char[] characters) {
        BufferedImage img = ImageUtil.createImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(font);
        FontMetrics fm = graphics.getFontMetrics();

        Dimension maxCharacter = new Dimension();
        for (char c : characters) {
            String character = Character.toString(c);

            Rectangle characterRectangle = new TextLayout(character, fm.getFont(), fm.getFontRenderContext())
                    .getOutline(null)
                    .getBounds();

            if (maxCharacter.width < characterRectangle.getWidth()) {
                maxCharacter.width = (int) characterRectangle.getWidth();
            }

            if (maxCharacter.height < characterRectangle.getHeight()) {
                maxCharacter.height = (int) characterRectangle.getHeight();
            }
        }

        return maxCharacter;
    }

    /**
     * Creates the cache with supplied font.
     *
     * @param font the font
     * @return the ascii img cache
     */
    public static AsciiImgCache create(final Font font) {
        return create(font, defaultCharacters);
    }

    /**
     * Initialize a new character cache with supplied font.
     *
     * @param font the font
     * @return the ascii img cache
     */
    public static AsciiImgCache create(final Font font, final char[] characters) {

        Dimension maxCharacterImageSize = calculateCharacterRectangle(font, characters);
        Map<Character, GrayscaleMatrix> imageCache = createCharacterImages(font, maxCharacterImageSize, characters);

        return new AsciiImgCache(maxCharacterImageSize, imageCache);

    }

    /**
     * Creates the character images.
     *
     * @param font          the font
     * @param characterSize the character size
     * @return the map
     */
    private static Map<Character, GrayscaleMatrix> createCharacterImages(final Font font, final Dimension characterSize, final char[] characters) {
        // create each image
        BufferedImage img = ImageUtil.createImage(characterSize.width, characterSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(font);
        FontMetrics fm = graphics.getFontMetrics();

        Map<Character, GrayscaleMatrix> imageCache = new HashMap<>();

        for (char c : characters) {
            String character = Character.toString(c);

            g.setColor(JBColor.WHITE);
            g.fillRect(0, 0, characterSize.width, characterSize.height);
            g.setColor(JBColor.BLACK);

            Rectangle rect = new TextLayout(character, fm.getFont(), fm.getFontRenderContext()).getOutline(null).getBounds();

            g.drawString(character, 0, (int) (rect.getHeight() - rect.getMaxY()));

            int[] pixels = img.getRGB(0, 0, characterSize.width, characterSize.height, null, 0, characterSize.width);
            GrayscaleMatrix matrix = new GrayscaleMatrix(pixels, characterSize.width, characterSize.height);
            imageCache.put(c, matrix);
        }

        return imageCache;
    }

    /**
     * @see Iterable#iterator()
     */
    @Override
    public @NotNull Iterator<Entry<Character, GrayscaleMatrix>> iterator() {
        return imageCache.entrySet().iterator();
    }

}