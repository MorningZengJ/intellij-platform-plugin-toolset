package com.github.morningzeng.toolbox.enums

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author Morning Zeng
 * @since 2025-05-22
 */
enum class CryptoHashEnum(
    val type: String,
    val algorithm: String,
) {
    MD5("MD5", "MD5") {
        override fun encrypt(data: String, vararg params: String): String {
            return MessageDigest.getInstance(algorithm)
                .digest(data.toByteArray(StandardCharsets.UTF_8))
                .let { String.format("%032x", BigInteger(1, it)) }
        }
    },
    MD5_16_FRONT("MD5", "MD5") {
        override fun encrypt(data: String, vararg params: String): String = MD5.encrypt(data).substring(0, 16)
    },
    MD5_16_MIDDLE("MD5", "MD5") {
        override fun encrypt(data: String, vararg params: String): String = MD5.encrypt(data).substring(8, 24)
    },
    MD5_16_LATER("MD5", "MD5") {
        override fun encrypt(data: String, vararg params: String): String = MD5.encrypt(data).substring(16)
    },
    SHA256("SHA", "SHA-256") {
        override fun encrypt(data: String, vararg params: String): String {
            return MessageDigest.getInstance(algorithm)
                .digest(data.toByteArray(StandardCharsets.UTF_8))
                .let { String.format("%064x", BigInteger(1, it)) }
        }
    },
    SHA384("SHA", "SHA-384") {
        override fun encrypt(data: String, vararg params: String): String {
            return MessageDigest.getInstance(algorithm)
                .digest(data.toByteArray(StandardCharsets.UTF_8))
                .let { String.format("%096x", BigInteger(1, it)) }
        }
    },
    SHA512("SHA", "SHA-512") {
        override fun encrypt(data: String, vararg params: String): String {
            return MessageDigest.getInstance(algorithm)
                .digest(data.toByteArray(StandardCharsets.UTF_8))
                .let { String.format("%0128x", BigInteger(1, it)) }
        }
    },
    SM3("SM", "SM3") {
        override fun encrypt(data: String, vararg params: String): String {
            return cn.hutool.crypto.digest.SM3.create().digestHex(data)
        }
    },
    HMAC_SHA256("Hmac", "HmacSHA256") {
        override fun encrypt(data: String, vararg params: String): String {
            return Mac.getInstance(algorithm)
                .apply { init(SecretKeySpec(params[0].toByteArray(StandardCharsets.UTF_8), algorithm)) }
                .doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .let { Base64.getEncoder().encodeToString(it) }
        }
    },
    HMAC_SHA512("Hmac", "HmacSHA512") {
        override fun encrypt(data: String, vararg params: String): String {
            return Mac.getInstance(algorithm)
                .apply { init(SecretKeySpec(params[0].toByteArray(StandardCharsets.UTF_8), algorithm)) }
                .doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .let { Base64.getEncoder().encodeToString(it) }
        }

    },
    ;

    abstract fun encrypt(data: String, vararg params: String): String

}