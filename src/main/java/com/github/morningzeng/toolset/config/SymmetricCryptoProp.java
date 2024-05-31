package com.github.morningzeng.toolset.config;

import com.github.morningzeng.toolset.enums.DataToBinaryTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SymmetricCryptoProp extends HashCryptoProp {
    private String iv;
    private DataToBinaryTypeEnum ivType;

    public DataToBinaryTypeEnum ivType() {
        return Optional.ofNullable(this.ivType).orElse(DataToBinaryTypeEnum.TEXT);
    }

    @Override
    public String toString() {
        return this.getTitle();
    }
}