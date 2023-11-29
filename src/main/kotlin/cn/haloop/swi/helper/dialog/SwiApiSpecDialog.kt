package cn.haloop.swi.helper.dialog

/**
 * @author yangtuo
 */
import cn.haloop.swi.helper.resovler.SwiPathResolver
import cn.haloop.swi.helper.resovler.SwiPayloadResolver
import cn.haloop.swi.helper.visitor.ApiFoxSchema
import com.goide.psi.GoCallExpr
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*

class SwiApiSpecDialog(private val elt: GoCallExpr) : DialogWrapper(elt.project) {
    private val exportOptions = listOf("ApiFox", "OpenAPI", "Swagger")
    private val pathResolver = SwiPathResolver()
    private val payloadResolver = SwiPayloadResolver(elt)

    init {
        init() // 初始化对话框
        title = "API Documentation Preview" // 设置对话框的标题
    }

    override fun createCenterPanel(): JComponent {
        val panel = createPanel()

        panel.add(SwiApiPanel(pathResolver.resolve(elt)))

        panel.add(SwiHttpRequestTypePanel(elt.expression.lastChild.text))
        panel.add(JSeparator())

        val requestPanel = SwiRequestPanel(payloadResolver.resolveRequestPayload())
        panel.add(requestPanel.title())
        panel.add(requestPanel.table())


        val responsePanel = SwiResponsePanel(payloadResolver.resolveResponsePayload())
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
            val requestPayload = payloadResolver.resolveRequestPayload()
            val responsePayload = payloadResolver.resolveResponsePayload()
            var requestSchema: ApiFoxSchema? = null
            var responseSchema: ApiFoxSchema? = null

            if (responsePayload.body.isNotEmpty()) {
                responseSchema = ApiFoxSchema()
                responseSchema.setType("object")
                responsePayload.body.forEach {
                    val properties = ApiFoxSchema()
                    if (!it.isReference) {
                        properties.setType(it.fieldType)
                        properties.setTitle(it.fieldDesc)
                        responseSchema.addProperty(it.fieldName, properties)
                    }
                }
            }
            if (requestPayload.body.isNotEmpty()) {
                requestSchema = ApiFoxSchema()
                requestSchema.setType("object")
                requestPayload.body.forEach {
                    val properties = ApiFoxSchema()
                    if (!it.isReference) {
                        properties.setType(it.fieldType)
                        properties.setTitle(it.fieldDesc)
                        requestSchema.addProperty(it.fieldName, properties)
                    }
                }
            }
            SwiApiSchemaDialog(elt.project, requestSchema, responseSchema).show()
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
