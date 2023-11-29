package cn.haloop.swi.openapi.visitor

import cn.haloop.swi.openapi.resovler.GoTypeSpecMetadata
import com.goide.psi.*
import com.goide.psi.impl.GoTypeUtil
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author yangtuo
 */
class SwiGoStructVisitor : GoRecursiveVisitor() {

    private val structMetas = mutableListOf<GoTypeSpecMetadata>()

    override fun visitStructType(o: GoStructType) {
        o.fieldDeclarationList.forEach { fieldDeclaration ->
            if (fieldDeclaration.fieldDefinitionList.isNotEmpty()) {
                // 正常字段
                fieldDeclaration.fieldDefinitionList.forEach { fieldDef ->
                    var jsonTag = fieldDeclaration.tag?.getValue("json")
                    if (jsonTag?.contains("-") == true) {
                        // 序列化时忽略该字段
                        return@forEach
                    }

                    val structMeta = GoTypeSpecMetadata()
                    if (jsonTag?.isNotBlank() == true) {
                        // 去掉json tag中的omitempty
                        jsonTag = jsonTag.replace(",omitempty", "")
                    }
                    structMeta.fieldName = jsonTag ?: fieldDef.name.toString()
                    val fieldType = fieldDeclaration.type
                    structMeta.fieldType = fieldType?.text ?: "Unknown Type"
                    structMeta.fieldTitle =
                        fieldDeclaration.tag?.getValue("desc") ?: fieldDeclaration.tag?.getValue("description") ?: ""
                    structMeta.fieldDesc = findFieldComment(fieldDef)
                    structMetas.add(structMeta)
                    if (GoTypeUtil.isSlice(fieldType, fieldType?.context)) {
                        structMeta.isReference = true
                        visitArrayOrSliceType(fieldType as GoArrayOrSliceType)
                    }
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

    override fun visitArrayOrSliceType(o: GoArrayOrSliceType) {
        when (val goType = o.type) {
            is GoPointerType -> {
                val resolved = goType.type?.resolve(ResolveState())
                if (resolved is GoTypeSpec) {
                    val embeddedVisitor = SwiGoStructVisitor()
                    resolved.accept(embeddedVisitor)
                    embeddedVisitor.structMetas.forEach {
                        it.isReference = true
                        it.isArray = true
                    }
                    structMetas.addAll(embeddedVisitor.structMetas)
                }
            }

            is GoTypeSpec -> {
                val resolved = goType.resolve(ResolveState())
                if (resolved is GoStructType) {
                    val embeddedVisitor = SwiGoStructVisitor()
                    resolved.accept(embeddedVisitor)
                    embeddedVisitor.structMetas.forEach {
                        it.isReference = true
                        it.isArray = true
                    }
                    structMetas.addAll(embeddedVisitor.structMetas)
                }
            }

            else -> {}
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

    fun structMetas(): MutableList<GoTypeSpecMetadata> {
        return structMetas
    }
}


