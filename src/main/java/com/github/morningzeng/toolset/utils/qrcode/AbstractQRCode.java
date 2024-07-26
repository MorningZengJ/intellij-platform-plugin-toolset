package com.github.morningzeng.toolset.utils.qrcode;

import com.google.common.collect.Maps;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author Morning Zeng
 * @since 2024-07-25
 */
@Getter
@Accessors(fluent = true)
@SuperBuilder(buildMethodName = "_build")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract sealed class AbstractQRCode permits QRCodeRect, QRCodeRound {

    protected final static String BASE64_PICTURE_PREFIX = "data:image/jpg;base64,";
    protected final static Base64.Encoder ENCODER = Base64.getEncoder();
    protected final static Base64.Decoder DECODER = Base64.getDecoder();
    /**
     * parameters
     */
    protected final Map<EncodeHintType, Object> hints = Maps.newHashMap();
    @Builder.Default
    protected final Color onColor = JBColor.BLACK;
    @Builder.Default
    protected final Color offColor = JBColor.WHITE;
    @Builder.Default
    protected final ErrorCorrectionLevel level = ErrorCorrectionLevel.H;
    @Builder.Default
    protected final int margin = 2;
    @Builder.Default
    protected final Charset charset = StandardCharsets.UTF_8;
    /**
     * QR code color configuration
     */
    protected MatrixToImageConfig config;
    protected SymbolShapeHint dataMatrixShape;
    protected int width;
    protected int height;
    protected String format;
    /**
     * Logo margins
     */
    protected int stroke;
    protected Color strokeColor;
    /**
     * Logo fillet diameter Width
     */
    protected int roundRectX;
    /**
     * Logo fillet diameter Height
     */
    protected int roundRectY;

    public abstract BufferedImage toBufferedImage(final String content);

    @SneakyThrows
    public BufferedImage toBufferedImage(final String content, final String logo) {
        return this.mergeLogoWithQRCode(content, logo).asBufferedImage();
    }

    @SneakyThrows
    public String toBase64(final String content) {
        try (final ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            this.writeToStream(content, bout);
            return this.getBase64Code(bout);
        }
    }

    @SneakyThrows
    public String toBase64(final String content, final String logo) {
        try (final ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            this.writeToStream(content, logo, bout);
            return this.getBase64Code(bout);
        }
    }

    @SneakyThrows
    public String toBase64(final BufferedImage image) {
        try (final ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            this.writeToStream(image, bout);
            return this.getBase64Code(bout);
        }
    }

    public abstract void writeToStream(final String content, final OutputStream stream);

    @SneakyThrows
    public void writeToStream(final String content, final String logo, final OutputStream out) {
        this.mergeLogoWithQRCode(content, logo).toOutputStream(out);
    }

    @SneakyThrows
    public void writeToStream(final BufferedImage image, final OutputStream out) {
        Thumbnails.of(image).scale(1).outputFormat("PNG").toOutputStream(out);
    }

    public abstract void writeToFile(final String content, final String filepath);

    @SneakyThrows
    public void writeToFile(final String content, final String logo, final String filepath) {
        this.mergeLogoWithQRCode(content, logo).toFile(new File(filepath));
    }

    /**
     * Merge the logo into the QR code
     *
     * @param content  QR code content
     * @param logoPath Logo image address
     */
    protected Thumbnails.Builder<BufferedImage> mergeLogoWithQRCode(String content, @NonNull String logoPath) throws Exception {
        final BufferedImage qrcode = this.toBufferedImage(content);
        final BufferedImage logo = this.getLogo(logoPath);
        // The width of the logo
        final int width = Math.min(logo.getWidth(), qrcode.getWidth() * 15 / 100);
        // The height of the logo
        final int height = Math.min(logo.getHeight(), qrcode.getHeight() * 15 / 100);

        final BufferedImage roundedLogo = ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = roundedLogo.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(JBColor.WHITE);
        g2d.fillRoundRect(0, 0, width, height, this.roundRectX, this.roundRectY);
        g2d.setComposite(AlphaComposite.SrcAtop);
        g2d.drawImage(logo, 0, 0, width, height, null);
        g2d.dispose();

        final int x = Math.round((qrcode.getWidth() - width) / 2F);
        final int y = Math.round((qrcode.getHeight() - height) / 2F);

        final BufferedImage combined = ImageUtil.createImage(qrcode.getWidth(), qrcode.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = (Graphics2D) combined.getGraphics();
        graphics.drawImage(qrcode, 0, 0, null);
        // Set the drawing method and transparency
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        graphics.drawImage(roundedLogo, x, y, width, height, null);
        // border
        graphics.setStroke(new BasicStroke(this.stroke));
        // Border color
        graphics.setColor(this.strokeColor);
//        graphics.drawRect(x, y, width, height);
        // Draw rounded corners for your logo
        graphics.drawRoundRect(x, y, width, height, this.roundRectX, this.roundRectY);
        graphics.dispose();
        combined.flush();
        return Thumbnails.of(combined).outputFormat(this.format).size(this.width, this.height);
    }

    private BufferedImage getLogo(final @NotNull String logoPath) throws Exception {
        final Thumbnails.Builder<?> builder;
        if (logoPath.matches("^https?://.+$")) {
            builder = Thumbnails.of(new URI(logoPath).toURL());
        } else if (logoPath.startsWith("data:image/")) {
            final byte[] decode = DECODER.decode(logoPath.split(",")[1]);
            try (final ByteArrayInputStream in = new ByteArrayInputStream(decode)) {
                builder = Thumbnails.of(in);
            }
        } else {
            builder = Thumbnails.of(logoPath);
        }
        return builder.scale(1)
                .outputQuality(1)
                .asBufferedImage();
    }

    protected String getBase64Code(final ByteArrayOutputStream bout) {
        final byte[] bytes = bout.toByteArray();
        final String baseString = ENCODER.encodeToString(bytes).trim();
        return BASE64_PICTURE_PREFIX + baseString.replaceAll("[\r\n]", "");
    }

    @SuppressWarnings("unused")
    public abstract static class AbstractQRCodeBuilder<C extends AbstractQRCode, B extends AbstractQRCodeBuilder<C, B>> {

        public C build() {
            final C build = this._build();
            build.hints.putIfAbsent(EncodeHintType.CHARACTER_SET, String.valueOf(build.charset));
            build.hints.putIfAbsent(EncodeHintType.ERROR_CORRECTION, build.level);
            build.hints.putIfAbsent(EncodeHintType.MARGIN, build.margin);
            build.hints.putIfAbsent(EncodeHintType.DATA_MATRIX_SHAPE, build.dataMatrixShape);
            build.config = new MatrixToImageConfig(build.onColor.getRGB(), build.offColor.getRGB());
            return build;
        }

    }

}
