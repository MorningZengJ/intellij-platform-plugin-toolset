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

    public Map<String, Set<SymmetricCryptoProp>> symmetricCryptoPropsMap() {
        return this.state.symmetricCryptoPropsMap;
    }

    public void symmetricCryptoPropsMap(final Map<String, Set<SymmetricCryptoProp>> symmetricCryptoPropsMap) {
        this.state.symmetricCryptoPropsMap = symmetricCryptoPropsMap;
    }

    public Map<String, Set<HashCryptoProp>> hashCryptoPropsMap() {
        return this.state.hashCryptoPropsMap;
    }

    public void hashCryptoPropsMap(final Map<String, Set<HashCryptoProp>> hashCryptoPropsMap) {
        this.state.hashCryptoPropsMap = hashCryptoPropsMap;
    }

    public Map<String, Set<JWTProp>> jwtPropsMap() {
        return this.state.jwtPropsMap;
    }

    public void jwtPropsMap(final Map<String, Set<JWTProp>> jwtPropsMap) {
        this.state.jwtPropsMap = jwtPropsMap;
    }

    @Data
    public static class State {
        private Map<String, Set<SymmetricCryptoProp>> symmetricCryptoPropsMap = Maps.newHashMap();
        private Map<String, Set<HashCryptoProp>> hashCryptoPropsMap = Maps.newHashMap();
        private Map<String, Set<JWTProp>> jwtPropsMap = Maps.newHashMap();
    }


}
