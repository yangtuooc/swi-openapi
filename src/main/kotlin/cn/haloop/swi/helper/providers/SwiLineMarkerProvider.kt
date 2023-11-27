package cn.haloop.swi.helper.providers

import cn.haloop.swi.helper.icons.ApiIcon
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
                    element,
                    element.textRange,
                    ApiIcon.API_ICON,
                    { "Api Document" },
                    null,
                    GutterIconRenderer.Alignment.CENTER,
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
        // 直接使用 Kotlin 的字符串比较，不需要转换为大写
        return methodName in listOf("GET", "POST", "PUT", "DELETE")
    }
}
