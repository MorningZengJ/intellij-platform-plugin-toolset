package com.github.morningzeng.toolbox.enums

import cn.hutool.crypto.SmUtil
import cn.hutool.crypto.asymmetric.KeyType
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

/**
 * @author Morning Zeng
 * @since 2025-05-23
 */
enum class CryptoAsymmetricEnum(
    val transformation: String,
    val algorithm: String,
    val publicEncPrivateDec: Boolean,
    val privateEncPublicDec: Boolean,
) {
    DSA("DSA", "DSA", false, false) {
        override fun publicEnc(key: String, data: String): String = throw UnsupportedOperationException()

        override fun privateEnc(key: String, data: String): String = throw UnsupportedOperationException()

        override fun publicDec(key: String, data: String): String = throw UnsupportedOperationException()

        override fun privateDec(key: String, data: String): String = throw UnsupportedOperationException()

        override fun signAlgorithm(): String = "SHA256withDSA"
    },
    ECDSA("ECDSA", "EC", false, false) {
        override fun publicEnc(key: String, data: String): String = throw UnsupportedOperationException()

        override fun privateEnc(key: String, data: String): String = throw UnsupportedOperationException()

        override fun publicDec(key: String, data: String): String = throw UnsupportedOperationException()

        override fun privateDec(key: String, data: String): String = throw UnsupportedOperationException()

        override fun signAlgorithm(): String = "SHA256withECDSA"

        override fun keyPairGeneratorConsumer(): (KeyPairGenerator) -> Unit = {
            it.initialize(ECGenParameterSpec("secp256r1"))
        }
    },
    ECIES("ECIES", "EC", true, false) {
        override fun publicEnc(key: String, data: String): String {
            Cipher.getInstance(transformation).apply {
                init(Cipher.ENCRYPT_MODE, getPublicKey(key))
                return Base64.getEncoder().encodeToString(doFinal(data.toByteArray(Charsets.UTF_8)))
            }
        }

        override fun privateEnc(key: String, data: String): String = throw UnsupportedOperationException()

        override fun publicDec(key: String, data: String): String = throw UnsupportedOperationException()

        override fun privateDec(key: String, data: String): String {
            Cipher.getInstance(transformation).apply {
                init(Cipher.DECRYPT_MODE, getPrivateKey(key))
                return String(doFinal(Base64.getDecoder().decode(data)), Charsets.UTF_8)
            }
        }

        override fun signAlgorithm(): String = "SHA256withECDSA"

        override fun keyPairGeneratorConsumer(): (KeyPairGenerator) -> Unit = { it.initialize(256) }
    },
    ELGAMAL("ElGamal/ECB/PKCS1Padding", "EIGamal", true, false) {
        override fun publicEnc(key: String, data: String): String {
            Cipher.getInstance(transformation).apply {
                init(Cipher.ENCRYPT_MODE, getPublicKey(key))
                return Base64.getEncoder().encodeToString(doFinal(data.toByteArray(Charsets.UTF_8)))
            }
        }

        override fun privateEnc(key: String, data: String): String = throw UnsupportedOperationException()

        override fun publicDec(key: String, data: String): String = throw UnsupportedOperationException()

        override fun privateDec(key: String, data: String): String {
            Cipher.getInstance(transformation).apply {
                init(Cipher.DECRYPT_MODE, getPrivateKey(key))
                return String(doFinal(Base64.getDecoder().decode(data)), Charsets.UTF_8)
            }
        }

        override fun signAlgorithm(): String = "SHA256withRSA"
    },
    RSA("RSA", "RSA", true, true) {
        override fun publicEnc(key: String, data: String): String {
            Cipher.getInstance(transformation).apply {
                init(Cipher.ENCRYPT_MODE, getPublicKey(key))
                return Base64.getEncoder().encodeToString(doFinal(data.toByteArray(Charsets.UTF_8)))
            }
        }

        override fun privateEnc(key: String, data: String): String {
            Cipher.getInstance(transformation).apply {
                init(Cipher.ENCRYPT_MODE, getPrivateKey(key))
                return Base64.getEncoder().encodeToString(doFinal(data.toByteArray(Charsets.UTF_8)))
            }
        }

        override fun publicDec(key: String, data: String): String {
            Cipher.getInstance(transformation).apply {
                init(Cipher.DECRYPT_MODE, getPublicKey(key))
                return String(doFinal(Base64.getDecoder().decode(data)), Charsets.UTF_8)
            }
        }

        override fun privateDec(key: String, data: String): String {
            Cipher.getInstance(transformation).apply {
                init(Cipher.DECRYPT_MODE, getPrivateKey(key))
                return String(doFinal(Base64.getDecoder().decode(data)), Charsets.UTF_8)
            }
        }

        override fun signAlgorithm(): String = "SHA256withRSA"
    },
    SM2("SM", "SM2", true, true) {
        override fun publicEnc(key: String, data: String): String {
            load(key, null).apply {
                return encryptHex(data, KeyType.PublicKey)
            }
        }

        override fun privateEnc(key: String, data: String): String {
            load(null, key).apply {
                return encryptHex(data, KeyType.PrivateKey)
            }
        }

        override fun publicDec(key: String, data: String): String {
            load(key, null).apply {
                return decryptStr(data, KeyType.PublicKey)
            }
        }

        override fun privateDec(key: String, data: String): String {
            load(null, key).apply {
                return decryptStr(data, KeyType.PrivateKey)
            }
        }

        override fun signAlgorithm(): String = throw UnsupportedOperationException()

        override fun sign(key: String, data: String): String {
            load(null, key).apply {
                val sign = sign(data.toByteArray(Charsets.UTF_8))
                return Base64.getEncoder().encodeToString(sign)
            }
        }

        override fun verify(key: String, data: String, sign: String): Boolean {
            load(key, null).apply {
                return verify(data.toByteArray(Charsets.UTF_8), Base64.getDecoder().decode(sign))
            }
        }

        override fun genKey(): Pair<String, String> {
            SmUtil.sm2().apply {
                return Pair(publicKeyBase64, privateKeyBase64)
            }
        }

        fun load(publicKey: String?, privateKey: String?): cn.hutool.crypto.asymmetric.SM2 {
            return cn.hutool.crypto.asymmetric.SM2(privateKey, publicKey)
        }
    },
    ;

    fun getPublicKey(key: String): PublicKey {
        val spec = Base64.getDecoder().decode(key)
            .let { X509EncodedKeySpec(it) }
        return KeyFactory.getInstance(algorithm).generatePublic(spec)
    }

    fun getPrivateKey(key: String): PrivateKey {
        val spec = Base64.getDecoder().decode(key)
            .let { PKCS8EncodedKeySpec(it) }
        return KeyFactory.getInstance(algorithm).generatePrivate(spec)
    }


    protected abstract fun publicEnc(key: String, data: String): String

    protected abstract fun privateEnc(key: String, data: String): String

    protected abstract fun publicDec(key: String, data: String): String

    protected abstract fun privateDec(key: String, data: String): String

    protected abstract fun signAlgorithm(): String

    protected open fun sign(key: String, data: String): String {
        Signature.getInstance(signAlgorithm()).apply {
            initSign(getPrivateKey(key))
            update(data.toByteArray(Charsets.UTF_8))
            return Base64.getEncoder().encodeToString(sign())
        }
    }

    protected open fun verify(key: String, data: String, sign: String): Boolean {
        Signature.getInstance(signAlgorithm()).apply {
            initVerify(getPublicKey(key))
            update(data.toByteArray(Charsets.UTF_8))
            return verify(Base64.getDecoder().decode(sign))
        }
    }

    protected open fun keyPairGeneratorConsumer(): (KeyPairGenerator) -> Unit = { it.initialize(2048) }

    open fun genKey(): Pair<String, String> {
        KeyPairGenerator.getInstance(algorithm).apply {
            keyPairGeneratorConsumer().invoke(this)
            val keyPair = generateKeyPair()
            return Pair(
                Base64.getEncoder().encodeToString(keyPair.public.encoded),
                Base64.getEncoder().encodeToString(keyPair.private.encoded)
            )
        }
    }

    fun publicKey(key: String): CryptoAsymmetricSupport = CryptoAsymmetricSupport.publicKey(this, key)

    fun privateKey(key: String): CryptoAsymmetricSupport = CryptoAsymmetricSupport.privateKey(this, key)

    interface CryptoAsymmetricSupport {
        companion object {
            fun publicKey(crypto: CryptoAsymmetricEnum, key: String): CryptoAsymmetricSupport {
                return object : CryptoAsymmetricSupport {
                    override fun encrypt(data: String): String = crypto.publicEnc(key, data)

                    override fun decrypt(data: String): String = crypto.publicDec(key, data)

                    override fun verify(data: String, sign: String): Boolean = crypto.verify(key, data, sign)
                }
            }

            fun privateKey(crypto: CryptoAsymmetricEnum, key: String): CryptoAsymmetricSupport {
                return object : CryptoAsymmetricSupport {
                    override fun encrypt(data: String): String = crypto.privateEnc(key, data)

                    override fun decrypt(data: String): String = crypto.privateDec(key, data)

                    override fun sign(data: String): String = crypto.sign(key, data)
                }
            }
        }

        fun encrypt(data: String): String

        fun decrypt(data: String): String

        fun sign(data: String): String {
            throw UnsupportedOperationException()
        }

        fun verify(data: String, sign: String): Boolean {
            throw UnsupportedOperationException()
        }
    }

    enum class Type(
        val alias: String
    ) {
        PUBLIC_KEY("PublicKey"),
        PRIVATE_KEY("PrivateKey"),
        ;
    }

}