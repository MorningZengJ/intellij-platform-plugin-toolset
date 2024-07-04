package com.github.morningzeng.toolset.model;

/**
 * @author Morning Zeng
 * @since 2024-07-01
 */
public final class PairVariable<K, V> extends Pair<K, V> {

    private PairVariable(K key, V value) {
        super(key, value);
    }

    public static <K, V> PairVariable<K, V> of(K key, V value) {
        return new PairVariable<>(key, value);
    }

    public PairVariable<K, V> set(K key, V value) {
        super.key = key;
        super.value = value;
        return this;
    }

    public K getKey() {
        return super.key;
    }

    public PairVariable<K, V> setKey(final K k) {
        return set(k, super.value);
    }

    public V getValue() {
        return super.value;
    }

    public PairVariable<K, V> setValue(final V v) {
        return set(super.key, v);
    }

}
