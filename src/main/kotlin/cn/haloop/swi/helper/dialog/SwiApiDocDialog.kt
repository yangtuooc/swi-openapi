package cn.haloop.swi.helper.dialog

/**
 * @author yangtuo
 */
import com.goide.psi.*
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import javax.swing.*
import javax.swing.table.DefaultTableModel

class SwiApiDocDialog(private val elt: GoReferenceExpression) : DialogWrapper(elt.project) {
    private val tableModel = DefaultTableModel()
    private val exportOptions = listOf("ApiFox", "OpenAPI", "Swagger")

    init {
        init() // 初始化对话框
        title = "API Documentation Preview" // 设置对话框的标题
    }

    override fun createCenterPanel(): JComponent {
        val panel = createPanel()

        panel.add(SwiApiPanel(findPath(elt)))

        panel.add(SwiHttpRequestTypePanel(elt.identifier.text))
        panel.add(JSeparator())

        val requestPanel = SwiRequestPanel(generateModelData())
        panel.add(requestPanel.title())
        panel.add(requestPanel.table())

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
        // 处理不同的导出逻辑
        Messages.showInfoMessage("导出选项：$option", "导出")
    }

    private fun generateModelData(): Array<Array<Any>> {
        // 从解析代码或其他来源获取数据
        return arrayOf(
            arrayOf("name", "string", "name of agent"),
            arrayOf("gender", "enum", "gender")
        )
    }


    private fun requestPanel(): JBScrollPane {
        val columnNames = arrayOf("字段", "类型", "标题", "描述")
        tableModel.setColumnIdentifiers(columnNames)
        // 假设有函数 generateModelData() 来填充表格数据
        val data = generateModelData()
        data.forEach { tableModel.addRow(it) }
        val table = JBTable(tableModel)
        return JBScrollPane(table)
    }


    private fun createPanel(): JPanel {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        return panel
    }

    private fun findPath(element: GoReferenceExpression): String? {
        val variableName = element.text
        val file = element.containingFile as GoFile
        val basePath = findBasePathInFile(file, variableName)

        val subPath = element.parent?.let {
            PsiTreeUtil.findChildOfType(it, GoArgumentList::class.java)
                ?.expressionList?.firstOrNull()?.text?.trim('"')
        }

        return if (basePath.isNullOrEmpty()) subPath else "$basePath$subPath"
    }

    private fun findBasePathInFile(file: GoFile, variableName: String): String? {
        val varDefinitions = PsiTreeUtil.findChildrenOfType(file, GoVarDefinition::class.java)
        for (varDef in varDefinitions) {
            if (variableName.contains(varDef.text)) {
                val callExpr = PsiTreeUtil.findChildOfType(varDef.parent, GoCallExpr::class.java)
                if (callExpr?.text?.contains(".Group") == true) {
                    return callExpr.argumentList.expressionList.firstOrNull()?.text?.trim('"')
                }
            }
        }
        return null
    }

}
