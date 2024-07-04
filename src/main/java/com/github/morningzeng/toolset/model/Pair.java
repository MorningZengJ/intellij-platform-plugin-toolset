package com.github.morningzeng.toolset.model;

import com.github.morningzeng.toolset.model.HttpBean.PairWithTypeDescription;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * @author Morning Zeng
 * @since 2024-07-01
 */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Accessors(fluent = true)
public sealed class Pair<K, V> permits PairWithTypeDescription, PairVariable {

    protected K key;
    protected V value;

    protected Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

}
