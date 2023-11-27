package cn.haloop.swi.helper.dialog

/**
 * @author yangtuo
 */
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import javax.swing.*
import javax.swing.table.DefaultTableModel

class SwiApiDocDialog(project: Project) : DialogWrapper(project) {
    private val apiField = JTextField()
    private val tableModel = DefaultTableModel()
    private val exportOptions = listOf("ApiFox", "OpenAPI", "Swagger")

    init {
        init() // 初始化对话框
        title = "API Documentation" // 设置对话框的标题
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        // API路由
        val apiPanel = JPanel()
        apiPanel.add(JLabel("接口路径:"))
        apiPanel.add(apiField.apply {
            text = "/api/agents/{id}"
        })
        panel.add(apiPanel, BoxLayout.X_AXIS)

        // 请求类型
        val requestTypePanel = JPanel()
        requestTypePanel.add(JLabel("请求类型:"))
        requestTypePanel.add(JComboBox<String>().apply {
            addItem("GET")
            addItem("POST")
            addItem("PUT")
            addItem("DELETE")
        })

        panel.add(requestTypePanel, BoxLayout.X_AXIS)

        // 模型字段
        val columnNames = arrayOf("Field", "Type", "Description")
        tableModel.setColumnIdentifiers(columnNames)
        // 假设有函数 generateModelData() 来填充表格数据
        val data = generateModelData()
        data.forEach { tableModel.addRow(it) }
        val table = JTable(tableModel)
        panel.add(JBScrollPane(table))

        // 导出选项
        val exportPanel = JPanel()
        exportOptions.forEach { option ->
            exportPanel.add(JButton(option).apply {
                addActionListener { exportAction(option) }
            })
        }
        panel.add(exportPanel)

        return panel
    }

    private fun exportAction(option: String) {
        // 处理不同的导出逻辑
        println("Exporting as $option")
    }

    private fun generateModelData(): Array<Array<Any>> {
        // 从解析代码或其他来源获取数据
        return arrayOf(
            arrayOf("name", "string", "name of agent"),
            arrayOf("gender", "enum", "gender")
        )
    }
}
