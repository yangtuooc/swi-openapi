package cn.haloop.swi.helper.dialog

import cn.haloop.swi.helper.visitor.ApiFoxSchema
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.json.JsonMapper
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * @author yangtuo
 */
class SwiApiSchemaDialog(project: Project, private val schema: ApiFoxSchema) : DialogWrapper(project) {

    private val om = JsonMapper.builder()
        .build()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

    init {
        init()
        title = "API Schema"
    }

    override fun createCenterPanel(): JComponent {
        val jsonStr = om.writeValueAsString(schema)
        val textArea = JTextArea(jsonStr)
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.wrapStyleWord = true

        val scrollPane = JBScrollPane(textArea)
        scrollPane.preferredSize = Dimension(700, 400)

        val panel = JPanel(BorderLayout())
        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }
}