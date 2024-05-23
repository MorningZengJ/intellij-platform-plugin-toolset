package com.github.morningzeng.toolset.enums;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;

/**
 * @author Morning Zeng
 * @since 2024-05-23
 */
public enum StringTypeEnum {

    TEXT {
        @Override
        public byte[] bytes(final String data) {
            return data.getBytes(StandardCharsets.UTF_8);
        }
    },
    HEX {
        @SneakyThrows
        @Override
        public byte[] bytes(final String data) {
            return Hex.decodeHex(data);
        }
    },

    ;

    public abstract byte[] bytes(final String data);

}
