package com.github.morningzeng.toolset.ui.crypto;

import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.model.Children;
import com.github.morningzeng.toolset.ui.AbstractCryptoPropComponent;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-07-09
 */
@Slf4j
public sealed abstract class AbstractCryptoComponent<T extends Children<T>> extends AbstractCryptoPropComponent<T> permits AbstractSymmetricCryptoComponent, AsymmetricComponent, HashComponent {
    protected final LanguageTextArea encryptArea;
    protected final LanguageTextArea decryptArea;

    public AbstractCryptoComponent(final Project project) {
        super(project);
        this.encryptArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.decryptArea = new LanguageTextArea(PlainTextLanguage.INSTANCE, project, "");
        this.encryptArea.setPlaceholder("Encrypted text content");
        this.decryptArea.setPlaceholder("Decrypted text content");
    }

    @Override
    protected Stream<T> flatProps(final List<T> props) {
        return props.stream()
                .mapMulti((prop, consumer) -> {
                    consumer.accept(prop);
                    Optional.ofNullable(prop.getChildren()).ifPresent(
                            ts -> ts.stream()
                                    .filter(this.filterProp())
                                    .sorted(this.comparator())
                                    .forEach(consumer)
                    );
                });
    }

}
