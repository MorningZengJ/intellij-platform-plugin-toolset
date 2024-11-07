package com.github.morningzeng.toolset.proxy;

import com.github.morningzeng.toolset.model.Pair;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;

/**
 * @author Morning Zeng
 * @since 2024-11-07
 */
public interface InitializingBean {

    @SafeVarargs
    @SneakyThrows
    static <T extends InitializingBean> T create(final Class<T> clazz, Pair<Class<?>, Object>... pairs) {
        final Class<?>[] argClasses = new Class[pairs.length];
        final Object[] args = new Object[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            final Pair<Class<?>, Object> pair = pairs[i];
            argClasses[i] = pair.key();
            args[i] = pair.value();
        }
        final Constructor<T> constructor = clazz.getDeclaredConstructor(argClasses);
        constructor.trySetAccessible();
        final T t = constructor.newInstance(args);
        t.afterPropertiesSet();
        return t;
    }

    void afterPropertiesSet() throws Exception;

}
