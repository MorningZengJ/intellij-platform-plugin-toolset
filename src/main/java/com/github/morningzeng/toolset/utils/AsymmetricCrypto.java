package com.github.morningzeng.toolset.utils;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.github.morningzeng.toolset.model.Pair;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Morning Zeng
 * @since 2024-10-31
 */
@Getter
@AllArgsConstructor
public enum AsymmetricCrypto {
    DSA("DSA", "DSA") {
        @Override
        String publicEnc(final String key, final String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        String privateEnc(final String key, final String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        String publicDec(final String key, final String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        String privateDec(final String key, final String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        String signAlgorithm() {
            return "SHA256withDSA";
        }
    },

    ECDSA("ECDSA", "EC") {
        @Override
        String publicEnc(final String key, final String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        String privateEnc(final String key, final String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        String publicDec(final String key, final String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        String privateDec(final String key, final String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        String signAlgorithm() {
            return "SHA256withECDSA";
        }

        @Override
        Consumer<KeyPairGenerator> keyPairGeneratorConsumer() {
            return keyGen -> {
                try {
                    keyGen.initialize(new ECGenParameterSpec("secp256r1"));
                } catch (InvalidAlgorithmParameterException e) {
                    throw new RuntimeException(e);
                }
            };
        }

    },

    @SuppressWarnings("SpellCheckingInspection")
    ECIES("ECIES", "EC") {
        @SneakyThrows
        @Override
        String publicEnc(final String key, final String data) {
            final Cipher cipher = Cipher.getInstance(this.getTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(key));
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(UTF_8)));
        }

        @SneakyThrows
        @Override
        String privateEnc(final String key, final String data) {
            throw new UnsupportedOperationException("Private key encryption is not supported in ECIES");
        }

        @SneakyThrows
        @Override
        String publicDec(final String key, final String data) {
            throw new UnsupportedOperationException("Public key decryption is not supported in ECIES");
        }

        @SneakyThrows
        @Override
        String privateDec(final String key, final String data) {
            final Cipher cipher = Cipher.getInstance(this.getTransformation());
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(key));
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)), UTF_8);
        }

        @Override
        String signAlgorithm() {
            return "SHA256withECDSA";
        }

        @Override
        Consumer<KeyPairGenerator> keyPairGeneratorConsumer() {
            return keyGen -> keyGen.initialize(256);
        }
    },

    ELGAMAL("ElGamal/ECB/PKCS1Padding", "EIGamal") {
        @SneakyThrows
        @Override
        String publicEnc(final String key, final String data) {
            final Cipher cipher = Cipher.getInstance(this.getTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(key));
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(UTF_8)));
        }

        @Override
        String privateEnc(final String key, final String data) {
            throw new UnsupportedOperationException("Private key encryption is not typically used in ElGamal");
        }

        @Override
        String publicDec(final String key, final String data) {
            throw new UnsupportedOperationException("Public key decryption is not typically used in ElGamal");
        }

        @SneakyThrows
        @Override
        String privateDec(final String key, final String data) {
            final Cipher cipher = Cipher.getInstance(this.getTransformation());
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(key));
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)), UTF_8);
        }

        @Override
        String signAlgorithm() {
            return "SHA256withRSA";
        }
    },

    RSA("RSA", "RSA") {
        @SneakyThrows
        @Override
        String publicEnc(final String key, final String data) {
            final Cipher cipher = Cipher.getInstance(this.getTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(key));
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(UTF_8)));
        }

        @SneakyThrows
        @Override
        String privateEnc(final String key, final String data) {
            final Cipher cipher = Cipher.getInstance(this.getTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey(key));
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(UTF_8)));
        }

        @SneakyThrows
        @Override
        String publicDec(final String key, final String data) {
            final Cipher cipher = Cipher.getInstance(this.getTransformation());
            cipher.init(Cipher.DECRYPT_MODE, getPublicKey(key));
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)), UTF_8);
        }

        @SneakyThrows
        @Override
        String privateDec(final String key, final String data) {
            final Cipher cipher = Cipher.getInstance(this.getTransformation());
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(key));
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)), UTF_8);
        }

        @Override
        String signAlgorithm() {
            return "SHA256withRSA";
        }

    },

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
        String signAlgorithm() {
            throw new UnsupportedOperationException();
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
        public @NotNull Pair<String, String> genKey() {
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

    private final String transformation;
    private final String algorithm;

    @SneakyThrows
    PublicKey getPublicKey(final String key) {
        final byte[] bytes = Base64.getDecoder().decode(key);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        final KeyFactory keyFac = KeyFactory.getInstance(this.getAlgorithm());
        return keyFac.generatePublic(spec);
    }

    @SneakyThrows
    PrivateKey getPrivateKey(final String key) {
        final byte[] bytes = Base64.getDecoder().decode(key);
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        final KeyFactory keyFac = KeyFactory.getInstance(this.getAlgorithm());
        return keyFac.generatePrivate(spec);
    }

    abstract String publicEnc(final String key, final String data);

    abstract String privateEnc(final String key, final String data);

    abstract String publicDec(final String key, final String data);

    abstract String privateDec(final String key, final String data);

    abstract String signAlgorithm();

    @SneakyThrows
    String sign(final String key, final String data) {
        final Signature signature = Signature.getInstance(this.signAlgorithm());
        signature.initSign(getPrivateKey(key));
        signature.update(data.getBytes(UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    @SneakyThrows
    boolean verify(final String key, final String data, final String sign) {
        final Signature signature = Signature.getInstance(this.signAlgorithm());
        signature.initVerify(getPublicKey(key));
        signature.update(data.getBytes(UTF_8));
        return signature.verify(Base64.getDecoder().decode(sign));
    }

    Consumer<KeyPairGenerator> keyPairGeneratorConsumer() {
        return keyGen -> keyGen.initialize(2048);
    }

    @SneakyThrows
    public @NotNull Pair<String, String> genKey() {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(this.getAlgorithm());
        this.keyPairGeneratorConsumer().accept(keyGen);
        final KeyPair keyPair = keyGen.genKeyPair();
        return Pair.of(
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
        );
    }

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
