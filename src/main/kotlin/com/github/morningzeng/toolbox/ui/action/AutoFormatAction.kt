package com.github.morningzeng.toolbox.ui.action

import com.github.morningzeng.toolbox.Constants
import com.github.morningzeng.toolbox.enums.JacksonType
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.github.morningzeng.toolbox.utils.LanguageUtils.detectLanguage
import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.json.json5.Json5Language
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.Alarm
import javax.swing.Icon

/**
 * @author Morning Zeng
 * @since 2025-08-15
 */
open class AutoFormatAction(
    val textArea: LanguageTextArea,
    text: String? = "Format",
    icon: Icon? = Constants.IconC.FORMAT_CODE,
    description: String? = null,
) : ToggleAction(text, description, icon), Disposable {

    var enabled: Boolean = false
    private val debounceAlarm = Alarm(this)

    init {
        triggerDelayedFormat()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return super.getActionUpdateThread()
    }

    override fun isSelected(e: AnActionEvent): Boolean = enabled

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        enabled = state
        debounceAlarm.cancelAllRequests()
        format()
    }

    fun format() {
        val project = textArea.project
        textArea.detectLanguage()
        textArea.editor?.let { editor ->
            textArea.language.associatedFileType?.let {
                editor.highlighter = HighlighterFactory.createHighlighter(project, it)
            }
        }
        if (enabled) {
            when (textArea.language) {
                Json5Language.INSTANCE -> {
                    try {
                        JacksonType.JSON.mapper().readValue(textArea.text, Any::class.java).let {
                            textArea.text = JacksonType.JSON.prettySerialize(it).toString()
                        }
                    } catch (_: Exception) {
                    }
                }

                XMLLanguage.INSTANCE -> {
                    try {
                        JacksonType.XML.mapper().readValue(textArea.text, Any::class.java).let {
                            textArea.text = JacksonType.XML.prettySerialize(it).toString()
                        }
                    } catch (_: Exception) {
                    }
                }

                else -> {
                    textArea.language.associatedFileType?.let {
                        val lang = it.language
                        PsiFileFactory.getInstance(project).createFileFromText(lang.id, lang, textArea.text).apply {
                            val document = PsiDocumentManager.getInstance(project).getDocument(this)
                            textArea.setNewDocumentAndFileType(it, document)
                        }
                        triggerDelayedFormat()
                    }
                    textArea.editor?.document?.let { documentEx ->
                        PsiDocumentManager.getInstance(project).getPsiFile(documentEx)?.also {
                            ApplicationManager.getApplication().invokeLater {
                                WriteCommandAction.runWriteCommandAction(project) {
                                    textArea.text = CodeStyleManager.getInstance(project).reformat(it).text
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun dispose() {
        debounceAlarm.cancelAllRequests()
    }

    private fun triggerDelayedFormat() {
        ApplicationManager.getApplication().invokeLater {
            textArea.delayDocumentListener {
                debounceAlarm.cancelAllRequests()
                debounceAlarm.addRequest({ format() }, 300)
            }
        }
    }

}