package com.github.morningzeng.toolset.utils.qrcode;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;
import lombok.AllArgsConstructor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-07-26
 */
@AllArgsConstructor
public abstract sealed class AbstractQRCodeWriter permits QRCodeRoundWriter {

    private static final int QUIET_ZONE_SIZE = 4;
    protected Color onColor;
    protected Color offColor;

    public BufferedImage createQRCode(final String contents, final int width, final int height) throws WriterException {
        return this.createQRCode(contents, width, height, null);
    }

    public BufferedImage createQRCode(final String contents, final int width, final int height, final Map<EncodeHintType, ?> hints) throws WriterException {
        this.verification(contents, width, height);
        final ErrorCorrectionLevel errorCorrectionLevel = this.getOrDefault(hints, EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        final int quietZone = this.getOrDefault(hints, EncodeHintType.MARGIN, QUIET_ZONE_SIZE);
        final QRCode code = Encoder.encode(contents, errorCorrectionLevel, hints);
        return this.renderResult(code, width, height, quietZone);
    }

    protected BufferedImage renderResult(final QRCode code, final int width, final int height, final int quietZone) {
        final ByteMatrix matrix = code.getMatrix();
        if (Objects.isNull(matrix)) {
            throw new IllegalStateException();
        }

        final int inputWidth = matrix.getWidth();
        final int inputHeight = matrix.getHeight();
        final int qrWidth = inputWidth + (quietZone * 2);
        final int qrHeight = inputHeight + (quietZone * 2);
        final int outputWidth = Math.max(width, qrWidth);
        final int outputHeight = Math.max(height, qrHeight);

        final int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        final int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        final int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

        final BufferedImage image = ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_RGB);

        final Graphics2D graphics2D = image.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setBackground(this.offColor);
        graphics2D.clearRect(0, 0, width, height);
        graphics2D.setColor(this.onColor);
        this.setRegion(matrix, inputWidth, inputHeight, multiple, leftPadding, topPadding, graphics2D);

        return image;
    }

    protected abstract void setRegion(final ByteMatrix matrix, final int inputWidth, final int inputHeight, final int multiple, final int leftPadding, final int topPadding, final Graphics2D graphics2D);

    <T> T getOrDefault(final Map<EncodeHintType, ?> hints, final EncodeHintType hintType, final T defaultValue) {
        if (Objects.isNull(hints)) {
            return defaultValue;
        }
        final Object value = hints.get(hintType);
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        //noinspection unchecked
        return (T) value;
    }

    public int getBufferedImageColorModel() {
        if (this.onColor == JBColor.BLACK && this.offColor == JBColor.WHITE) {
            // Use faster BINARY if colors match default
            return BufferedImage.TYPE_BYTE_BINARY;
        }
        if (this.hasTransparency(this.onColor.getRGB()) || this.hasTransparency(this.offColor.getRGB())) {
            // Use ARGB representation if colors specify non-opaque alpha
            return BufferedImage.TYPE_INT_ARGB;
        }
        // Default otherwise to RGB representation with ignored alpha channel
        return BufferedImage.TYPE_INT_RGB;
    }

    void verification(final String contents, final int width, final int height) {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Requested dimensions are too small: " + width + 'x' + height);
        }
    }

    boolean hasTransparency(int argb) {
        return (argb & 0xFF000000) != 0xFF000000;
    }
}
