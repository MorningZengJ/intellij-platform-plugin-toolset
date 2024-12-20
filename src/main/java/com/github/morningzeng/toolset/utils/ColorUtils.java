package com.github.morningzeng.toolset.utils;

import com.intellij.ui.JBColor;

import java.awt.Color;

/**
 * @author Morning Zeng
 * @since 2024-11-25
 */
public final class ColorUtils {

    public static JBColor from(int rgb) {
        return new JBColor(rgb, rgb);
    }

    public static JBColor from(int rgb, int darkRgb) {
        return new JBColor(rgb, darkRgb);
    }

    public static JBColor invert(Color color) {
        return invert(color, color);
    }

    public static JBColor invert(Color color, Color darkColor) {
        return new JBColor(new Color(
                255 - color.getRed(),
                255 - color.getGreen(),
                255 - color.getBlue()
        ), new Color(
                255 - darkColor.getRed(),
                255 - darkColor.getGreen(),
                255 - darkColor.getBlue()
        ));
    }


}
