package cn.haloop.swi.helper.actions

import cn.haloop.swi.helper.service.SwiConversionService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages

/**
 * @author yangtuo
 */
class SwiStructAction : AnAction() {

    private val svc: SwiConversionService = SwiConversionService()

    override fun actionPerformed(e: AnActionEvent) {
        val data = e.getData(CommonDataKeys.EDITOR)
        val selectedText = data?.selectionModel?.selectedText
        val convert = svc.convert(selectedText)
        Messages.showInfoMessage(convert, "转换结果")
    }
}