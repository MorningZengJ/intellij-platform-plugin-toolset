package com.github.morningzeng.toolbox.ui.component

import com.github.morningzeng.toolbox.ui.bar.ActionBar
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.util.textCompletion.TextCompletionUtil

/**
 * @author Morning Zeng
 * @since 2025-05-19
 */
open class LanguageTextArea(
    project: Project,
    value: String = "",
    var language: Language = PlainTextLanguage.INSTANCE,
    readOnly: Boolean = false,
    var showLineNumber: Boolean = true,
    var showHint: Boolean = false,
    var oneline: Boolean = false,
) : LanguageTextField(language, project, value, false) {

    init {
        isOneLineMode = oneline
    }

    var editor: EditorEx? = null

    var readOnly: Boolean = readOnly
        set(value) {
            field = value
            editor?.isViewer = value
        }

    override fun createEditor(): EditorEx {
        val editorEx = super.createEditor()
        editorEx.setVerticalScrollbarVisible(true)
        editorEx.setHorizontalScrollbarVisible(true)
        editorEx.isViewer = this.readOnly

        editorEx.settings.also {
            it.isLineNumbersShown = showLineNumber
            it.isAutoCodeFoldingEnabled = true
            it.isFoldingOutlineShown = true
            it.isRightMarginShown = true
            it.isAllowSingleLogicalLineFolding = true
        }

        language.associatedFileType?.let {
            editorEx.highlighter = HighlighterFactory.createHighlighter(project, it)
        }

        ApplicationManager.getApplication().runReadAction {
            SpellCheckingEditorCustomizationProvider.getInstance().disabledCustomization?.customize(editorEx)
        }

        if (showHint) TextCompletionUtil.installCompletionHint(editorEx)
        this.editor = editorEx
        return editorEx
    }

    fun withRightBar(vararg actions: AnAction): JBPanel<JBPanelWithEmptyText> {
        return GridBagUtils.builder()
            .fill(GridBagUtils.GridBagFill.BOTH)
            .row {
                ActionBar(*defaultRightBarActions(), *actions, horizontal = false).apply {
                    it.cell().weightX(1.0).weightY(1.0).add(this@LanguageTextArea)
                        .cell().weightX(0.0).add(this)
                }
            }
            .build()
    }

    open fun defaultRightBarActions(): Array<AnAction> {
        return arrayOf(softWrapAction(), scrollToEndAction(), clearAllAction())
    }

    protected fun softWrapAction(): ToggleAction {
        return object : ToggleAction("Soft-Wrap", "Soft-Wrap", AllIcons.Actions.ToggleSoftWrap) {
            override fun getActionUpdateThread(): ActionUpdateThread {
                return super.getActionUpdateThread()
            }

            override fun isSelected(e: AnActionEvent): Boolean {
                return editor?.settings?.isUseSoftWraps ?: false
            }

            override fun setSelected(e: AnActionEvent, state: Boolean) {
                editor?.settings?.isUseSoftWraps = state
            }
        }
    }

    protected fun scrollToEndAction(): AnAction {
        return object : AnAction("Scroll to End", "Scroll to End", AllIcons.RunConfigurations.Scroll_down) {
            override fun actionPerformed(e: AnActionEvent) {
                editor?.document?.apply {
                    val lastLine = lineCount - 1
                    val lineStartOffset = getLineStartOffset(lastLine)
                    editor?.caretModel?.moveToOffset(lineStartOffset)
                    editor?.scrollingModel?.scrollToCaret(ScrollType.MAKE_VISIBLE)
                }
            }
        }
    }

    protected fun clearAllAction(): AnAction {
        return object : AnAction("Clear All", "Clear all", AllIcons.Actions.GC) {
            override fun actionPerformed(e: AnActionEvent) {
                text = ""
            }
        }
    }

    protected fun upperCaseAction(callback: (state: Boolean) -> Unit = {}): ToggleAction {
        var upperCase = true
        return object : ToggleAction("Upper Case", "Upper Case UUID", AllIcons.Actions.MatchCase) {
            override fun getActionUpdateThread(): ActionUpdateThread {
                return super.getActionUpdateThread()
            }

            override fun isSelected(e: AnActionEvent): Boolean = upperCase

            override fun setSelected(e: AnActionEvent, state: Boolean) {
                upperCase = state
                callback(upperCase)
            }
        }
    }

}