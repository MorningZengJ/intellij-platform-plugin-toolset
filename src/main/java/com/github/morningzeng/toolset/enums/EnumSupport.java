package com.github.morningzeng.toolset.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-02-22 10:44:37
 */
public sealed interface EnumSupport<T> permits HttpBodyParamTypeEnum, HttpBodyTypeEnum {

    static <T, E extends EnumSupport<T>> Map<T, E> get(Class<E> eClass) {
        if (!eClass.isEnum()) {
            throw new IllegalArgumentException("类型错误");
        }
        if (!EnumBean.MAP.containsKey(eClass)) {
            final Map<T, E> enumMap = Arrays.stream(eClass.getEnumConstants()).collect(
                    Collectors.toMap(EnumSupport::key, Function.identity())
            );
            EnumBean.MAP.put(eClass, Collections.unmodifiableMap(enumMap));
        }
        //noinspection unchecked
        return (Map<T, E>) EnumBean.MAP.get(eClass);
    }

    T key();

    class EnumBean {
        private final static Map<Class<?>, Map<?, ? extends EnumSupport<?>>> MAP = new ConcurrentHashMap<>();
    }

}
