package com.github.morningzeng.toolset.model;

import com.github.morningzeng.toolset.annotations.ScratchConfig;
import com.github.morningzeng.toolset.enums.AlgorithmEnum;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-05-30
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ScratchConfig(value = "jwt-crypto-prop", directory = "Crypto")
public class JWTProp extends Children<JWTProp> {

    @Builder.Default
    private final boolean directory = false;
    private String title;
    private AlgorithmEnum signAlgorithm;
    private String symmetricKey;
    private DataToBinaryTypeEnum symmetricKeyType;
    private String privateKey;
    private String publicKey;
    private String desc;
    private int sorted;

    public AlgorithmEnum signAlgorithm() {
        return Optional.ofNullable(this.signAlgorithm).orElse(AlgorithmEnum.HS512);
    }

    public DataToBinaryTypeEnum symmetricKeyType() {
        return Optional.ofNullable(this.symmetricKeyType).orElse(DataToBinaryTypeEnum.TEXT);
    }

    public SecretKeySpec secretKeySpec() {
        return new SecretKeySpec(this.symmetricKeyType().bytes(this.getSymmetricKey()), this.signAlgorithm().getJcaName());
    }

    @Override
    public String toString() {
        return this.title;
    }

    @Override
    public String name() {
        return this.title;
    }

    @Override
    public boolean isGroup() {
        return this.directory;
    }
}
