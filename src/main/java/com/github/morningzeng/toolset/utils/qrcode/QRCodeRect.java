package com.github.morningzeng.toolset.utils.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;

/**
 * @author Morning Zeng
 * @since 2024-07-25
 */
@SuperBuilder(buildMethodName = "_build")
public final class QRCodeRect extends AbstractQRCode {

    @Override
    @SneakyThrows
    public BufferedImage toBufferedImage(final String content) {
        final BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, this.width, this.height, this.hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix, this.config);
    }

    @Override
    @SneakyThrows
    public void writeToStream(final String content, final OutputStream out) {
        final BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, this.width, this.height, this.hints);
        MatrixToImageWriter.writeToStream(bitMatrix, this.format, out, this.config);
    }

    @Override
    @SneakyThrows
    public void writeToFile(final String content, final String filepath) {
        final BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, this.width, this.height, this.hints);
        MatrixToImageWriter.writeToPath(bitMatrix, this.format, new File(filepath).toPath(), this.config);
    }

}
