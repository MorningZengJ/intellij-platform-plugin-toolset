package com.github.morningzeng.toolset.config;

import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * @author Morning Zeng
 * @since 2024-05-11
 */
@Service(Level.APP)
@State(name = "LocalConfigFactory", storages = {@Storage(StoragePathMacros.NON_ROAMABLE_FILE)})
public final class LocalConfigFactory implements PersistentStateComponent<LocalConfigFactory.State> {

    private final State state = new State();

    public static LocalConfigFactory getInstance() {
        return ApplicationManager.getApplication().getService(LocalConfigFactory.class);
    }

    @Override
    public @NotNull State getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull final State state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    @Data
    public static class State {
        private Map<String, String> componentState = Maps.newHashMap();

        private Map<String, Set<HashCryptoProp>> hashCryptoPropsMap = Maps.newHashMap();

        public Map<String, String> componentState() {
            return this.componentState;
        }

        public void componentState(final String key, final String value) {
            this.componentState.put(key, value);
        }

        public Map<String, Set<HashCryptoProp>> hashCryptoPropsMap() {
            return this.hashCryptoPropsMap;
        }

        public void hashCryptoPropsMap(final Map<String, Set<HashCryptoProp>> hashCryptoPropsMap) {
            this.hashCryptoPropsMap = hashCryptoPropsMap;
        }

    }


}
