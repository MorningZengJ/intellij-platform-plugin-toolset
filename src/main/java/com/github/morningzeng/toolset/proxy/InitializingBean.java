package com.github.morningzeng.toolset.proxy;

import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * @author Morning Zeng
 * @since 2024-11-07
 */
public interface InitializingBean {

    @SneakyThrows
    static <T extends InitializingBean> T create(final Class<T> clazz, Object... args) {
        final Class<?>[] argClasses = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        final Constructor<T> constructor = clazz.getDeclaredConstructor(argClasses);
        constructor.trySetAccessible();
        final T t = constructor.newInstance(args);
        t.afterPropertiesSet();
        return t;
    }

    void afterPropertiesSet() throws Exception;

}
