package com.github.morningzeng.toolset.model;

import com.github.morningzeng.toolset.annotations.ScratchConfig;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-07-08
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ScratchConfig(value = "hash-crypto-prop", directory = "Crypto")
public class HashCryptoProp extends Children<HashCryptoProp> {
    @Builder.Default
    private final boolean directory = false;
    private String title;
    private String key;
    private DataToBinaryTypeEnum keyType;
    private String description;
    private int sorted;

    public DataToBinaryTypeEnum keyType() {
        return Optional.ofNullable(this.keyType).orElse(DataToBinaryTypeEnum.TEXT);
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
