package com.github.morningzeng.toolset.ui.coding;

import com.intellij.openapi.project.Project;

import java.util.Base64;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public final class Base64Component extends AbstractCodingComponent {

    public Base64Component(final Project project) {
        super(project);
        this.encodeArea.setPlaceholder("Base64 encoded text");
        this.decodeArea.setPlaceholder("Base64 decoded text");
    }

    @Override
    Supplier<String> encode() {
        return () -> Base64.getEncoder().encodeToString(this.decodeArea.getText().getBytes(UTF_8));
    }

    @Override
    Supplier<String> decode() {
        return () -> new String(Base64.getDecoder().decode(this.encodeArea.getText()), UTF_8);
    }

}
