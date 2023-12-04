package cn.haloop.swi.openapi.resovler

import cn.haloop.swi.openapi.schema.SwiPayload
import cn.haloop.swi.openapi.visitor.SwiGoStructVisitor
import com.goide.psi.*
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author yangtuo
 */
class SwiPayloadResolver(private val expr: GoCallExpr) {

    private val callExprs = resolveCallExprs()
    fun resolveResponsePayload(): SwiPayload {
        val responseContent = callExprs.filter { it.expression.text.contains("result.Success") }.map { it.argumentList }
            .map { it.expressionList }

        val swiPayload = SwiPayload.empty()
        if (responseContent.isNotEmpty()) {
            val resp = responseContent.first().first()
            swiPayload.appendToBody(resolveType(resp))
        }

        return swiPayload
    }

    fun resolveRequestPayload(): SwiPayload {
        val query =
            callExprs.filter { it.expression.text.contains("Query") }.map { it.argumentList }.map { it.expressionList }
        val path =
            callExprs.filter { it.expression.text.contains("Param") }.map { it.argumentList }.map { it.expressionList }
        val body =
            callExprs.filter { it.expression.text.contains("BindJSON") || it.expression.text.contains("ShouldBind") }
                .map { it.argumentList }
                .map { it.expressionList }

        if (query.isEmpty() && path.isEmpty() && body.isEmpty()) {
            return SwiPayload.empty()
        }

        val swiPayload = SwiPayload.empty()

        if (query.isNotEmpty()) {
            val queryList = query.first()
            queryList.forEach { queryExpr ->
                val arrays = resolveQuery(queryExpr)
                swiPayload.query.addAll(arrays)
            }
        }

        if (path.isNotEmpty()) {
            val pathList = path.first()
            pathList.forEach { pathExpr ->
//                val arrays = resolvePath(pathExpr)
//                swiPayload.path.addAll(arrays)
                println(pathExpr.text)
            }
        }

        if (body.isNotEmpty()) {
            val bodyList = body.first()
            bodyList.forEach { bodyExpr ->
                val arrays = resolveType(bodyExpr)
                swiPayload.appendToBody(arrays)
            }
        }

        return swiPayload
    }


    private fun resolveCallExprs(): Collection<GoCallExpr> {
        val controllerMethod =
            expr.argumentList.expressionList.lastOrNull()?.reference?.resolve()
        return PsiTreeUtil.findChildrenOfType(controllerMethod, GoCallExpr::class.java)
    }

    private fun resolveQuery(queryExpr: GoExpression): MutableList<GoTypeSpecMetadata> {
        return when (queryExpr) {
            is GoUnaryExpr -> {
                val referenceExpression = PsiTreeUtil.findChildOfType(queryExpr, GoReferenceExpression::class.java)
                val visitor = SwiGoStructVisitor()
                val resolvedType = when (val goType = referenceExpression?.getGoType(ResolveState.initial())) {
                    is GoPointerType -> goType.type
                    else -> goType
                }
                resolvedType?.contextlessResolve()?.accept(visitor)
                return visitor.structMetas()
            }

            else -> mutableListOf()
        }
    }

    private fun resolveType(bodyExpr: GoExpression): MutableList<GoTypeSpecMetadata> {
        val referenceExpression = when (bodyExpr) {
            is GoUnaryExpr -> PsiTreeUtil.findChildOfType(bodyExpr, GoReferenceExpression::class.java)
            is GoReferenceExpression -> bodyExpr
            else -> null
        }
        val visitor = SwiGoStructVisitor()
        val resolvedType = when (val goType = referenceExpression?.getGoType(ResolveState.initial())) {
            is GoPointerType -> goType.type
            else -> goType
        }
        resolvedType?.contextlessResolve()?.accept(visitor)
        return visitor.structMetas()
    }
}

