package com.github.morningzeng.toolset.config;

import com.github.morningzeng.toolset.enums.AlgorithmEnum;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

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
public class JWTProp {

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

}
