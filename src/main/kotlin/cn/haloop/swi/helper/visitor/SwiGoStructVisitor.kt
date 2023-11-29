package cn.haloop.swi.helper.visitor

import com.goide.psi.GoFieldDefinition
import com.goide.psi.GoRecursiveVisitor
import com.goide.psi.GoStructType
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author yangtuo
 */
class SwiGoStructVisitor : GoRecursiveVisitor() {

    private val structMetas = mutableListOf<StructMeta>()

    override fun visitStructType(o: GoStructType) {
        o.fieldDeclarationList.forEach { fieldDeclaration ->
            if (fieldDeclaration.fieldDefinitionList.isNotEmpty()) {
                // 正常字段
                fieldDeclaration.fieldDefinitionList.forEach { fieldDef ->
                    val structMeta = StructMeta()
                    structMeta.name = fieldDef.name.toString()
                    structMeta.type = fieldDeclaration.type?.text ?: "Unknown Type"
                    structMeta.title =
                        fieldDeclaration.tag?.getValue("desc") ?: fieldDeclaration.tag?.getValue("description") ?: ""
                    structMeta.desc = findFieldComment(fieldDef)
                    structMetas.add(structMeta)
                }
            } else {
                // 处理嵌入的结构体
                val embeddedStructType = fieldDeclaration.type ?: fieldDeclaration.anonymousFieldDefinition?.type
                val embeddedVisitor = SwiGoStructVisitor()
                embeddedStructType?.resolve(ResolveState.initial())?.accept(embeddedVisitor)
                structMetas.addAll(embeddedVisitor.structMetas)
            }
        }
    }

    private fun findFieldComment(fieldDef: GoFieldDefinition): String {
        var nextSibling: PsiElement? = PsiTreeUtil.nextLeaf(fieldDef)
        while (nextSibling != null) {
            if (nextSibling is PsiComment) {
                return nextSibling.text.trimStart('/').trim() // 提取注释文本
            }
            nextSibling = PsiTreeUtil.nextLeaf(nextSibling)
        }

        return ""
    }

    fun toList(): MutableList<MutableList<Any>> {
        return structMetas.map { it.toList() }.toMutableList()
    }
}

class StructMeta {
    var name: String = ""
    var type: String = ""
    var title: String = ""
    var desc: String = ""

    fun toList(): MutableList<Any> {
        return mutableListOf(name, type, title, desc)
    }
}