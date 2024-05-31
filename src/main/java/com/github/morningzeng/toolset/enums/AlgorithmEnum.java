package com.github.morningzeng.toolset.enums;

import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.KeyPairBuilderSupplier;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Key;

/**
 * @author Morning Zeng
 * @since 2024-05-29
 */
@Getter
@AllArgsConstructor
public enum AlgorithmEnum {
    /**
     * The "none" signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.6">RFC 7518, Section 3.6</a>.  This algorithm
     * is used only when creating unsecured (not integrity protected) JWSs and is not usable in any other scenario.
     * Any attempt to call its methods will result in an exception being thrown.
     */
    NONE("The \"none\" signature algorithm as defined by RFC 7518, Section 3.6.  This algorithm is used only when creating unsecured (not integrity protected) JWSs and is not usable in any other scenario. Any attempt to call its methods will result in an exception being thrown.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.NONE;
        }
    },

    /**
     * {@code HMAC using SHA-256} message authentication algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.2">RFC 7518, Section 3.2</a>.  This algorithm
     * requires a 256-bit (32 byte) key.
     */
    HS256("HMAC using SHA-256 message authentication algorithm as defined by RFC 7518, Section 3.2.  This algorithm requires a 256-bit (32 byte) key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.HS256;
        }
    },

    /**
     * {@code HMAC using SHA-384} message authentication algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.2">RFC 7518, Section 3.2</a>.  This algorithm
     * requires a 384-bit (48 byte) key.
     */
    HS384("HMAC using SHA-384 message authentication algorithm as defined by RFC 7518, Section 3.2.  This algorithm requires a 384-bit (48 byte) key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.HS384;
        }
    },

    /**
     * {@code HMAC using SHA-512} message authentication algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.2">RFC 7518, Section 3.2</a>.  This algorithm
     * requires a 512-bit (64 byte) key.
     */
    HS512("HMAC using SHA-512 message authentication algorithm as defined by RFC 7518, Section 3.2.  This algorithm requires a 512-bit (64 byte) key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.HS512;
        }
    },

    /**
     * {@code RSASSA-PKCS1-v1_5 using SHA-256} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.3">RFC 7518, Section 3.3</a>.  This algorithm
     * requires a 2048-bit key.
     */
    RS256("RSASSA-PKCS1-v1_5 using SHA-256 signature algorithm as defined by RFC 7518, Section 3.3.  This algorithm requires a 2048-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.RS256;
        }
    },

    /**
     * {@code RSASSA-PKCS1-v1_5 using SHA-384} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.3">RFC 7518, Section 3.3</a>.  This algorithm
     * requires a 2048-bit key, but the JJWT team recommends a 3072-bit key.
     */
    RS384("RSASSA-PKCS1-v1_5 using SHA-384 signature algorithm as defined by RFC 7518, Section 3.3.  This algorithm requires a 2048-bit key, but the JJWT team recommends a 3072-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.RS384;
        }
    },

    /**
     * {@code RSASSA-PKCS1-v1_5 using SHA-512} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.3">RFC 7518, Section 3.3</a>.  This algorithm
     * requires a 2048-bit key, but the JJWT team recommends a 4096-bit key.
     */
    RS512("RSASSA-PKCS1-v1_5 using SHA-512 signature algorithm as defined by RFC 7518, Section 3.3.  This algorithm requires a 2048-bit key, but the JJWT team recommends a 4096-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.RS512;
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
    PS256("RSASSA-PSS using SHA-256 and MGF1 with SHA-256 signature algorithm as defined by RFC 7518, Section 3.5.  This algorithm requires a 2048-bit key. ") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.PS256;
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
    PS384("RSASSA-PSS using SHA-384 and MGF1 with SHA-384 signature algorithm as defined by RFC 7518, Section 3.5.  This algorithm requires a 2048-bit key, but the JJWT team recommends a 3072-bit key. ") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.PS384;
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
    PS512("RSASSA-PSS using SHA-512 and MGF1 with SHA-512 signature algorithm as defined by RFC 7518, Section 3.5.  This algorithm requires a 2048-bit key, but the JJWT team recommends a 4096-bit key. ") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.PS512;
        }
    },

    /**
     * {@code ECDSA using P-256 and SHA-256} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.4">RFC 7518, Section 3.4</a>.  This algorithm
     * requires a 256-bit key.
     */
    ES256("ECDSA using P-256 and SHA-256 signature algorithm as defined by RFC 7518, Section 3.4.  This algorithm requires a 256-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.ES256;
        }
    },

    /**
     * {@code ECDSA using P-384 and SHA-384} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.4">RFC 7518, Section 3.4</a>.  This algorithm
     * requires a 384-bit key.
     */
    ES384("ECDSA using P-384 and SHA-384 signature algorithm as defined by RFC 7518, Section 3.4.  This algorithm requires a 384-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.ES384;
        }
    },

    /**
     * {@code ECDSA using P-521 and SHA-512} signature algorithm as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3.4">RFC 7518, Section 3.4</a>.  This algorithm
     * requires a 521-bit key.
     */
    ES512("ECDSA using P-521 and SHA-512 signature algorithm as defined by RFC 7518, Section 3.4.  This algorithm requires a 521-bit key.") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.ES512;
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
    EdDSA("EdDSA signature algorithm defined by RFC 8037, Section 3.1 that requires either Ed25519 or Ed448 Edwards Elliptic Curve keys") {
        @Override
        public SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm() {
            return SIG.EdDSA;
        }
    },

    ;


    private final String desc;

    public abstract SecureDigestAlgorithm<? extends Key, ? extends Key> algorithm();

}
