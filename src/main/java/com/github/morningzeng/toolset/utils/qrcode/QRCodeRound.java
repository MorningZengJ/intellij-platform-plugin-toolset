package com.github.morningzeng.toolset.utils.qrcode;

import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

/**
 * @author Morning Zeng
 * @since 2024-07-25
 */
@SuperBuilder(buildMethodName = "_build")
public final class QRCodeRound extends AbstractQRCode {

    @Override
    @SneakyThrows
    public BufferedImage toBufferedImage(final String content) {
        return new QRCodeRoundWriter(this.onColor, this.offColor).createQRCode(content, this.width, this.height, this.hints);
    }

    @Override
    @SneakyThrows
    public void writeToStream(final String content, final OutputStream out) {
        Thumbnails.of(this.toBufferedImage(content))
                .toOutputStream(out);
    }

    @Override
    @SneakyThrows
    public void writeToFile(final String content, final String filepath) {
        Thumbnails.of(this.toBufferedImage(content))
                .toFile(filepath);
    }

}
