package com.github.morningzeng.toolbox.enums

import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @author Morning Zeng
 * @since 2025-05-20
 */
enum class CryptoSymmetricEnum(
    val type: String,
    val algorithm: String,
) {
    DES("DES", "DES") {
        override fun secretKey(
            key: String,
            keyType: DataToBinaryTypeEnum
        ): SecretKey = SecretKeyFactory.getInstance(type).generateSecret(DESKeySpec(keyType.bytes(key)))

        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            return String(cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType).doFinal(data))
        }
    },
    DES_CBC_PKCS5("DES", "DES/CBC/PKCS5Padding") {
        override fun secretKey(
            key: String,
            keyType: DataToBinaryTypeEnum
        ): SecretKey = SecretKeyFactory.getInstance(type).generateSecret(DESKeySpec(keyType.bytes(key)))

        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },
    DES_EDE("DES", "DESede") {
        override fun secretKey(
            key: String,
            keyType: DataToBinaryTypeEnum
        ): SecretKey = SecretKeyFactory.getInstance(type).generateSecret(DESKeySpec(keyType.bytes(key)))

        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },
    AES_ECB_PKCS5("AES", "AES/ECB/PKCS5Padding") {
        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },
    AES_ECB_NO_PADDING("AES", "AES/ECB/NoPadding") {
        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },
    AES_CBC_PKCS5("AES", "AES/CBC/PKCS5Padding") {
        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },
    AES_CFB_NO_PADDING("AES", "AES/CFB/NoPadding") {
        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },
    AES_OFB_NO_PADDING("AES", "AES/OFB/NoPadding") {
        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },
    AES_CTR_NO_PADDING("AES", "AES/CTR/NoPadding") {
        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },
    BLOWFISH("Blowfish", "Blowfish") {
        override fun encrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): ByteArray {
            return this.cipher(Cipher.ENCRYPT_MODE, key, keyType, iv, ivType).doFinal(data)
        }

        override fun decrypt(
            data: ByteArray,
            key: String,
            keyType: DataToBinaryTypeEnum,
            iv: String?,
            ivType: DataToBinaryTypeEnum?
        ): String {
            val cipher = this.cipher(Cipher.DECRYPT_MODE, key, keyType, iv, ivType)
            val decryptedValue = cipher.doFinal(data)
            return String(decryptedValue, StandardCharsets.UTF_8)
        }
    },

    ;

    fun cipher(
        encryptMode: Int,
        key: String,
        keyType: DataToBinaryTypeEnum,
        iv: String?,
        ivType: DataToBinaryTypeEnum?
    ): Cipher {
        return Cipher.getInstance(algorithm).apply {
            val secretKey = secretKey(key, keyType)
            if (iv?.isEmpty() == true) {
                init(encryptMode, secretKey)
            } else {
                init(
                    encryptMode,
                    secretKey,
                    IvParameterSpec(ivType?.bytes(iv!!))
                )
            }
        }
    }

    fun encrypt(
        data: ByteArray, key: String, iv: String?,
    ): ByteArray = encrypt(data, key, DataToBinaryTypeEnum.TEXT, iv, DataToBinaryTypeEnum.TEXT)

    open fun secretKey(
        key: String,
        keyType: DataToBinaryTypeEnum,
    ): SecretKey = SecretKeySpec(keyType.bytes(key), type)

    fun encrypt(
        data: String,
        key: String,
        keyType: DataToBinaryTypeEnum,
        iv: String?,
        ivType: DataToBinaryTypeEnum?
    ): ByteArray = encrypt(DataToBinaryTypeEnum.TEXT.bytes(data), key, keyType, iv, ivType)

    abstract fun encrypt(
        data: ByteArray,
        key: String,
        keyType: DataToBinaryTypeEnum,
        iv: String?,
        ivType: DataToBinaryTypeEnum?
    ): ByteArray

    fun decrypt(
        data: ByteArray, key: String, iv: String?
    ): String = decrypt(data, key, DataToBinaryTypeEnum.TEXT, iv, DataToBinaryTypeEnum.TEXT)

    fun decrypt(
        data: String,
        key: String,
        keyType: DataToBinaryTypeEnum,
        iv: String?,
        ivType: DataToBinaryTypeEnum?
    ): String = decrypt(DataToBinaryTypeEnum.BASE64.bytes(data), key, keyType, iv, ivType)

    abstract fun decrypt(
        data: ByteArray,
        key: String,
        keyType: DataToBinaryTypeEnum,
        iv: String?,
        ivType: DataToBinaryTypeEnum?
    ): String

    fun crypto(key: String): Support = Support.crypto(this, key, null)

    fun crypto(key: String, keyType: DataToBinaryTypeEnum): Support = Support.crypto(this, key, keyType, null, null)

    fun crypto(key: String, iv: String): Support = Support.crypto(this, key, iv)

    fun crypto(
        key: String, keyType: DataToBinaryTypeEnum, iv: String, ivType: DataToBinaryTypeEnum
    ): Support = Support.crypto(this, key, keyType, iv, ivType)

    interface Support {
        companion object {
            fun crypto(crypto: CryptoSymmetricEnum, key: String, iv: String?): Support {
                return object : Support {
                    override fun encrypt(data: ByteArray): ByteArray = crypto.encrypt(data, key, iv)

                    override fun decrypt(data: ByteArray): String = crypto.decrypt(data, key, iv)
                }
            }

            fun crypto(
                crypto: CryptoSymmetricEnum,
                key: String,
                keyType: DataToBinaryTypeEnum,
                iv: String?,
                ivType: DataToBinaryTypeEnum?
            ): Support {
                return object : Support {
                    override fun encrypt(data: ByteArray): ByteArray = crypto.encrypt(data, key, keyType, iv, ivType)

                    override fun decrypt(data: ByteArray): String = crypto.decrypt(data, key, keyType, iv, ivType)
                }
            }
        }

        fun encrypt(data: String): ByteArray {
            return encrypt(DataToBinaryTypeEnum.TEXT.bytes(data))
        }

        fun encrypt(data: ByteArray): ByteArray

        fun decrypt(data: String): String {
            return decrypt(DataToBinaryTypeEnum.BASE64.bytes(data))
        }

        fun decrypt(data: ByteArray): String

    }

}