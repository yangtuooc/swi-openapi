package cn.haloop.swi.openapi.provider

import cn.haloop.swi.openapi.icons.ApiIcon
import cn.haloop.swi.openapi.nav.SwiNavigationHandler
import com.goide.psi.GoCallExpr
import com.goide.psi.GoReferenceExpression
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement


/**
 * @author yangtuo
 */
class SwiLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element is GoReferenceExpression && element.parent is GoCallExpr) {
            val callExpr = element.parent as GoCallExpr
            if (isRouteMethodCall(callExpr)) {
                return LineMarkerInfo(
                    callExpr.expression.lastChild,
                    callExpr.expression.lastChild.textRange,
                    ApiIcon.API_ICON,
                    { "Api Document" },
                    { e, elt ->
                        SwiNavigationHandler().navigate(e, elt)
                    },
                    GutterIconRenderer.Alignment.LEFT,
                    { "Api Document" }
                )
            }
        }
        return null
    }

    private fun isRouteMethodCall(callExpr: GoCallExpr): Boolean {
        val referenceExpression = callExpr.expression
        if (referenceExpression is GoReferenceExpression) {
            val methodName = referenceExpression.lastChild.text
            return isHttpMethod(methodName)
        }
        return false
    }

    private fun isHttpMethod(methodName: String): Boolean {
        return methodName in listOf("GET", "POST", "PUT", "DELETE")
    }
}
