package com.github.morningzeng.toolset.popup;

import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Morning Zeng
 * @since 2024-06-25
 */
public class ReformatPopupMenuAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (Objects.isNull(editor)) {
            return;
        }
        final Boolean editorUserData = editor.getUserData(LanguageTextArea.LANGUAGE_TEXT_AREA_REFORMAT_POPUP_ACTION_KEY);
        final Boolean enabled = Optional.ofNullable(editorUserData).orElse(false);
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
//        JBPopupFactory.getInstance().
        final Editor editor = e.getDataContext().getData(LangDataKeys.EDITOR);
        if (editor != null) {
            Optional.ofNullable(PsiUtilBase.getElementAtCaret(editor))
                    .map(PsiElement::getContainingFile)
                    .ifPresent(psiFile -> {
                        int startOffset = EditorUtil.getSelectionInAnyMode(editor).getStartOffset();
                        int endOffset = EditorUtil.getSelectionInAnyMode(editor).getEndOffset();
                        CodeStyleManager.getInstance(psiFile.getProject()).reformatText(psiFile, startOffset, endOffset);
                    });
        }
    }

}
