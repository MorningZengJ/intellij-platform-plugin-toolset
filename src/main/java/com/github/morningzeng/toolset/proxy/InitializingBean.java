package com.github.morningzeng.toolset.proxy;

import com.github.morningzeng.toolset.model.Pair;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author Morning Zeng
 * @since 2024-11-07
 */
public interface InitializingBean {

    @SneakyThrows
    static <T extends InitializingBean> T create(final Class<T> clazz, final Object... args) {
        return InitializingBean.create(clazz, Collections.emptyMap(), args);
    }

    @SneakyThrows
    static <T extends InitializingBean> T create(final Class<T> clazz, final Map<Object, Class<?>> map, final Object... args) {
        final Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            final Class<?> argType = map.getOrDefault(args[i], args[i].getClass());
            argClasses[i] = argType;
        }
        final Constructor<T> constructor = clazz.getDeclaredConstructor(argClasses);
        constructor.trySetAccessible();
        final T t = constructor.newInstance(args);
        t.afterPropertiesSet();
        return t;
    }

    @SafeVarargs
    @SneakyThrows
    static <T extends InitializingBean> T create(final Class<T> clazz, Pair<Class<?>, Object>... pairs) {
        final Map<Object, Class<?>> map = Maps.newHashMap();
        final Object[] args = Arrays.stream(pairs)
                .map(pair -> {
                    map.put(pair.value(), pair.key());
                    return pair.value();
                })
                .toArray(Object[]::new);
        return InitializingBean.create(clazz, map, args);
    }

    void afterPropertiesSet() throws Exception;

}
