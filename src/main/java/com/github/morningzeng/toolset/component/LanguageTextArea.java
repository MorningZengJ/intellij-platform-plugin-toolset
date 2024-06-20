package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.utils.LanguageUtils;
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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private boolean readOnly;
    private boolean showNumber = true;
    private EditorEx editor;
    private Language language;

    public LanguageTextArea(final Language language, final Project project, @NotNull final String value) {
        this(language, project, value, false);
    }

    public LanguageTextArea(final Language language, final Project project, @NotNull final String value, final boolean readOnly) {
        super(language, project, value);
        this.language = language;
        this.readOnly = readOnly;

        this.project = project;
        this.setOneLineMode(false);
        this.reformatCode();

        this.initEvent();
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        this.editor = super.createEditor();
        this.editor.setVerticalScrollbarVisible(true);
        this.editor.setHorizontalScrollbarVisible(true);
        this.editor.setViewer(this.readOnly);

        final EditorSettings settings = this.editor.getSettings();
        settings.setLineNumbersShown(this.showNumber);
        settings.setAutoCodeFoldingEnabled(true);
        settings.setFoldingOutlineShown(true);
        settings.setAllowSingleLogicalLineFolding(true);
        settings.setRightMarginShown(true);

        return editor;
    }

    public void setLanguage(final Language language) {
        final LanguageFileType fileType = language.getAssociatedFileType();
        if (Objects.nonNull(fileType)) {
            this.language = language;
            this.setNewDocumentAndFileType(fileType, this.getDocument());
            this.editor.setHighlighter(HighlighterFactory.createHighlighter(this.project, fileType));
            this.reformatCode();
        }
    }

    public void reformatCode() {
        final PsiFile psiFile = PsiFileFactory.getInstance(this.project)
                .createFileFromText(this.language.getID(), this.language, this.getText());
        ApplicationManager.getApplication().invokeLater(
                () -> WriteCommandAction.runWriteCommandAction(this.project, () -> {
                    final CodeStyleManager instance = CodeStyleManager.getInstance(this.project);
                    Optional.ofNullable(psiFile).ifPresent(ps -> {
                        if (ps.getLanguage() == PlainTextLanguage.INSTANCE) {
                            this.setText(StringUtil.convertLineSeparators(this.getText()));
                            return;
                        }
                        final String text = instance.reformat(ps).getText();
                        this.setText(text);
                    });
                })
        );
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
        Optional.ofNullable(this.editor)
                .ifPresent(editorEx -> editorEx.setViewer(true));
    }

    public void setLineNumbersShown(final boolean showNumber) {
        this.showNumber = showNumber;
        Optional.ofNullable(this.editor)
                .map(EditorEx::getSettings)
                .ifPresent(editorSettings -> editorSettings.setLineNumbersShown(showNumber));
    }

    @Override
    public void setText(@Nullable final String text) {
        final Language language = LanguageUtils.tryResolve(text);
        super.setText(text);
        this.reformatCode();
        if (!Objects.equals(this.language, language)) {
            this.setLanguage(language);
        }
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