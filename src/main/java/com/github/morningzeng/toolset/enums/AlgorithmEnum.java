package com.github.morningzeng.toolset.enums;

import com.github.morningzeng.toolset.model.JWTProp;
import com.github.morningzeng.toolset.model.Pair;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.KeyBuilderSupplier;
import io.jsonwebtoken.security.KeyPairBuilderSupplier;
import io.jsonwebtoken.security.SecretKeyBuilder;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-05-29
 */
@Getter
@AllArgsConstructor
public enum AlgorithmEnum {
    /**
     * {@code HMAC using SHA-256} message authentication algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.2">RFC 7518, Section 3.2</a>.  This algorithm
     * requires a 256-bit (32 byte) key.
     */
    HS256("HmacSHA256", "HMAC using SHA-256 message authentication algorithm as defined by RFC 7518, Section 3.2.  This algorithm requires a 256-bit (32 byte) key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.HS256;
        }

        @Override
        public Pair<String, String> genKey() {
            //noinspection unchecked
            final KeyBuilderSupplier<SecretKey, SecretKeyBuilder> algorithm = (KeyBuilderSupplier<SecretKey, SecretKeyBuilder>) this.algorithm();
            final String key = Base64.getEncoder().encodeToString(algorithm.key().build().getEncoded());
            return Pair.of(key, key);
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            final SecretKey key = this.withSymmetric(prop);
            builder.decryptWith(key).verifyWith(key);
        }
    },

    /**
     * {@code HMAC using SHA-384} message authentication algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.2">RFC 7518, Section 3.2</a>.  This algorithm
     * requires a 384-bit (48 byte) key.
     */
    HS384("HmacSHA384", "HMAC using SHA-384 message authentication algorithm as defined by RFC 7518, Section 3.2.  This algorithm requires a 384-bit (48 byte) key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.HS384;
        }

        @Override
        public Pair<String, String> genKey() {
            //noinspection unchecked
            final KeyBuilderSupplier<SecretKey, SecretKeyBuilder> algorithm = (KeyBuilderSupplier<SecretKey, SecretKeyBuilder>) this.algorithm();
            final String key = Base64.getEncoder().encodeToString(algorithm.key().build().getEncoded());
            return Pair.of(key, key);
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            final SecretKey key = this.withSymmetric(prop);
            builder.decryptWith(key).verifyWith(key);
        }
    },

    /**
     * {@code HMAC using SHA-512} message authentication algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.2">RFC 7518, Section 3.2</a>.  This algorithm
     * requires a 512-bit (64 byte) key.
     */
    HS512("HmacSHA512", "HMAC using SHA-512 message authentication algorithm as defined by RFC 7518, Section 3.2.  This algorithm requires a 512-bit (64 byte) key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.HS512;
        }

        @Override
        public Pair<String, String> genKey() {
            //noinspection unchecked
            final KeyBuilderSupplier<SecretKey, SecretKeyBuilder> algorithm = (KeyBuilderSupplier<SecretKey, SecretKeyBuilder>) this.algorithm();
            final String key = Base64.getEncoder().encodeToString(algorithm.key().build().getEncoded());
            return Pair.of(key, key);
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            final SecretKey key = this.withSymmetric(prop);
            builder.decryptWith(key).verifyWith(key);
        }
    },

    /**
     * {@code RSASSA-PKCS1-v1_5 using SHA-256} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.3">RFC 7518, Section 3.3</a>.  This algorithm
     * requires a 2048-bit key.
     */
    RS256("SHA256withRSA", "RSASSA-PKCS1-v1_5 using SHA-256 signature algorithm as defined by RFC 7518, Section 3.3.  This algorithm requires a 2048-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.RS256;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }

    },

    /**
     * {@code RSASSA-PKCS1-v1_5 using SHA-384} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.3">RFC 7518, Section 3.3</a>.  This algorithm
     * requires a 2048-bit key, but the JJWT team recommends a 3072-bit key.
     */
    RS384("SHA384withRSA", "RSASSA-PKCS1-v1_5 using SHA-384 signature algorithm as defined by RFC 7518, Section 3.3.  This algorithm requires a 2048-bit key, but the JJWT team recommends a 3072-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.RS384;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    /**
     * {@code RSASSA-PKCS1-v1_5 using SHA-512} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.3">RFC 7518, Section 3.3</a>.  This algorithm
     * requires a 2048-bit key, but the JJWT team recommends a 4096-bit key.
     */
    RS512("SHA512withRSA", "RSASSA-PKCS1-v1_5 using SHA-512 signature algorithm as defined by RFC 7518, Section 3.3.  This algorithm requires a 2048-bit key, but the JJWT team recommends a 4096-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.RS512;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    /**
     * {@code RSASSA-PSS using SHA-256 and MGF1 with SHA-256} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.5">RFC 7518, Section 3.5</a><b><sup>1</sup></b>.
     * This algorithm requires a 2048-bit key.
     *
     * <p><b><sup>1</sup></b> Requires Java 11 or a compatible JCA Provider (like BouncyCastle) in the runtime
     * classpath. If on Java 10 or earlier, BouncyCastle will be used automatically if found in the runtime
     * classpath.</p>
     */
    PS256("SHA256withRSAandMGF1", "RSASSA-PSS using SHA-256 and MGF1 with SHA-256 signature algorithm as defined by RFC 7518, Section 3.5.  This algorithm requires a 2048-bit key. ") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.PS256;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    /**
     * {@code RSASSA-PSS using SHA-384 and MGF1 with SHA-384} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.5">RFC 7518, Section 3.5</a><b><sup>1</sup></b>.
     * This algorithm requires a 2048-bit key, but the JJWT team recommends a 3072-bit key.
     *
     * <p><b><sup>1</sup></b> Requires Java 11 or a compatible JCA Provider (like BouncyCastle) in the runtime
     * classpath. If on Java 10 or earlier, BouncyCastle will be used automatically if found in the runtime
     * classpath.</p>
     */
    PS384("SHA384withRSAandMGF1", "RSASSA-PSS using SHA-384 and MGF1 with SHA-384 signature algorithm as defined by RFC 7518, Section 3.5.  This algorithm requires a 2048-bit key, but the JJWT team recommends a 3072-bit key. ") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.PS384;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    /**
     * {@code RSASSA-PSS using SHA-512 and MGF1 with SHA-512} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.5">RFC 7518, Section 3.5</a><b><sup>1</sup></b>.
     * This algorithm requires a 2048-bit key, but the JJWT team recommends a 4096-bit key.
     *
     * <p><b><sup>1</sup></b> Requires Java 11 or a compatible JCA Provider (like BouncyCastle) in the runtime
     * classpath. If on Java 10 or earlier, BouncyCastle will be used automatically if found in the runtime
     * classpath.</p>
     */
    PS512("SHA512withRSAandMGF1", "RSASSA-PSS using SHA-512 and MGF1 with SHA-512 signature algorithm as defined by RFC 7518, Section 3.5.  This algorithm requires a 2048-bit key, but the JJWT team recommends a 4096-bit key. ") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.PS512;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    /**
     * {@code ECDSA using P-256 and SHA-256} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.4">RFC 7518, Section 3.4</a>.  This algorithm
     * requires a 256-bit key.
     */
    ES256("SHA256withECDSA", "ECDSA using P-256 and SHA-256 signature algorithm as defined by RFC 7518, Section 3.4.  This algorithm requires a 256-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.ES256;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    /**
     * {@code ECDSA using P-384 and SHA-384} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.4">RFC 7518, Section 3.4</a>.  This algorithm
     * requires a 384-bit key.
     */
    ES384("SHA384withECDSA", "ECDSA using P-384 and SHA-384 signature algorithm as defined by RFC 7518, Section 3.4.  This algorithm requires a 384-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.ES384;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    /**
     * {@code ECDSA using P-521 and SHA-512} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.4">RFC 7518, Section 3.4</a>.  This algorithm
     * requires a 521-bit key.
     */
    ES512("SHA512withECDSA", "ECDSA using P-521 and SHA-512 signature algorithm as defined by RFC 7518, Section 3.4.  This algorithm requires a 521-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.ES512;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    /**
     * {@code EdDSA} signature algorithm defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc8037#section-3.1">RFC 8037, Section 3.1</a> that requires
     * either {@code Ed25519} or {@code Ed448} Edwards Elliptic Curve<sup><b>1</b></sup> keys.
     *
     * <p><b>KeyPair Generation</b></p>
     *
     * <p>This instance's {@link KeyPairBuilderSupplier#keyPair() keyPair()} builder creates {@code Ed448} keys,
     * and is essentially an alias for
     * <code>{@link io.jsonwebtoken.security.Jwks.CRV Jwks.CRV}.{@link io.jsonwebtoken.security.Jwks.CRV#Ed448 Ed448}.{@link KeyPairBuilderSupplier#keyPair() keyPair()}</code>.</p>
     *
     * <p>If you would like to generate an {@code Ed25519} {@code KeyPair} for use with the {@code EdDSA} algorithm,
     * you may use the
     * <code>{@link io.jsonwebtoken.security.Jwks.CRV Jwks.CRV}.{@link io.jsonwebtoken.security.Jwks.CRV#Ed25519 Ed25519}.{@link KeyPairBuilderSupplier#keyPair() keyPair()}</code>
     * builder instead.</p>
     *
     * <p><b><sup>1</sup>This algorithm requires at least JDK 15 or a compatible JCA Provider (like BouncyCastle) in the runtime
     * classpath.</b></p>
     */
    EdDSA("Ed448", "EdDSA signature algorithm defined by RFC 8037, Section 3.1 that requires either Ed25519 or Ed448 Edwards Elliptic Curve keys") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.EdDSA;
        }

        @Override
        public void withKey(final JwtParserBuilder builder, final JWTProp prop) {
            builder.decryptWith(this.withPrivate(prop)).verifyWith(this.withPublic(prop));
        }
    },

    ;

    private final String jcaName;
    private final String desc;

    public abstract SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm();

    public abstract void withKey(final JwtParserBuilder builder, final JWTProp prop);

    Consumer<KeyPairGenerator> keyPairGeneratorConsumer() {
        return keyGen -> keyGen.initialize(2048);
    }

    @SneakyThrows
    public Pair<String, String> genKey() {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(this.jcaName);
        this.keyPairGeneratorConsumer().accept(keyGen);
        final KeyPair keyPair = keyGen.genKeyPair();
        return Pair.of(
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
        );
    }

    SecretKey withSymmetric(final JWTProp prop) {
        final byte[] bytes = prop.symmetricKeyType().bytes(prop.getSymmetricKey());
        return new SecretKeySpec(bytes, this.jcaName);
    }

    @SneakyThrows
    PublicKey withPublic(final JWTProp prop) {
        final byte[] bytes = Base64.getDecoder().decode(prop.getPublicKey());
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        final KeyFactory keyFactory = KeyFactory.getInstance(this.jcaName);
        return keyFactory.generatePublic(spec);
    }

    @SneakyThrows
    PrivateKey withPrivate(final JWTProp prop) {
        final byte[] bytes = Base64.getDecoder().decode(prop.getPrivateKey());
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        final KeyFactory keyFactory = KeyFactory.getInstance(this.jcaName);
        return keyFactory.generatePrivate(spec);
    }

}
