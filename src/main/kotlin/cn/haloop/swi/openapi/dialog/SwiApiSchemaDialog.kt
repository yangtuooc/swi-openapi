package cn.haloop.swi.openapi.dialog

import cn.haloop.swi.openapi.schema.SwiCompositeApiFoxSchema
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.json.JsonMapper
import com.intellij.json.JsonFileType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.EditorTextField
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author yangtuo
 */
class SwiApiSchemaDialog(
    private val project: Project,
    private val requestSchema: SwiCompositeApiFoxSchema?,
    private val responseSchema: SwiCompositeApiFoxSchema?
) : DialogWrapper(project) {

    private val om = JsonMapper.builder()
        .build()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

    init {
        init()
        title = "API Schema"
    }

    override fun createCenterPanel(): JComponent {
        val requestJsonStr = requestSchema?.let { om.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: "{}"
        val responseJsonStr = responseSchema?.let { om.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: "{}"

        val requestEditor = createEditor(requestJsonStr, "请求:")
        val responseEditor = createEditor(responseJsonStr, "响应:")

        val splitter = Splitter(true, 0.5f).apply {
            firstComponent = requestEditor
            secondComponent = responseEditor
        }

        return splitter
    }

    private fun createEditor(jsonString: String, title: String): JComponent {
        val document: Document = EditorFactory.getInstance().createDocument(jsonString)
        val editorTextField = EditorTextField(document, project, JsonFileType.INSTANCE, false, false).apply {
            setOneLineMode(false)
            addSettingsProvider { editor ->
                editor.setHorizontalScrollbarVisible(true)
                editor.setVerticalScrollbarVisible(true)
                editor.setCaretVisible(false)
            }
        }

        val label = JLabel(title)
        val panel = JPanel(BorderLayout()).apply {
            add(label, BorderLayout.NORTH)
            add(editorTextField, BorderLayout.CENTER)
        }
        return panel
    }
}