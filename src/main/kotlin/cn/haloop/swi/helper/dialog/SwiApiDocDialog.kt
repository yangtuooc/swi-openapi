package cn.haloop.swi.helper.dialog

/**
 * @author yangtuo
 */
import cn.haloop.swi.helper.resovler.SwiPathResolver
import cn.haloop.swi.helper.visitor.SwiGoStructVisitor
import com.goide.psi.GoArgumentList
import com.goide.psi.GoCallExpr
import com.goide.psi.GoReferenceExpression
import com.goide.psi.GoUnaryExpr
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.*
import javax.swing.table.DefaultTableModel

class SwiApiDocDialog(private val elt: GoCallExpr) : DialogWrapper(elt.project) {
    private val tableModel = DefaultTableModel()
    private val exportOptions = listOf("ApiFox", "OpenAPI", "Swagger")
    private val pathResolver = SwiPathResolver()

    init {
        init() // 初始化对话框
        title = "API Documentation Preview" // 设置对话框的标题
    }

    override fun createCenterPanel(): JComponent {
        val panel = createPanel()

        panel.add(SwiApiPanel(pathResolver.resolve(elt)))

        panel.add(SwiHttpRequestTypePanel(elt.expression.lastChild.text))
        panel.add(JSeparator())

        val requestPanel = SwiRequestPanel(requestModel())
        panel.add(requestPanel.title())
        panel.add(requestPanel.table())

        val responsePanel = SwiResponsePanel(requestModel())
        panel.add(responsePanel.title())
        panel.add(responsePanel.table())

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


    private fun requestModel(): Array<Array<Any>> {

        val argumentList = PsiTreeUtil.findChildOfType(elt.parent, GoArgumentList::class.java)
        val controllerMethod = argumentList?.expressionList?.get(1)?.reference?.resolve()
        controllerMethod?.let {
            // 1. 定位函数调用
            val callExprs = PsiTreeUtil.findChildrenOfType(controllerMethod, GoCallExpr::class.java)
            val bindCall = callExprs.firstOrNull { it.text.contains("Bind") }

            //2. 提取变量
            val reqReference = when (val reqArgument = bindCall?.argumentList?.expressionList?.firstOrNull()) {
                is GoUnaryExpr -> PsiTreeUtil.findChildOfType(reqArgument, GoReferenceExpression::class.java)
                is GoReferenceExpression -> reqArgument
                else -> null
            }
            val visitor = SwiGoStructVisitor()
            reqReference?.getGoType(ResolveState.initial())?.contextlessResolve()?.accept(visitor)
            return visitor.toArrays()
        }
        return arrayOf()
    }


    private fun createPanel(): JPanel {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        return panel
    }
}
