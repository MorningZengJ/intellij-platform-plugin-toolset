package com.github.morningzeng.toolset.ui.crypto;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Morning Zeng
 * @since 2024-10-29
 */
public final class BlowfishComponent extends SymmetricCryptoComponent {

    private final static String TYPE = "Blowfish";

    public BlowfishComponent(final Project project) {
        super(project);
    }

    @Override
    @NotNull String getType() {
        return TYPE;
    }
}
