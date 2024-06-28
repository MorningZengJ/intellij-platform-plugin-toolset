package com.github.morningzeng.toolset.config;

import com.github.morningzeng.toolset.annotations.ScratchConfig;
import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ScratchConfig(value = "hash-crypto-prop", directory = "crypto")
public class HashCryptoProp {
    private String key;
    private DataToBinaryTypeEnum keyType;
    private String title;
    private String desc;
    private int sorted;

    public DataToBinaryTypeEnum keyType() {
        return Optional.ofNullable(this.keyType).orElse(DataToBinaryTypeEnum.TEXT);
    }

    @Override
    public String toString() {
        return this.title;
    }
}