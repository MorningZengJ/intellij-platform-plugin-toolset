package com.github.morningzeng.toolset.component;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-06-15 13:08:51
 */
public final class LanguageTextArea extends LanguageTextField {

    private final Project project;


    public LanguageTextArea(final Language language, final Project project, @NotNull final String value) {
        super(language, project, value);
        this.project = project;
        this.setOneLineMode(false);
        this.reformatCode();

        this.initEvent();
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

    public void setLanguage(final Language language) {
        final LanguageFileType fileType = language.getAssociatedFileType();
        if (Objects.nonNull(fileType)) {
            this.setNewDocumentAndFileType(fileType, this.getDocument());
            Optional.ofNullable(this.getEditor(true))
                    .ifPresent(editorEx -> editorEx.setHighlighter(HighlighterFactory.createHighlighter(this.project, fileType)));
        }
    }

    public void reformatCode() {
        final PsiFile psiFile = PsiDocumentManager.getInstance(this.project)
                .getPsiFile(this.getDocument());
        ApplicationManager.getApplication().invokeLater(
                () -> WriteCommandAction.runWriteCommandAction(this.project, () -> {
                    final CodeStyleManager instance = CodeStyleManager.getInstance(this.project);
                    Optional.ofNullable(psiFile).ifPresent(ps -> {
                        if (ps.getLanguage() == PlainTextLanguage.INSTANCE) {
                            this.setText(StringUtil.convertLineSeparators(this.getText()));
                            return;
                        }
                        instance.reformat(ps);
                    });
                })
        );
    }

    private void initEvent() {
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                reformatCode();
            }
        });

        final ShortcutSet shortcutSet = ActionManager.getInstance().getAction("ReformatCode").getShortcutSet();
        DumbAwareAction.create(e -> this.reformatCode())
                .registerCustomShortcutSet(shortcutSet, this);
    }
}