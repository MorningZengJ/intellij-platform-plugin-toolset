package com.github.morningzeng.toolset.utils;

import com.intellij.openapi.util.text.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Getter
@AllArgsConstructor
public enum SymmetricCrypto {

    /**
     * DES
     * <ul>
     *     <li>The key length of DES must be 8 bytes, which is shorter than the key length of AES (usually 16 bytes or 32 bytes).</li>
     * <li>DES is now considered insecure due to its short key length, making it vulnerable to brute force attacks. In practice, it is recommended to use a more secure algorithm such as AES or an upgraded version of DES, 3DES.
     *     </li>
     * </ul>
     */
    DES("DES", "DES") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * DES/CBC/PKCS5Padding
     */
    DES_CBC_PKCS5("DES", "DES/CBC/PKCS5Padding") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * 3DES
     */
    @SuppressWarnings("SpellCheckingInspection")
    DES_EDE("DESede", "DESede") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * AES AES/ECB/PKCS5Padding
     *
     * <ul>
     *     <li>The simplest AES encryption mode, which is also the default mode.</li>
     *     <li>If the same plaintext is encrypted with the same key, the encrypted ciphertext is exactly the same. This makes it easy to expose the plaintext pattern and is rarely used in practice.</li>
     * </ul>
     */
    AES_ECB_PKCS5("AES", "AES/ECB/PKCS5Padding") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * AES AES/ECB/NoPadding
     *
     * <ul>
     *     <li>The simplest AES encryption mode, which is also the default mode.</li>
     *     <li>If the same plaintext is encrypted with the same key, the encrypted ciphertext is exactly the same. This makes it easy to expose the plaintext pattern and is rarely used in practice.</li>
     * </ul>
     */
    AES_ECB_NO_PADDING("AES", "AES/ECB/NoPadding") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * AES AES/CBC/PKCS5Padding
     *
     * <ul>
     *     <li>Each plaintext block is XOR with the previous ciphertext block before it is encrypted, and then it is encrypted.</li>
     *     <li>The use of the initiation vector (IV) can compensate for the shortcomings of the ECB mode.</li>
     *     <li>It is often used in scenarios such as network transmission, where large amounts of data need to be encrypted.</li>
     * </ul>
     */
    AES_CBC_PKCS5("AES", "AES/CBC/PKCS5Padding") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * AES AES/CFB/NoPadding
     * <ul>
     *     <li>Each plaintext block is XOR with the previous ciphertext block before it is encrypted, and then it is encrypted.</li>
     *     <li>The use of the initiation vector (IV) can compensate for the shortcomings of the ECB mode.</li>
     *     <li>
     *         CFB(Cipher Feedback). "Feedback" here refers to the transmission of the state by sending the ciphertext of one block back to the input of the next block. Each operation depends on the result of the previous step. Commonly used for streaming mode communication.
     *     </li>
     * </ul>
     */
    AES_CFB_NO_PADDING("AES", "AES/CFB/NoPadding") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * AES AES/OFB/NoPadding
     * <ul>
     *     <li>Each plaintext block is XOR with the previous ciphertext block before it is encrypted, and then it is encrypted.</li>
     *     <li>The use of the initiation vector (IV) can compensate for the shortcomings of the ECB mode.</li>
     *     <li>
     *         OFB(Output Feedback). Similar to CFB, it is also a streaming mode, but it recovers errors in a different way.
     *         In OFB mode, the feedback input is the output of the previous step.
     *     </li>
     * </ul>
     */
    AES_OFB_NO_PADDING("AES", "AES/OFB/NoPadding") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * AES AES/CTR/NoPadding
     * <ul>
     *     <li>Each plaintext block is XOR with the previous ciphertext block before it is encrypted, and then it is encrypted.</li>
     *     <li>The use of the initiation vector (IV) can compensate for the shortcomings of the ECB mode.</li>
     *     <li>
     *         CTR(Counter). Encryption is achieved by using a counter as the input of the pattern.
     *         In this mode, AES is actually used as a type of streaming encryption. This means you can encrypt and decrypt data entries of any size.
     *     </li>
     * </ul>
     */
    AES_CTR_NO_PADDING("AES", "AES/CTR/NoPadding") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    /**
     * Blowfish
     * <ul>
     *     <li>Symmetric encryption algorithms</li>
     *     <li>is a symmetrical block cipher, its block size is 64 bits, and the key can be of any length (from 32 bits to 448 bits).</li>
     * </ul>
     */
    BLOWFISH("Blowfish", "Blowfish") {
        @SneakyThrows
        @Override
        public String enc(String data, String key, String iv) {
            final Cipher cipher = this.cipher(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        }

        @SneakyThrows
        @Override
        public String dec(String encrypt, String key, String iv) {
            byte[] decodedValue = Base64.getDecoder().decode(encrypt);
            Cipher cipher = this.cipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        }
    },

    ;

    private final String type;
    private final String algorithm;

    /**
     * AES
     *
     * @param encryptMode {@link int}
     * @return {@link Cipher}
     */
    @SneakyThrows
    Cipher cipher(int encryptMode, String key, String iv) {
        final Cipher cipher = Cipher.getInstance(this.getAlgorithm());
        if (!StringUtil.isEmpty(iv)) {
            cipher.init(
                    encryptMode,
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), this.getType()),
                    new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8))
            );
            return cipher;
        }
        cipher.init(
                encryptMode,
                new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), this.getType())
        );
        return cipher;
    }

    /**
     * encrypt
     *
     * @param data {@link String}
     * @param key  {@link String}
     * @param iv   {@link String}
     * @return {@link String}
     */
    abstract String enc(String data, String key, String iv);

    /**
     * decrypt
     *
     * @param data {@link String}
     * @param key  {@link String}
     * @param iv   {@link String}
     * @return {@link String}
     */
    abstract String dec(String data, String key, String iv);

    public SymmetricCryptoSupport crypto(final String key) {
        return SymmetricCryptoSupport.crypto(this, key, null);
    }

    public SymmetricCryptoSupport crypto(final String key, final String iv) {
        return SymmetricCryptoSupport.crypto(this, key, iv);
    }

    public interface SymmetricCryptoSupport {

        static SymmetricCryptoSupport crypto(SymmetricCrypto crypto, String key, String iv) {
            return new SymmetricCryptoSupport() {
                @Override
                public String enc(final String data) {
                    return crypto.enc(data, key, iv);
                }

                @Override
                public String dec(final String data) {
                    return crypto.dec(data, key, iv);
                }
            };
        }

        String enc(String data);

        String dec(String data);

    }
}