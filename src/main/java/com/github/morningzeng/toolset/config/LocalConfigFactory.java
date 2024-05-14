package com.github.morningzeng.toolset.config;

import com.google.common.collect.Sets;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

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

    public Set<SymmetricCryptoProp> symmetricCryptos() {
        return this.state.symmetricCryptoProps;
    }

    public void symmetricCryptos(final Set<SymmetricCryptoProp> symmetricCryptoProps) {
        this.state.symmetricCryptoProps = symmetricCryptoProps;
    }

    @Data
    public static class State {
        private Set<SymmetricCryptoProp> symmetricCryptoProps = Sets.newHashSet();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class SymmetricCryptoProp {
        private String key;
        private String iv;
        private String title;
        private String desc;
        private int sorted;
    }

}
