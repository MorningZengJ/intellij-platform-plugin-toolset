package com.github.morningzeng.toolset.model;

import com.github.morningzeng.toolset.annotations.ScratchConfig;
import com.github.morningzeng.toolset.utils.AsymmetricCrypto;
import com.github.morningzeng.toolset.utils.AsymmetricCrypto.AsymmetricCryptoSupport;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-10-31
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ScratchConfig(value = "asymmetric-crypto-prop", directory = "Crypto")
public class AsymmetricCryptoProp extends Children<AsymmetricCryptoProp> {
    private String title;
    private String key;
    private AsymmetricCrypto crypto;
    private Boolean isPublicKey;
    private String description;
    private int sorted;

    @Override
    public String name() {
        return this.title;
    }

    public AsymmetricCryptoSupport crypto(final Project project, final AsymmetricCrypto crypto) {
        if (Objects.isNull(this.isPublicKey)) {
            Messages.showErrorDialog(project, "Please select key type", "Error");
            return null;
        }
        return this.isPublicKey ? crypto.publicKey(this.key) : crypto.privateKey(this.key);
    }
}
