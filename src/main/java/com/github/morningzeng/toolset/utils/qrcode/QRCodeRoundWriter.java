package com.github.morningzeng.toolset.utils.qrcode;

import com.google.zxing.qrcode.encoder.ByteMatrix;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author Morning Zeng
 * @since 2024-07-26
 */
public final class QRCodeRoundWriter extends AbstractQRCodeWriter {

    public QRCodeRoundWriter(final Color onColor, final Color offColor) {
        super(onColor, offColor);
    }

    @Override
    protected void setRegion(final ByteMatrix matrix, final int inputWidth, final int inputHeight, final int multiple, final int leftPadding, final int topPadding, final Graphics2D graphics2D) {
        final int FINDER_PATTERN_SIZE = 7;
        final float CIRCLE_SCALE_DOWN_FACTOR = 21f / 30f;
        final int circleSize = (int) (multiple * CIRCLE_SCALE_DOWN_FACTOR);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (matrix.get(inputX, inputY) == 1) {
                    if (!(inputX <= FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE ||
                            inputX >= inputWidth - FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE ||
                            inputX <= FINDER_PATTERN_SIZE && inputY >= inputHeight - FINDER_PATTERN_SIZE)) {
                        graphics2D.fillOval(outputX, outputY, circleSize, circleSize);
                    }
                }
            }
        }
        int circleDiameter = multiple * FINDER_PATTERN_SIZE;
        drawFinderPatternCircleStyle(graphics2D, leftPadding, topPadding, circleDiameter);
        drawFinderPatternCircleStyle(graphics2D, leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple, topPadding, circleDiameter);
        drawFinderPatternCircleStyle(graphics2D, leftPadding, topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple, circleDiameter);
    }

    void drawFinderPatternCircleStyle(Graphics2D graphics, int x, int y, int circleDiameter) {
        final int WHITE_CIRCLE_DIAMETER = circleDiameter * 5 / 7;
        final int WHITE_CIRCLE_OFFSET = circleDiameter / 7;
        final int MIDDLE_DOT_DIAMETER = circleDiameter * 3 / 7;
        final int MIDDLE_DOT_OFFSET = circleDiameter * 2 / 7;

        graphics.setColor(this.onColor);
        graphics.fillOval(x, y, circleDiameter, circleDiameter);
        graphics.setColor(this.offColor);
        graphics.fillOval(x + WHITE_CIRCLE_OFFSET, y + WHITE_CIRCLE_OFFSET, WHITE_CIRCLE_DIAMETER, WHITE_CIRCLE_DIAMETER);
        graphics.setColor(this.onColor);
        graphics.fillOval(x + MIDDLE_DOT_OFFSET, y + MIDDLE_DOT_OFFSET, MIDDLE_DOT_DIAMETER, MIDDLE_DOT_DIAMETER);
    }

}
