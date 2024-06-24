package com.github.morningzeng.toolset.support;

import com.github.morningzeng.toolset.Constants;
import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.config.LocalConfigFactory.State;

import static com.github.morningzeng.toolset.utils.JacksonUtils.IGNORE_TRANSIENT_AND_NULL;

/**
 * @author Morning Zeng
 * @since 2024-06-24
 */
public interface ComponentStatePersistenceSupport {

    static String key(final Class<?> componentClass, final String key) {
        return String.join(Constants.COLON, componentClass.getName(), key);
    }

    default <T> void write(final String key, final T value) {
        final State state = LocalConfigFactory.getInstance().getState();
        state.componentState(key(this.getClass(), key), IGNORE_TRANSIENT_AND_NULL.toJson(value));
        LocalConfigFactory.getInstance().loadState(state);
    }

    default <T> T read(final String key, final Class<T> tClass) {
        final State state = LocalConfigFactory.getInstance().getState();
        final String value = state.componentState().get(key(this.getClass(), key));
        return IGNORE_TRANSIENT_AND_NULL.fromJson(value, tClass);
    }

}
