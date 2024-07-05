package com.github.morningzeng.toolset.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author ch.zeng
 * @since 2023-03-30
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(innerTypeName = "Children_")
public abstract class Children<T> {

    /**
     * children
     */
    @JsonAlias({"item"})
    private List<T> children;
    @JsonIgnore
    private T parent;

}
