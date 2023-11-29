package cn.haloop.swi.helper.resovler

import cn.haloop.swi.helper.visitor.SwiGoStructVisitor
import com.goide.psi.*
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author yangtuo
 */
class SwiResponsePayloadResolver {


    fun resolve(expr: GoCallExpr): SwiPayload {
        val controllerMethod =
            expr.argumentList.expressionList.lastOrNull()?.reference?.resolve() ?: return SwiPayload.empty()

        val callExprs = PsiTreeUtil.findChildrenOfType(controllerMethod, GoCallExpr::class.java)
        val responseContent = callExprs.filter { it.expression.text.contains("result.Success") }.map { it.argumentList }
            .map { it.expressionList }

        val swiPayload = SwiPayload.empty()
        if (responseContent.isNotEmpty()) {
            val resp = responseContent.first().first()
            swiPayload.appendToBody(resolveContent(resp))
        }

        return swiPayload
    }

    private fun resolveContent(bodyExpr: GoExpression): MutableList<GoTypeSpecMetadata> {
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

