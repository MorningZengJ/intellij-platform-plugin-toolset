package com.github.morningzeng.toolbox.model

import com.github.morningzeng.toolbox.annotations.ScratchConfig
import com.github.morningzeng.toolbox.enums.CryptoAsymmetricEnum

/**
 * @author Morning Zeng
 * @since 2025-05-23
 */
@ScratchConfig(value = "asymmetric-crypto-prop", directory = "Crypto")
data class CryptoAsymmetric(
    var title: String = "",
    var key: String = "",
    var crypto: CryptoAsymmetricEnum = CryptoAsymmetricEnum.RSA,
    var isPublicKey: Boolean = true,
    var description: String = "",
) : Children<CryptoAsymmetric>() {

    constructor(
        title: String,
        crypto: CryptoAsymmetricEnum,
        directory: Boolean
    ) : this(title, crypto = crypto) {
        super.directory = directory
    }

    override fun name(): String = title

    fun crypto(): CryptoAsymmetricEnum.CryptoAsymmetricSupport {
        return if (isPublicKey) crypto.publicKey(key) else crypto.privateKey(key)
    }

}
