package com.github.morningzeng.toolset.utils;

import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Morning Zeng
 * @since 2024-11-05
 */
public final class PictureUtils {
    final static String BASE64_PICTURE_PREFIX = "data:image/%s;base64,";
    final static Pattern PATTERN = Pattern.compile("data:image/([^;]+);");
    final static Base64.Encoder ENCODER = Base64.getEncoder();
    final static Base64.Decoder DECODER = Base64.getDecoder();

    /**
     * Creates a {@link BufferedImage} from a given path which can be a URL, a Base64 data URI, or a file path.
     *
     * @param content The path to the image. This can be a URL starting with "http" or "https",
     *                a Base64 data URI starting with "data:image/", or a direct file path.
     * @return A {@link BufferedImage} created from the given path.
     * @throws Exception If an error occurs during the creation of the image, such as an invalid path or decoding issue.
     */
    public static BufferedImage fromPath(final @NotNull String content) throws Exception {
        final Thumbnails.Builder<?> builder;
        if (content.matches("^https?://.+$")) {
            final URL url = new URI(content).toURL();
            try (final InputStream in = url.openStream()) {
                builder = Thumbnails.of(in);
            }
        } else if (content.startsWith("data:image/")) {
            final byte[] decode = DECODER.decode(content.split(",")[1]);
            try (final ByteArrayInputStream in = new ByteArrayInputStream(decode)) {
                builder = Thumbnails.of(in);
            }
        } else {
            builder = Thumbnails.of(content);
        }
        return builder.scale(1)
                .outputQuality(1)
                .asBufferedImage();
    }

    @SneakyThrows
    public static String getFormat(final String content) {
        if (content.matches("^https?://.+$")) {
            final URL url = new URI(content).toURL();
            try (final ImageInputStream iis = ImageIO.createImageInputStream(url)) {
                if (Objects.isNull(iis)) {
                    final String[] split = content.split("/");
                    final String[] suffix = split[split.length - 1].split("\\.");
                    if (suffix.length > 1) {
                        return suffix[1];
                    }
                    return "jpg";
                }
                final ImageReader imgReader = ImageIO.getImageReaders(iis).next();
                return imgReader.getFormatName();
            }
        }
        if (content.startsWith("data:image/")) {
            final Matcher matcher = PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group();
            }
            throw new IllegalArgumentException("Image format not found");
        }
        final File file = new File(content);
        try (final ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            final ImageReader imgReader = ImageIO.getImageReaders(iis).next();
            return imgReader.getFormatName();
        }
    }

    @SneakyThrows
    public static String base64Code(final BufferedImage image, final String format) {
        try (final ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                    .scale(1)
                    .outputFormat(format)
                    .toOutputStream(bout);
            return base64Code(bout, format);
        }
    }

    public static String base64Code(final ByteArrayOutputStream bout) {
        return base64Code(bout, "jpg");
    }

    public static String base64Code(final ByteArrayOutputStream bout, final String format) {
        final byte[] bytes = bout.toByteArray();
        final String baseString = ENCODER.encodeToString(bytes).trim();
        return BASE64_PICTURE_PREFIX.formatted(format) + baseString.replaceAll("[\r\n]", "");
    }

}
