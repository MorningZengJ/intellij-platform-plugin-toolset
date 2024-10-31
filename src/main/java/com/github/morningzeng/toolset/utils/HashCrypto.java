package com.github.morningzeng.toolset.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum HashCrypto {

    /**
     * MD5
     */
    MD5("MD5", "MD5") {
        @SneakyThrows
        @Override
        public String enc(String data, String... params) {
            MessageDigest messageDigest = MessageDigest.getInstance(this.getAlgorithm());
            byte[] digest = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest));
        }
    },

    /**
     * This is an enum constant representing a specific encryption algorithm called MD5_16_FRONT.
     * <p>
     * The MD5_16_FRONT algorithm is a variation of the MD5 algorithm that produces a 16-character
     * cryptographic hash value. It takes an input data string and returns the first 16 characters of
     * the MD5 hash value of the input data.
     * <p>
     * Example usage:
     * String encryptedData = MD5_16_FRONT.enc("Hello World");
     * </p>
     */
    MD5_16_FRONT("MD5", "MD5") {
        @SneakyThrows
        @Override
        public String enc(String data, String... params) {
            return MD5.enc(data).substring(0, 16);
        }
    },

    /**
     * Enum representing a specific implementation of the MD5 algorithm.
     * This implementation returns the middle 16 characters of the MD5 hash of the input data.
     */
    MD5_16_MIDDLE("MD5", "MD5") {
        @SneakyThrows
        @Override
        public String enc(String data, String... params) {
            return MD5.enc(data).substring(8, 24);
        }
    },

    /**
     * MD5_16_LATER is an enum constant that represents a specific implementation of the MD5 algorithm.
     * It is used for encoding data and extracting a substring of the MD5 hash, starting from the 16th character.
     */
    MD5_16_LATER("MD5", "MD5") {
        @SneakyThrows
        @Override
        public String enc(String data, String... params) {
            return MD5.enc(data).substring(16);
        }
    },

    /**
     * The SHA_256 variable represents the SHA-256 hashing algorithm.
     * It is an implementation of the MessageDigestHash enum that uses the "SHA-256" algorithm.
     * It provides a method to hash data using this algorithm.
     */
    SHA_256("SHA", "SHA-256") {
        @SneakyThrows
        @Override
        public String enc(String data, String... params) {
            MessageDigest messageDigest = MessageDigest.getInstance(this.getAlgorithm());
            byte[] digest = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, digest));
        }
    },

    /**
     * SHA_384 represents the SHA-384 hashing algorithm.
     */
    SHA_384("SHA", "SHA-384") {
        @SneakyThrows
        @Override
        public String enc(String data, String... params) {
            MessageDigest messageDigest = MessageDigest.getInstance(this.getAlgorithm());
            byte[] digest = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            return String.format("%096x", new BigInteger(1, digest));
        }
    },

    /**
     * Represents the SHA3-512 algorithm for hashing data.
     */
    SHA3_512("SHA", "SHA3-512") {
        @SneakyThrows
        @Override
        public String enc(String data, String... params) {
            MessageDigest messageDigest = MessageDigest.getInstance(this.getAlgorithm());
            byte[] digest = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            return String.format("%0128x", new BigInteger(1, digest));
        }
    },

    SM3("SM", "SM3") {
        @Override
        public String enc(final String data, final String... params) {
            final cn.hutool.crypto.digest.SM3 sm3 = cn.hutool.crypto.digest.SM3.create();
            return sm3.digestHex(data);
        }
    },

    /**
     * This enum represents a HMAC algorithm with its specific parameters.
     * <pre>
     *  {@code
     *      CryptoUtils.HMAC.init("your-key", "").encrypt("your-message")
     *  }
     * </pre>
     */
    HMAC("Hmac", "HmacSHA512") {
        @SneakyThrows
        @Override
        public String enc(String data, String... params) {
            if (Objects.isNull(params) || StringUtils.isBlank(params[0])) {
                throw new IllegalArgumentException(this.getType() + "密钥不能为空");
            }
            final Mac mac = Mac.getInstance(this.getAlgorithm());
            final SecretKeySpec keySpec = new SecretKeySpec(
                    params[0].getBytes(StandardCharsets.UTF_8),
                    this.getAlgorithm()
            );
            mac.init(keySpec);
            final byte[] encodedBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedBytes);
        }
    },

    ;

    private final String type;
    private final String algorithm;

    /**
     * Method to perform encryption on given data using specified parameters.
     *
     * @param data   the data to be encrypted
     * @param params the additional parameters used for encryption
     * @return the encrypted data
     */
    public abstract String enc(String data, String... params);

}