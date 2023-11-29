package cn.haloop.swi.openapi.visitor

import cn.haloop.swi.openapi.resovler.GoTypeSpecMetadata
import com.goide.psi.*
import com.goide.psi.impl.GoTypeUtil
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author yangtuo
 */
class SwiGoStructVisitor : GoRecursiveVisitor() {

    private val structMetas = mutableListOf<GoTypeSpecMetadata>()
    private val metadataStack = Stack<GoTypeSpecMetadata>()
    private val depth = AtomicInteger(0)

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

                    val metadata = GoTypeSpecMetadata()
                    if (jsonTag?.isNotBlank() == true) {
                        // 去掉json tag中的omitempty
                        jsonTag = jsonTag.replace(",omitempty", "")
                    }
                    metadata.fieldName = jsonTag ?: fieldDef.name.toString()
                    val fieldType = fieldDeclaration.type
                    metadata.fieldType = fieldType?.text ?: "Unknown Type"
                    metadata.fieldTitle =
                        fieldDeclaration.tag?.getValue("desc") ?: fieldDeclaration.tag?.getValue("description") ?: ""
                    metadata.fieldDesc = findFieldComment(fieldDef)
                    structMetas.add(metadata)
                    if (isPrimitiveType(fieldType)) {
                        return@forEach
                    }
                    if (GoTypeUtil.isSlice(fieldType, fieldType?.context)) {
                        metadata.isReference = true
                        metadata.isArray = true
                        metadataStack.push(metadata)
                        visitArrayOrSliceType(fieldType as GoArrayOrSliceType)
                    }
                    if (GoTypeUtil.isPointer(fieldType, fieldType?.context)) {
                        metadata.isReference = true
                        metadataStack.push(metadata)
                        visitPointerType(fieldType as GoPointerType)
                    }
                    val resolved = fieldType?.contextlessResolve()
                    if (resolved is GoTypeSpec) {
                        val embeddedVisitor = SwiGoStructVisitor()
                        resolved.accept(embeddedVisitor)
                        metadata.references = embeddedVisitor.structMetas
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

    override fun visitPointerType(o: GoPointerType) {
        val resolved = o.type?.contextlessResolve()
        if (resolved is GoTypeSpec) {
            val embeddedVisitor = SwiGoStructVisitor()
            resolved.accept(embeddedVisitor)
            metadataStack.pop().references = embeddedVisitor.structMetas
        }
    }

    override fun visitArrayOrSliceType(o: GoArrayOrSliceType) {
        when (val goType = o.type.contextlessResolve()) {
            is GoPointerType -> {
                visitPointerType(goType)
            }

            is GoTypeSpec -> {
                val embeddedVisitor = SwiGoStructVisitor()
                goType.accept(embeddedVisitor)
                metadataStack.pop().references = embeddedVisitor.structMetas
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

    private fun isPrimitiveType(type: GoType?): Boolean {
        type ?: return false
        return GoTypeUtil.isUintType(type, type.context) ||
                GoTypeUtil.isUint64(type, type.context) ||
                GoTypeUtil.isIntType(type, type.context) ||
                GoTypeUtil.isIntegerType(type, type.context) ||
                GoTypeUtil.isInt64(type, type.context) ||
                GoTypeUtil.isFloatType(type, type.context) ||
                GoTypeUtil.isFloat32(type, type.context) ||
                GoTypeUtil.isFloat64(type, type.context) ||
                GoTypeUtil.isComplexType(type, type.context) ||
                GoTypeUtil.isComplex64(type, type.context) ||
                GoTypeUtil.isComplex128(type, type.context) ||
                GoTypeUtil.isString(type, type.context) ||
                GoTypeUtil.isBoolean(type, type.context) ||
                GoTypeUtil.isByteType(type, type.context) ||
                GoTypeUtil.isRuneType(type, type.context)
    }


    fun toList(): MutableList<MutableList<Any>> {
        return structMetas.map { it.toList() }.toMutableList()
    }

    fun structMetas(): MutableList<GoTypeSpecMetadata> {
        return structMetas

    }
}

