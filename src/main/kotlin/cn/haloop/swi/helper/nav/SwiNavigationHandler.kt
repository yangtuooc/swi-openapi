package cn.haloop.swi.helper.nav

import cn.haloop.swi.helper.dialog.SwiApiSpecDialog
import com.goide.psi.GoCallExpr
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.awt.event.MouseEvent

/**
 * @author yangtuo
 */
class SwiNavigationHandler : GutterIconNavigationHandler<PsiElement> {
    override fun navigate(e: MouseEvent?, elt: PsiElement) {
        val callExpr = PsiTreeUtil.getParentOfType(elt, GoCallExpr::class.java) ?: return
        SwiApiSpecDialog(callExpr).show()
    }
}