package com.github.morningzeng.toolset.utils.qrcode;

import com.github.morningzeng.toolset.utils.qrcode.AbstractQRCode.AbstractQRCodeBuilder;

/**
 * @author Morning Zeng
 * @since 2024-07-25
 */
@SuppressWarnings("unchecked")
public enum QRCodeFillTypeEnum {

    RECT {
        @Override
        public <B extends AbstractQRCode, C extends AbstractQRCodeBuilder<B, C>> C builder() {
            return (C) QRCodeRect.builder();
        }
    },
    ROUND {
        @Override
        public <B extends AbstractQRCode, C extends AbstractQRCodeBuilder<B, C>> C builder() {
            return (C) QRCodeRound.builder();
        }
    },
    ;

    public abstract <B extends AbstractQRCode, C extends AbstractQRCodeBuilder<B, C>> C builder();

}
