package com.github.morningzeng.toolset.ui.crypto;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Morning Zeng
 * @since 2024-05-11
 */
public final class DESComponent extends SymmetricCryptoComponent {

    private final static String TYPE = "DES";


    /**
     * Initializes a new instance of the AESComponent class.
     * <p>
     * This constructor initializes the layout and action listeners for the AESComponent.
     * It calls the initLayout() and initAction() methods to set up the UI components and their actions.
     */
    public DESComponent(final Project project) {
        super(project);
    }

    @Override
    @NotNull String getType() {
        return TYPE;
    }

}
