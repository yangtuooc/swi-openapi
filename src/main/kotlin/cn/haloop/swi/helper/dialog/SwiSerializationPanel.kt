package cn.haloop.swi.helper.dialog

import com.intellij.openapi.ui.ComboBox
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author yangtuo
 */
class SwiSerializationPanel : JPanel(FlowLayout(FlowLayout.LEFT)) {

    init {
        add(JLabel("序列化类型:"))
        val box = ComboBox<String>().apply {
            addItem("JSON")
            addItem("XML")
            addItem("FORM")
        }
        box.selectedItem = "JSON"
        add(box)
    }
}