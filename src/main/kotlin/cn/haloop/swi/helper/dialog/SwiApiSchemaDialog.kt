package cn.haloop.swi.helper.dialog

import cn.haloop.swi.helper.visitor.ApiFoxSchema
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.json.JsonMapper
import com.intellij.json.JsonFileType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author yangtuo
 */
class SwiApiSchemaDialog(private val project: Project, private val schema: ApiFoxSchema) : DialogWrapper(project) {

    private val om = JsonMapper.builder()
        .build()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

    init {
        init()
        title = "API Schema"
    }

    override fun createCenterPanel(): JComponent {
        val jsonStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(schema)
        val document: Document = EditorFactory.getInstance().createDocument(jsonStr)
        val editorTextField = EditorTextField(document, project, JsonFileType.INSTANCE, false, false).apply {
            setOneLineMode(false) // Disable one-line mode
            addSettingsProvider { editor ->
                editor.setHorizontalScrollbarVisible(true)
                editor.setVerticalScrollbarVisible(true)
                editor.setCaretVisible(false)
            }
        }

        val panel = JPanel(BorderLayout())
        panel.add(editorTextField, BorderLayout.CENTER)
        return panel
    }
}