package com.github.morningzeng.toolset.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import io.jsonwebtoken.security.SecureDigestAlgorithm;

import java.security.Key;

/**
 * @author Morning Zeng
 * @since 2024-05-29
 */
public class JWTComponent extends JBPanel<JBPanelWithEmptyText> {

    private final Project project;

    private final ComboBox<Key> signKeyComboBox = new ComboBox<>();
    private final ComboBox<SecureDigestAlgorithm<? extends Key, ?>> signAlgorithmComboBox = new ComboBox<>();

    public JWTComponent(final Project project) {
        this.project = project;
//        Jwts.builder().signWith()
    }
}
