package com.github.morningzeng.toolset.component;

import com.github.morningzeng.toolset.utils.LanguageUtils;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.TextFieldWithAutoCompletion.StringsCompletionProvider;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.textCompletion.TextCompletionProvider;
import com.intellij.util.textCompletion.TextCompletionUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-06-15 13:08:51
 */
public final class LanguageTextArea extends LanguageTextField {

    public static final Key<Boolean> LANGUAGE_TEXT_AREA_REFORMAT_POPUP_ACTION_KEY = new Key<>(LanguageTextArea.class.getName());

    private final Project project;
    private final boolean forceAutoPopup;
    private final boolean showHint;
    private boolean readOnly;
    private boolean showNumber = true;
    @Getter
    @Accessors(fluent = true)
    private EditorEx editor;
    private Language language;
    private TextCompletionProvider provider;
    private boolean autoReformat = true;

    public LanguageTextArea(final Language language, final Project project, @NotNull final String value) {
        this(language, project, value, false);
    }

    public LanguageTextArea(final Language language, final Project project, @NotNull final String value, final boolean readOnly) {
        super(language, project, value, false);
        this.language = language;
        this.forceAutoPopup = false;
        this.showHint = false;
        this.readOnly = readOnly;

        this.project = project;
        this.reformatCode();

        this.initEvent();
    }

    public LanguageTextArea(final Language language, final Project project, @NotNull TextCompletionProvider provider,
                            @NotNull final String value,
                            boolean oneLineMode,
                            boolean autoPopup,
                            boolean forceAutoPopup,
                            boolean showHint,
                            boolean forbidWordCompletion) {
        super(language, project, value, new TextCompletionUtil.DocumentWithCompletionCreator(provider, autoPopup, forbidWordCompletion), oneLineMode);
        this.project = project;
        this.language = language;
        this.forceAutoPopup = forceAutoPopup;
        this.showHint = showHint;
        this.provider = provider;

        this.reformatCode();

        this.initEvent();
    }

    public static LanguageTextArea create(final Project project, final Language language, Collection<String> items, final String value) {
        return create(project, language, items, null, value);
    }

    public static LanguageTextArea create(final Project project, final Language language, Collection<String> items, final Icon icon, final String value) {
        return new LanguageTextArea(language, project, new StringsCompletionProvider(items, icon), value, false, true, false, true, false);
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

        Optional.ofNullable(SpellCheckingEditorCustomizationProvider.getInstance().getDisabledCustomization())
                .ifPresent(disableSpellChecking -> disableSpellChecking.customize(this.editor));
        this.editor.putUserData(AutoPopupController.ALWAYS_AUTO_POPUP, this.forceAutoPopup);
        if (this.showHint) {
            TextCompletionUtil.installCompletionHint(this.editor);
        }

        this.editor.putUserData(LANGUAGE_TEXT_AREA_REFORMAT_POPUP_ACTION_KEY, true);

        return this.editor;
    }

    public void setLanguage(final Language language) {
        if (Objects.equals(this.language, language)) {
            return;
        }
        final LanguageFileType fileType = language.getAssociatedFileType();
        if (Objects.nonNull(fileType)) {
            this.language = language;
            final PsiFile psiFile = PsiFileFactory.getInstance(this.project)
                    .createFileFromText(this.language.getID(), this.language, this.getText());
            if (this.showHint) {
                TextCompletionUtil.installProvider(psiFile, this.provider, true);
            }
            final Document document = PsiDocumentManager.getInstance(this.project).getDocument(psiFile);
            this.setNewDocumentAndFileType(fileType, document);
            this.editor.setHighlighter(HighlighterFactory.createHighlighter(this.project, fileType));
            if (this.autoReformat) {
                this.reformatCode();
            }
        }
    }

    public void reformatCode() {
        final PsiFile psiFile = PsiDocumentManager.getInstance(this.project).getPsiFile(this.getDocument());
        ApplicationManager.getApplication().invokeLater(
                () -> WriteCommandAction.runWriteCommandAction(this.project, () -> {
                    final CodeStyleManager instance = CodeStyleManager.getInstance(this.project);
                    Optional.ofNullable(psiFile).ifPresent(ps -> {
                        if (ps.getLanguage() == PlainTextLanguage.INSTANCE) {
                            super.setText(StringUtil.convertLineSeparators(this.getText()));
                            return;
                        }
                        final String text = instance.reformat(ps).getText();
                        super.setText(text);
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

    public <T> void installProvider(@NotNull TextFieldWithAutoCompletionListProvider<T> provider) {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(this.getDocument());
        if (psiFile != null) {
            TextCompletionUtil.installProvider(psiFile, provider, true);
        }
    }

    @Override
    public void setText(@Nullable final String text) {
        final Language language = LanguageUtils.tryResolve(text);
        super.setText(text);
        if (Objects.equals(this.language, language)) {
            if (this.autoReformat) {
                this.reformatCode();
            }
            return;
        }
        this.setLanguage(language);
    }

    public void releaseEditor() {
        if (Objects.isNull(this.editor)) {
            return;
        }
        final EditorImpl editorImpl = (EditorImpl) this.editor;
        final Boolean released = ReflectionUtil.getField(editorImpl.getClass(), editorImpl, boolean.class, "isReleased");
        if (!Optional.ofNullable(released).orElse(false)) {
            EditorFactory.getInstance().releaseEditor(this.editor);
        }
    }

    public void autoReformat(final boolean autoReformat) {
        this.autoReformat = autoReformat;
    }

    private void initEvent() {
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                final Language lan = LanguageUtils.tryResolve(getText());
                if (Objects.equals(language, lan)) {
                    reformatCode();
                    return;
                }
                setLanguage(lan);
            }
        });

        final ShortcutSet shortcutSet = ActionManager.getInstance().getAction("ReformatCode").getShortcutSet();
        DumbAwareAction.create(e -> this.reformatCode())
                .registerCustomShortcutSet(shortcutSet, this);
    }

}