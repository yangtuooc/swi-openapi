package cn.haloop.swi.helper.resovler

import com.goide.psi.GoCallExpr
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author yangtuo
 */
class SwiPayloadResolver {

    fun resolve(expr: GoCallExpr): SwiPayload {
        val controllerMethod =
            expr.argumentList.expressionList.lastOrNull()?.reference?.resolve() ?: return SwiPayload.empty()
        val callExprs = PsiTreeUtil.findChildrenOfType(controllerMethod, GoCallExpr::class.java)
        val query =
            callExprs.filter { it.expression.text.contains("Query") }.map { it.argumentList }.map { it.expressionList }
        val path =
            callExprs.filter { it.expression.text.contains("Param") }.map { it.argumentList }.map { it.expressionList }
        val body =
            callExprs.filter { it.expression.text.contains("Bind") }.map { it.argumentList }.map { it.expressionList }

        if (query.isEmpty() && path.isEmpty() && body.isEmpty()) {
            return SwiPayload.empty()
        }

        if (query.isNotEmpty()) {
            val queryList = query.first()
            queryList.forEach { queryExpr ->
                val queryName = queryExpr.text
                println("query-name: $queryName")
            }
        }

        if (path.isNotEmpty()) {
            val pathList = path.first()
            pathList.forEach { pathExpr ->
                val pathName = pathExpr.text
                println("path-name: $pathName")
            }
        }

        if (body.isNotEmpty()) {
            val bodyList = body.first()
            bodyList.forEach { bodyExpr ->
                val bodyName = bodyExpr.text
                println("body-name: $bodyName")
            }
        }

        // TODO: 提取请求参数
        return SwiPayload.empty()
    }
}

class SwiPayload {
    var query: Array<Array<Any>> = emptyArray()
    var path: Array<Array<Any>> = emptyArray()
    var body: Array<Array<Any>> = emptyArray()

    companion object {
        fun empty(): SwiPayload {
            return SwiPayload()
        }
    }
}