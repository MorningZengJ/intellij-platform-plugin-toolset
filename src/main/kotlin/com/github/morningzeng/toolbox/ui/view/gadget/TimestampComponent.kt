package com.github.morningzeng.toolbox.ui.view.gadget

import com.github.morningzeng.toolbox.ui.bar.RadioBar
import com.github.morningzeng.toolbox.ui.component.LanguageTextArea
import com.github.morningzeng.toolbox.utils.GridBagUtils
import com.google.common.collect.Lists
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.Alarm
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.swing.plaf.basic.BasicComboBoxEditor

/**
 * @author Morning Zeng
 * @since 2025-07-23
 */
class TimestampComponent(val project: Project) : JBPanel<JBPanelWithEmptyText>(), Disposable {

    private val alarm = Alarm(this)
    private val currentTimestamp = LabeledComponent.create(
        JBTextField().apply { isEditable = false }, "Current timestamp", BorderLayout.WEST
    )
    private val currentDatetime = LabeledComponent.create(
        JBTextField().apply { isEditable = false }, "Current datetime", BorderLayout.WEST
    )
    private val formatterModel = MutableCollectionComboBoxModel(
        Lists.newArrayList<String>(
            "yyyy-MM-dd HH:mm:ss",
        )
    )
    private val formatter = LabeledComponent.create(
        ComboBox(formatterModel), "Formatter", BorderLayout.WEST
    )
    private val timeunit = LabeledComponent.create(
        RadioBar(TimeUnit.SECONDS, listOf(TimeUnit.SECONDS, TimeUnit.MILLISECONDS)),
        "Timeunit", BorderLayout.WEST
    )
    private val area = LanguageTextArea(project, "", oneline = true)

