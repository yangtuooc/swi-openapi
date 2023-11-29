package cn.haloop.swi.openapi.resovler

import com.goide.psi.GoArgumentList
import com.goide.psi.GoCallExpr
import com.goide.psi.GoFile
import com.goide.psi.GoVarDefinition
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author yangtuo
 */
class SwiPathResolver {

    fun resolve(expr: GoCallExpr): String? {
        val variableName = expr.expression.firstChild.text
        val file = expr.containingFile as GoFile
        val basePath = findBasePathInFile(file, variableName)

        var subPath = expr.parent?.let {
            PsiTreeUtil.findChildOfType(it, GoArgumentList::class.java)
                ?.expressionList?.firstOrNull()?.text?.trim('"')
        }
        if (subPath.isNullOrEmpty()) {
            return null
        }
        if (subPath.contains(Regex(":(\\w+)"))) {
            subPath = subPath.replace(Regex(":(\\w+)"), "{$1}")
        }

        return if (basePath.isNullOrEmpty()) subPath else "$basePath$subPath"
    }

    private fun findBasePathInFile(file: GoFile, variableName: String): String? {
        val varDefinitions = PsiTreeUtil.findChildrenOfType(file, GoVarDefinition::class.java)
        for (varDef in varDefinitions) {
            if (variableName.contains(varDef.text)) {
                val callExpr = PsiTreeUtil.findChildOfType(varDef.parent, GoCallExpr::class.java)
                if (callExpr?.text?.contains(".Group") == true) {
                    return Regex(".Group\\(\"(.*?)\"\\)").find(callExpr.text)?.groups?.get(1)?.value
                }
            }
        }
        return null
    }
}
