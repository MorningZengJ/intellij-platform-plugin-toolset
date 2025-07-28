package com.github.morningzeng.toolbox.enums

import org.apache.commons.codec.binary.Hex
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author Morning Zeng
 * @since 2025-05-20
 */
enum class DataToBinaryTypeEnum {

    BASE64 {
        override fun bytes(data: String): ByteArray = Base64.getDecoder().decode(data)

        override fun toString(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
    },
    HEX {
        override fun bytes(data: String): ByteArray = Hex.decodeHex(data)

        override fun toString(bytes: ByteArray): String = Hex.encodeHexString(bytes)
    },
    TEXT {
        override fun bytes(data: String): ByteArray = data.toByteArray(StandardCharsets.UTF_8)

        override fun toString(bytes: ByteArray): String = String(bytes, StandardCharsets.UTF_8)
    },


    ;

    abstract fun bytes(data: String): ByteArray

    abstract fun toString(bytes: ByteArray): String

}