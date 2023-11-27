package cn.haloop.swi.helper.nav

import cn.haloop.swi.helper.dialog.SwiApiDocDialog
import com.goide.psi.GoReferenceExpression
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import java.awt.event.MouseEvent

/**
 * @author yangtuo
 */
class SwiNavigationHandler : GutterIconNavigationHandler<GoReferenceExpression> {
    override fun navigate(e: MouseEvent?, elt: GoReferenceExpression?) {
        if (elt == null) {
            return
        }
        val project = elt.project
        val dialog = SwiApiDocDialog(project)
        dialog.show()
    }
}