    private val formatterMap: MutableMap<String, DateTimeFormatter> =
        formatterModel.items.associateWith { DateTimeFormatter.ofPattern(it) }.toMutableMap()
    private var currentInlay: Inlay<*>? = null
    private val zoneOffset = ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())

    init {
        this.initLayout()
        this.initEvent()
    }

    fun initLayout() {
        GridBagUtils.builder(this)
            .fill(GridBagUtils.GridBagFill.HORIZONTAL)
            .row {
                val panel = GridBagUtils.builder().fill(GridBagUtils.GridBagFill.HORIZONTAL)
                    .row { cit ->
                        cit.cell().weightX(1.0).add(currentTimestamp)
                            .cell().add(currentDatetime)
                            .cell().add(formatter)
                            .cell().add(timeunit)
                    }
                    .build()
                it.cell().weightX(1.0).add(panel)
            }
            .fill(GridBagUtils.GridBagFill.BOTH)
            .row { it.cell().weightY(1.0).add(area.withRightBar(this.copyHiltAction())) }
    }

    fun initEvent() {
        updateTimestamp()
        scheduleNextUpdate()

        ApplicationManager.getApplication().invokeLater {
            delayDocumentListener()
        }
        formatter.component.addItemListener {
            updateInlineDisplay()
            updateTimestamp()
        }
        timeunit.component.addChangeListener {
            if (timeunit.component.getSelected() == it) {
                updateTimestamp()
                updateInlineDisplay()
            }
        }
        this.formatter.component.isEditable = true
        this.formatter.component.editor = object : BasicComboBoxEditor() {
            override fun createEditorComponent(): JBTextField {
                return ExtendableTextField().apply {
                    border = null
                }
            }

            override fun setItem(anObject: Any?) {
                super.setItem(anObject)
                if (anObject is String) {
                    if (!formatterMap.containsKey(anObject)) {
                        try {
                            formatterMap[anObject] = DateTimeFormatter.ofPattern(anObject)
                            formatterModel.add(anObject)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Messages.showErrorDialog(project, e.message, "Format Error")
                        }
                    }
                }
            }
        }
    }

    fun delayDocumentListener() {
        if (area.editor == null) {
            ApplicationManager.getApplication().invokeLater {
                delayDocumentListener()
            }
            return
        }
        area.editor?.document?.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                updateInlineDisplay()
            }
        })

    }

    fun updateInlineDisplay() {
        val editorEx = area.editor ?: return
        val document = editorEx.document
        val text = document.text.trim()

        currentInlay?.let {
            it.dispose()
            currentInlay = null
        }

        if (text.isEmpty()) return
        val hilt = this.format(text)
        if (hilt.isNotEmpty()) {
            val offset = document.textLength
            currentInlay = editorEx.inlayModel.addInlineElement(offset, true, TimestampInlayRenderer(hilt))
        }
    }

    fun format(text: String): String {
        val formatter = formatterMap[formatterModel.selectedItem]
        val selected = timeunit.component.getSelected()
        return try {
            val l = text.toLong()
            val dateTime = if (selected == TimeUnit.MILLISECONDS) {
                LocalDateTime.ofEpochSecond(l / 1000, (l % 1000).toInt(), zoneOffset)
            } else {
                LocalDateTime.ofEpochSecond(l, 0, zoneOffset)
            }
            return dateTime.format(formatter)
        } catch (_: Exception) {
            try {
                val dateTime = LocalDateTime.parse(text, formatter!!)
                if (selected == TimeUnit.MILLISECONDS) {
                    dateTime.toInstant(zoneOffset).toEpochMilli().toString()
                } else {
                    dateTime.atZone(ZoneId.systemDefault()).toEpochSecond().toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return e.message ?: ""
            }
        }
    }

    fun updateTimestamp() {
        val now = LocalDateTime.now()
        val isMilliseconds = timeunit.component.getSelected() == TimeUnit.MILLISECONDS
        val timestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val displayTimestamp = if (isMilliseconds) timestamp else timestamp / 1000
        val datetime = now.format(formatterMap[formatterModel.selectedItem])
        ApplicationManager.getApplication().invokeLater {
            currentTimestamp.component.text = displayTimestamp.toString()
            currentDatetime.component.text = datetime
        }
    }

    fun scheduleNextUpdate() {
        if (alarm.isDisposed) return
        alarm.addRequest({
            if (alarm.isDisposed) return@addRequest
            updateTimestamp()
            scheduleNextUpdate()
        }, 1000, true)
    }

    override fun dispose() {
        currentInlay?.let {
            if (it.isValid) {
                it.dispose()
            }
        }
        currentInlay = null
    }

    private fun copyHiltAction(): AnAction {
        return object : AnAction("Copy", "Copy the converted text", AllIcons.Actions.Copy) {
            override fun actionPerformed(e: AnActionEvent) {
                val editorEx = area.editor ?: return
                val document = editorEx.document
                val text = document.text.trim()
                if (text.isEmpty()) return
                val hilt = format(text)
                if (hilt.isNotEmpty()) {
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(StringSelection(hilt), null)
                    Messages.showInfoMessage(project, "Copied to clipboard！", "Copy Success")
                }
            }
        }
    }

    class TimestampInlayRenderer(private val text: String) : EditorCustomElementRenderer {
        private val margin = 10

        override fun calcWidthInPixels(p0: Inlay<*>): Int {
            val editor = p0.editor
            val fontMetrics = editor.contentComponent.getFontMetrics(getFont(editor))
            return fontMetrics.stringWidth(text) + margin
        }

        override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
            val editor = inlay.editor
            val font = this.getFont(editor)
            val oldFont = g.font
            try {
                g.font = font
                val fontMetrics = g.fontMetrics
                val y = targetRegion.y + fontMetrics.ascent

                g.color = JBColor.GRAY
                g.drawString(text, targetRegion.x + margin, y)
            } finally {
                g.font = oldFont
            }
            super.paint(inlay, g, targetRegion, textAttributes)
        }

        fun getFont(editor: Editor): Font {
            val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
            return font.deriveFont(Font.ITALIC, font.size * 1f)
        }

    }

}