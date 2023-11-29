package cn.haloop.swi.helper.resovler

import cn.haloop.swi.helper.visitor.GoTypeSpecMetadata
import cn.haloop.swi.helper.visitor.SwiGoStructVisitor
import com.goide.psi.*
import com.intellij.psi.ResolveState
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
                val arrays = resolveBody(bodyExpr)
                swiPayload.appendToBody(arrays)
            }
        }

        return swiPayload
    }

//    private fun resolvePath(pathExpr: GoExpression): MutableList<StructMeta> {
//        return mutableListOf(mutableListOf(pathExpr.text.trim('"'), "string", "", ""))
//    }

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

    private fun resolveBody(bodyExpr: GoExpression): MutableList<GoTypeSpecMetadata> {
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

class SwiPayload {
    var query: MutableList<GoTypeSpecMetadata> = mutableListOf()
    var path: MutableList<GoTypeSpecMetadata> = mutableListOf()
    var body: MutableList<GoTypeSpecMetadata> = mutableListOf()

    companion object {
        fun empty(): SwiPayload = SwiPayload()
    }

    fun appendToBody(element: MutableList<GoTypeSpecMetadata>) {
        body.addAll(element)
    }
}