package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.enums.DataFormatTypeEnum;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.intellij.lang.Language;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.GridBag;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        final Document document = EditorFactory.getInstance().createDocument(json);
        final DataFormatTypeEnum dataFormatType = DataFormatTypeEnum.fileType(document.getText());
        final FileType fileType = dataFormatType.getFileType();
        final JT textField = new JT(dataFormatType.getLanguage(), project, json);

        Optional.ofNullable(textField.getEditor(true))
                .map(Editor::getSettings)
                .ifPresent(editorSettings -> editorSettings.setLineNumbersShown(true));

        final LightVirtualFile virtualFile = new LightVirtualFile("temp", fileType, document.getText());
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        textField.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                final PsiDocumentManager psiInstance = PsiDocumentManager.getInstance(project);
                psiInstance.commitDocument(document);
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    psiInstance.doPostponedOperationsAndUnblockDocument(document);
                    Optional.ofNullable(psiFile).ifPresent(CodeStyleManager.getInstance(project)::reformat);
                });
            }
        });

        setLayout(new GridBagLayout());
        GridLayoutUtils.builder()
                .container(this).fill(GridBag.BOTH).weightX(1).weightY(1).add(textField);
    }

    public static class JT extends LanguageTextField {
        public JT() {
        }

        public JT(final Language language, @Nullable final Project project, @NotNull final String value) {
            super(language, project, value);
        }

        @Override
        protected @NotNull EditorEx createEditor() {
            final EditorEx editor = super.createEditor();
            editor.setVerticalScrollbarVisible(true);
            editor.setHorizontalScrollbarVisible(true);

            final EditorSettings settings = editor.getSettings();
            settings.setLineNumbersShown(true);
            settings.setAutoCodeFoldingEnabled(true);
            settings.setFoldingOutlineShown(true);
            settings.setAllowSingleLogicalLineFolding(true);
            settings.setRightMarginShown(true);
            return editor;
        }
    }

}
