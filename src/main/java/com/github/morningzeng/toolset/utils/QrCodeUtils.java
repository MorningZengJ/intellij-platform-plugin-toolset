package com.github.morningzeng.toolset.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * QR code
 *
 * @author Morning Zeng
 * @since 2024-07-22 17:03:40
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class QrCodeUtils {
    private final static String BASE64_PICTURE_PREFIX = "data:image/jpg;base64,";
    private final static Base64.Encoder ENCODER = Base64.getEncoder();
    private final static Base64.Decoder DECODER = Base64.getDecoder();
    private final static QrCodeUtils INSTANCE = QrCodeUtils.builder().build();
    private int width;
    private int height;
    private String format;
    /**
     * Logo margins
     */
    private int stroke;
    private Color strokeColor;
    /**
     * Logo fillet diameter Width
     */
    private int roundRectX;
    /**
     * Logo fillet diameter Height
     */
    private int roundRectY;
    /**
     * parameters
     */
    private Map<EncodeHintType, Object> hints;
    /**
     * QR code color configuration
     */
    private MatrixToImageConfig config;

    /**
     * Get the default configuration
     * <pre>
     *     width: 500,
     *     height: 500,
     *     format: PNG,
     *     stroke: 5,
     *     roundRectX: 15,
     *     roundRectY: 15,
     *     onColor: 0xFF000000,
     *     offColor: 0xFFFFFFFF,
     *     Fault tolerance level{@link ErrorCorrectionLevel}: H
     * </pre>
     *
     * @return {@link QrCodeUtils}Default instance
     */
    public static QrCodeUtils getInstance() {
        return INSTANCE;
    }

    public static QrCodeUtilBuilder builder() {
        return new QrCodeUtilBuilder();
    }

    /**
     * @param content QR code content
     * @return BufferedImage
     */
    public BufferedImage toBufferedImage(String content) throws WriterException {
        final BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, this.width, this.height, this.hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix, this.config);
    }

    /**
     * Generate an image with a logo
     *
     * @param content  QR code content
     * @param logoPath Logo image address
     */
    public BufferedImage toBufferedImageWithLogo(String content, String logoPath) throws Exception {
        return this.getBufferedImageBuilder(content, logoPath).asBufferedImage();
    }

    /**
     * The QR code is output to the stream
     *
     * @param content      QR code content
     * @param outputStream Output stream
     */
    public void writeToStream(String content, OutputStream outputStream) throws WriterException, IOException {
        final BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, this.width, this.height, this.hints);
        MatrixToImageWriter.writeToStream(bitMatrix, this.format, outputStream, this.config);
    }

    /**
     * Generate an image with a logo
     *
     * @param content      QR code content
     * @param logoPath     Logo image address
     * @param outputStream Output stream
     */
    public void writeToStreamWithLogo(String content, @NonNull String logoPath, OutputStream outputStream) throws Exception {
        this.getBufferedImageBuilder(content, logoPath).toOutputStream(outputStream);
    }

    /**
     * Generate a QR code image
     *
     * @param content QR code content
     * @param path    File path
     */
    public void toFile(String content, String path) throws WriterException, IOException {
        final BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, this.width, this.height, this.hints);
        MatrixToImageWriter.writeToPath(bitMatrix, this.format, new File(path).toPath(), this.config);
    }

    /**
     * Generate an image with a logo
     *
     * @param content  QR code content
     * @param logoPath Logo image address
     * @param outPath  File path
     */
    public void toFileWithLogo(String content, @NonNull String logoPath, @NonNull String outPath) throws Exception {
        this.getBufferedImageBuilder(content, logoPath).toFile(new File(outPath));
    }

    /**
     * Create a base64-encoded QR code to a string
     *
     * @param content QR code content
     * @return Base64 encoded QR code
     */
    public String toBase64Code(String content) throws IOException, WriterException {
        try (final ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            this.writeToStream(content, bout);
            return this.getBase64Code(bout);
        }
    }

    /**
     * Create a base64 coded QR code with a logo image to a string
     *
     * @param content QR code content
     * @return Base64 encoded QR code
     */
    public String toBase64Code(String content, @NonNull String logoPath) throws Exception {
        try (final ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            this.writeToStreamWithLogo(content, logoPath, bout);
            return this.getBase64Code(bout);
        }
    }

    private String getBase64Code(ByteArrayOutputStream bout) {
        final byte[] bytes = bout.toByteArray();
        final String baseString = ENCODER.encodeToString(bytes).trim();
        return BASE64_PICTURE_PREFIX + baseString.replaceAll("[\r\n]", "");
    }

    /**
     * Merge the logo into the QR code
     *
     * @param content  QR code content
     * @param logoPath Logo image address
     */
    private Thumbnails.Builder<BufferedImage> getBufferedImageBuilder(String content, @NonNull String logoPath) throws Exception {
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
        final Builder<?> builder;
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

    @Setter
    @ToString
    @Accessors(fluent = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QrCodeUtilBuilder {
        private int width = 500;
        private int height = 500;
        private String format = "PNG";
        private int onColor = JBColor.BLACK.getRGB();
        private int offColor = JBColor.WHITE.getRGB();
        private Color strokeColor = JBColor.WHITE;
        private ErrorCorrectionLevel level = ErrorCorrectionLevel.H;
        private int margin = 2;
        private int stroke = 5;
        private int roundRectX = 55;
        private int roundRectY = 55;
        private Charset charset;
        private SymbolShapeHint dataMatrixShape;

        public QrCodeUtils build() {
            final MatrixToImageConfig config = new MatrixToImageConfig(this.onColor, this.offColor);
            final Map<EncodeHintType, Object> hints = new HashMap<>(3) {{
                // encoding
                this.put(EncodeHintType.CHARACTER_SET, String.valueOf(charset));
                // Fault tolerance level L<M<Q<H
                this.put(EncodeHintType.ERROR_CORRECTION, QrCodeUtilBuilder.this.level);
                // margins
                this.put(EncodeHintType.MARGIN, QrCodeUtilBuilder.this.margin);
                this.put(EncodeHintType.DATA_MATRIX_SHAPE, dataMatrixShape);
            }};
            return new QrCodeUtils(this.width, this.height, this.format, this.stroke, this.strokeColor, this.roundRectX, this.roundRectY, hints, config);
        }
    }
}
