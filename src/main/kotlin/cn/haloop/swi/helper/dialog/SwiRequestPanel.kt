package cn.haloop.swi.helper.dialog

import cn.haloop.swi.helper.resovler.SwiPayload
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

/**
 * @author yangtuo
 */
class SwiRequestPanel(private val payload: SwiPayload) : JPanel() {

    private val tableModel = object : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }
    }

    private val columnNames = arrayOf("字段", "类型", "标题", "描述")

    private val titlePanel = JPanel(FlowLayout(FlowLayout.LEFT));
    private val tablePanel = JBScrollPane();

    init {
        if (payload.body.isNotEmpty()) {
            titlePanel.add(JLabel("requestBody:"))
            tableModel.setColumnIdentifiers(columnNames)
            payload.body.forEach {
                tableModel.addRow(it.toList().toTypedArray())
            }
        }

        if (payload.query.isNotEmpty()) {
            titlePanel.add(JLabel("queryString:"))
            tableModel.setColumnIdentifiers(columnNames)
            payload.query.forEach { tableModel.addRow(it.toList().toTypedArray()) }
        }

        if (payload.path.isNotEmpty()) {
            titlePanel.add(JLabel("pathVariable:"))
            tableModel.setColumnIdentifiers(columnNames)
            payload.path.forEach { tableModel.addRow(it.toList().toTypedArray()) }
        }

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