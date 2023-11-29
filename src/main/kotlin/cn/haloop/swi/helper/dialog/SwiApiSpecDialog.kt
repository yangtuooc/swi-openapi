package cn.haloop.swi.helper.dialog

/**
 * @author yangtuo
 */
import cn.haloop.swi.helper.resovler.SwiPathResolver
import cn.haloop.swi.helper.resovler.SwiRequestPayloadResolver
import cn.haloop.swi.helper.resovler.SwiResponsePayloadResolver
import cn.haloop.swi.helper.visitor.ApiFoxSchema
import com.goide.psi.GoCallExpr
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*

class SwiApiSpecDialog(private val elt: GoCallExpr) : DialogWrapper(elt.project) {
    private val exportOptions = listOf("ApiFox", "OpenAPI", "Swagger")
    private val pathResolver = SwiPathResolver()
    private val requestPayloadResolver = SwiRequestPayloadResolver()
    private val responsePayloadResolver = SwiResponsePayloadResolver()

    init {
        init() // 初始化对话框
        title = "API Documentation Preview" // 设置对话框的标题
    }

    override fun createCenterPanel(): JComponent {
        val panel = createPanel()

        panel.add(SwiApiPanel(pathResolver.resolve(elt)))

        panel.add(SwiHttpRequestTypePanel(elt.expression.lastChild.text))
        panel.add(JSeparator())

        val requestPanel = SwiRequestPanel(requestPayloadResolver.resolve(elt))
        panel.add(requestPanel.title())
        panel.add(requestPanel.table())


        val responsePanel = SwiResponsePanel(responsePayloadResolver.resolve(elt))
        panel.add(responsePanel.title())
        panel.add(responsePanel.table())

        panel.add(JSeparator())
        panel.add(SwiSerializationPanel())

        // 导出选项
        val exportPanel = JPanel()
        exportOptions.forEach { option ->
            exportPanel.add(JButton(option).apply {
                addActionListener { exportAction(option) }
            })
        }
        panel.add(exportPanel)
        panel.add(JSeparator())

        return panel
    }

    private fun exportAction(option: String) {
        if ("ApiFox" == option) {
            val requestPayload = requestPayloadResolver.resolve(elt)
            val body = requestPayload.body
            if (body.isNotEmpty()) {
                val schema = ApiFoxSchema()
                schema.setType("object")
                body.forEach {
                    val properties = ApiFoxSchema()
                    if (!it.isReference) {
                        properties.setType(GoTypeMapper.from(it.fieldType).toApiFoxType())
                        properties.setTitle(it.fieldDesc)
                        schema.addProperty(it.fieldName, properties)
                    }
                }
                SwiApiSchemaDialog(elt.project, schema).show()
            }
        }
    }


    private fun createPanel(): JPanel {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        return panel
    }
}
