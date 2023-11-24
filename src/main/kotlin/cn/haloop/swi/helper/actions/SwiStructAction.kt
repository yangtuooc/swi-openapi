package cn.haloop.swi.helper.actions

import cn.haloop.swi.helper.visitor.SwiApiDocVisitor
import com.fasterxml.jackson.annotation.JsonInclude
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

    init {
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        PsiDocumentManager.getInstance(project).getPsiFile(editor.document)?.let { file ->
            val visitor = SwiApiDocVisitor()
            file.accept(visitor)
            val schema = visitor.apiFoxSchema()
            val json = om.writeValueAsString(schema)
            Messages.showInfoMessage(json, "ApiFox Schema")
        }
    }
}

