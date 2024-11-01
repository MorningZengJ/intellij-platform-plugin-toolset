package com.github.morningzeng.toolset.utils;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.github.morningzeng.toolset.model.Pair;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Morning Zeng
 * @since 2024-10-31
 */
@Getter
@AllArgsConstructor
public enum AsymmetricCrypto {

    SM2("SM", "SM2") {
        static final Map<String, cn.hutool.crypto.asymmetric.SM2> CRYPTO_MAP = Maps.newHashMap();

        @Override
        String publicEnc(final String key, final String data) {
            return this.load(key, null).encryptHex(data, KeyType.PublicKey);
        }

        @Override
        String privateEnc(final String key, final String data) {
            return this.load(null, key).encryptHex(data, KeyType.PrivateKey);
        }

        @Override
        String publicDec(final String key, final String data) {
            return this.load(key, null).decryptStr(data, KeyType.PublicKey);
        }

        @Override
        String privateDec(final String key, final String data) {
            return this.load(null, key).decryptStr(data, KeyType.PrivateKey);
        }

        @Override
        String sign(final String key, final String data) {
            final byte[] sign = this.load(null, key).sign(data.getBytes(UTF_8));
            return Base64.getEncoder().encodeToString(sign);
        }

        @Override
        boolean verify(final String key, final String data, final String sign) {
            return this.load(key, null).verify(data.getBytes(UTF_8), Base64.getDecoder().decode(sign));
        }

        @Override
        public Pair<String, String> genKey() {
            final cn.hutool.crypto.asymmetric.SM2 sm2 = SmUtil.sm2();
            return Pair.of(sm2.getPublicKeyBase64(), sm2.getPrivateKeyBase64());
        }

        cn.hutool.crypto.asymmetric.SM2 load(final String publicKey, final String privateKey) {
            if (Objects.isNull(publicKey) && Objects.isNull(privateKey)) {
                throw new IllegalArgumentException();
            }
            String key;
            if (Objects.nonNull(publicKey) && Objects.nonNull(privateKey)) {
                key = String.join("-", publicKey, privateKey);
            } else {
                key = Optional.ofNullable(publicKey).orElse(privateKey);
            }
            return CRYPTO_MAP.computeIfAbsent(key, _key -> new SM2(privateKey, publicKey));
        }
    },

    ;

    private final String type;
    private final String algorithm;


    abstract String publicEnc(final String key, final String data);

    abstract String privateEnc(final String key, final String data);

    abstract String publicDec(final String key, final String data);

    abstract String privateDec(final String key, final String data);

    abstract String sign(final String key, final String data);

    abstract boolean verify(final String key, final String data, final String sign);

    public abstract Pair<String, String> genKey();

    public AsymmetricCryptoSupport publicKey(final String key) {
        return AsymmetricCryptoSupport.publicKey(this, key);
    }

    public AsymmetricCryptoSupport privateKey(final String key) {
        return AsymmetricCryptoSupport.privateKey(this, key);
    }

    public interface AsymmetricCryptoSupport {

        static AsymmetricCryptoSupport publicKey(final AsymmetricCrypto crypto, final String key) {
            return new AsymmetricCryptoSupport() {
                @Override
                public String enc(final String data) {
                    return crypto.publicEnc(key, data);
                }

                @Override
                public String dec(final String data) {
                    return crypto.publicDec(key, data);
                }

                @Override
                public boolean verify(final String data, final String sign) {
                    return crypto.verify(key, data, sign);
                }
            };
        }

        static AsymmetricCryptoSupport privateKey(final AsymmetricCrypto crypto, final String key) {
            return new AsymmetricCryptoSupport() {
                @Override
                public String enc(final String data) {
                    return crypto.privateEnc(key, data);
                }

                @Override
                public String dec(final String data) {
                    return crypto.privateDec(key, data);
                }

                @Override
                public String sign(final String data) {
                    return crypto.sign(key, data);
                }
            };
        }

        String enc(String data);

        String dec(String data);

        default String sign(String data) {
            throw new UnsupportedOperationException();
        }

        default boolean verify(String data, String sign) {
            throw new UnsupportedOperationException();
        }

    }

}
