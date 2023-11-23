package cn.haloop.swi.helper.actions

import cn.haloop.swi.helper.visitor.SwiOpenApiVisitor
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager

/**
 * @author yangtuo
 */
class SwiStructAction : AnAction() {

    private val om: ObjectMapper = ObjectMapper()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return

        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
        val visitor = SwiOpenApiVisitor(editor)
        file.accept(visitor)

        val openApiDoc = visitor.apifoxModel()
        Messages.showInfoMessage(om.writeValueAsString(openApiDoc), "OpenApiDoc")
    }
}

