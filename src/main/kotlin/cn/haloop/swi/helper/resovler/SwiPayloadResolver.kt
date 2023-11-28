package cn.haloop.swi.helper.resovler

import com.goide.psi.GoCallExpr
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author yangtuo
 */
class SwiPayloadResolver {

    fun resolve(expr: GoCallExpr): SwiPayload {
        val controllerMethod = expr.argumentList.expressionList.firstOrNull() ?: return SwiPayload.empty()
        val callExprs = PsiTreeUtil.findChildrenOfType(controllerMethod, GoCallExpr::class.java)
        val query = callExprs.filter { it.expression.text == "Query" }.map { it.argumentList }.map { it.expressionList }
        val path = callExprs.filter { it.expression.text == "Param" }.map { it.argumentList }.map { it.expressionList }
        val body = callExprs.filter { it.expression.text == "Bind" }.map { it.argumentList }.map { it.expressionList }

        // TODO: 提取请求参数
        return SwiPayload.empty()
    }
}

class SwiPayload {
    var query: Array<Array<Any>>? = null
    var path: Array<Array<Any>>? = null
    var body: Array<Array<Any>>? = null

    companion object {
        fun empty(): SwiPayload {
            return SwiPayload()
        }
    }
}