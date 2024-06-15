package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.enums.DataFormatTypeEnum;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import lombok.extern.slf4j.Slf4j;

import java.awt.GridBagLayout;
import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-06-07
 */
@Slf4j
public final class HttpComponent extends JBPanel<JBPanelWithEmptyText> {

    public HttpComponent(final Project project) {
        final String json = "{\"a\":123}";
        final DataFormatTypeEnum dataFormatType = DataFormatTypeEnum.fileType(json);
        final LanguageTextArea textField = new LanguageTextArea(dataFormatType.getLanguage(), project, json);

        Optional.ofNullable(textField.getEditor(true))
                .map(Editor::getSettings)
                .ifPresent(editorSettings -> editorSettings.setLineNumbersShown(true));

        setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.BOTH).weightX(1).weightY(1).add(textField);
    }

}
