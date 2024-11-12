package com.github.morningzeng.toolset.ui.coding;

import com.intellij.openapi.project.Project;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Morning Zeng
 * @since 2024-05-21
 */
public final class URLComponent extends AbstractCodingComponent {

    public URLComponent(final Project project) {
        super(project);
        this.encodeArea.setPlaceholder("URL encoded text");
        this.decodeArea.setPlaceholder("URL decoded text");
    }

    @Override
    Supplier<String> encode() {
        return () -> URLEncoder.encode(this.decodeArea.getText(), UTF_8);
    }

    @Override
    Supplier<String> decode() {
        return () -> URLDecoder.decode(this.encodeArea.getText(), UTF_8);
    }

}
