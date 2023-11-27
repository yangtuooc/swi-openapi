package cn.haloop.swi.helper.dialog

import com.intellij.openapi.ui.ComboBox
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author yangtuo
 */
class SwiHttpRequestTypePanel(type: String) : JPanel(FlowLayout(FlowLayout.LEFT)) {
    init {
        add(JLabel("请求类型:"))
        val box = ComboBox<String>().apply {
            addItem("GET")
            addItem("POST")
            addItem("PUT")
            addItem("DELETE")
        }
        box.selectedItem = type
        add(box)
    }
}