package com.github.morningzeng.toolbox.model

import com.github.morningzeng.toolbox.annotations.ScratchConfig
import com.github.morningzeng.toolbox.enums.DataToBinaryTypeEnum

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
@ScratchConfig(value = "symmetric-crypto-prop", directory = "Crypto")
data class CryptoSymmetric(
    var title: String = "",
    var key: String = "",
    var keyType: DataToBinaryTypeEnum = DataToBinaryTypeEnum.TEXT,
    var iv: String = "",
    var ivType: DataToBinaryTypeEnum = DataToBinaryTypeEnum.TEXT,
    var description: String = "",
) : Children<CryptoSymmetric>() {

    constructor(title: String, directory: Boolean) : this(title) {
        this.directory = directory
    }

    override fun name(): String = this.title

}
