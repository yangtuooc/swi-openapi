package cn.haloop.swi.helper.dialog

import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @author yangtuo
 */
class SwiApiPanel(path: String?) : JPanel(FlowLayout(FlowLayout.LEFT)) {

    init {
        add(JLabel("接口路径:"))
        add(JTextField(path).apply {
            isEditable = true
            preferredSize = preferredSize.apply { width = 500 }
        })
    }
}