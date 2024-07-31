package com.github.morningzeng.toolset.component;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBImageIcon;

import javax.swing.UIManager;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-07-30
 */
public final class ImageLabel extends JBLabel {

    private final JBImageIcon imageIcon;

    public ImageLabel(final int width, final int height) {
        this(ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB));
    }

    public ImageLabel(final BufferedImage image) {
        this(new JBImageIcon(image));
    }

    public ImageLabel(final JBImageIcon image) {
        super(image);
        this.imageIcon = image;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (Objects.isNull(this.getIcon())) {
            return;
        }
        final JBImageIcon icon = (JBImageIcon) this.getIcon();
        final Image image = icon.getImage();
        final int size = Math.min(this.getWidth(), this.getHeight());
        g.setColor(UIManager.getColor("Label.background"));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.drawImage(image, 0, 0, size, size, this);
    }

    public void setImage(final BufferedImage image) {
        this.imageIcon.setImage(image);
        this.setIcon(this.imageIcon);
        this.repaint();
    }

}
