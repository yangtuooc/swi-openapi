package cn.haloop.swi.helper.dialog

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

/**
 * @author yangtuo
 */
class SwiResponsePanel(data: Array<Array<Any>>) : JPanel() {

    private val tableModel = object : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }
    }

    private val columnNames = arrayOf("字段", "类型", "标题", "描述")

    private val titlePanel = JPanel(FlowLayout(FlowLayout.LEFT));
    private val tablePanel = JBScrollPane();

    init {
        titlePanel.add(JLabel("响应体:"))
        tableModel.setColumnIdentifiers(columnNames)
        data.forEach { tableModel.addRow(it) }

        val table = JBTable(tableModel)
        tablePanel.setViewportView(table)
    }

    fun title(): JPanel {
        return titlePanel
    }

    fun table(): JBScrollPane {
        return tablePanel
    }
}