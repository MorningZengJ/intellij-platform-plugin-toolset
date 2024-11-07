package com.github.morningzeng.toolset.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.morningzeng.toolset.Constants.IconC;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import javax.swing.Icon;
import java.util.List;
import java.util.Objects;

/**
 * @author ch.zeng
 * @since 2023-03-30
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(innerTypeName = "Children_")
public abstract class Children<T> {

    @Builder.Default
    private final boolean directory = false;
    /**
     * children
     */
    @JsonAlias({"item"})
    private List<T> children;
    @JsonIgnore
    private T parent;

    public void addChild(T child) {
        if (Objects.isNull(this.children)) {
            this.children = Lists.newArrayList();
        }
        this.children.add(child);
    }

    public Icon icon() {
        return this.isGroup() ? IconC.FOLDER_COLOR : IconC.TREE_NODE;
    }

    public abstract String name();

    public boolean isGroup() {
        return this.directory;
    }

}
