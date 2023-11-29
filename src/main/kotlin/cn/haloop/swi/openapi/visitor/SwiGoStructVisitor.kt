package cn.haloop.swi.openapi.visitor

import cn.haloop.swi.openapi.resovler.GoTypeSpecMetadata
import com.goide.psi.*
import com.goide.psi.impl.GoTypeUtil
import com.intellij.psi.ResolveState
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
            processFieldDeclaration(fieldDeclaration)
        }
    }

    private fun processFieldDeclaration(fieldDeclaration: GoFieldDeclaration) {
        if (fieldDeclaration.fieldDefinitionList.isNotEmpty()) {
            fieldDeclaration.fieldDefinitionList.forEach { fieldDef ->
                processFieldDefinition(fieldDeclaration, fieldDef)
            }
        } else {
            // 处理嵌入的结构体
            processEmbeddedStruct(fieldDeclaration)
        }
    }

    private fun processFieldDefinition(fieldDeclaration: GoFieldDeclaration, fieldDef: GoFieldDefinition) {
        var jsonTag = fieldDeclaration.tag?.getValue("json")
        if (jsonTag?.contains("-") == true) return // 序列化时忽略该字段

        val metadata = createMetadata(fieldDeclaration, fieldDef, jsonTag)
        structMetas.add(metadata)

        val fieldType = fieldDeclaration.type
        if (!isPrimitiveType(fieldType)) {
            processComplexType(fieldType, metadata)
        }
    }

    private fun processComplexType(fieldType: GoType?, metadata: GoTypeSpecMetadata) {
        when {
            GoTypeUtil.isSlice(fieldType, fieldType?.context) -> {
                metadata.isReference = true
                metadata.isArray = true
                metadataStack.push(metadata)
                visitArrayOrSliceType(fieldType as GoArrayOrSliceType)
            }

            GoTypeUtil.isPointer(fieldType, fieldType?.context) -> {
                metadata.isReference = true
                metadataStack.push(metadata)
                visitPointerType(fieldType as GoPointerType)
            }

            else -> {
                fieldType?.contextlessResolve()?.let {
                    if (it is GoTypeSpec) {
                        visitResolvedTypeSpec(it, metadata)
                    }
                }
            }
        }
    }

    private fun visitResolvedTypeSpec(typeSpec: GoTypeSpec, metadata: GoTypeSpecMetadata) {
        val embeddedVisitor = SwiGoStructVisitor()
        typeSpec.accept(embeddedVisitor)
        metadata.references = embeddedVisitor.structMetas
    }

    private fun createMetadata(
        fieldDeclaration: GoFieldDeclaration,
        fieldDef: GoFieldDefinition,
        jsonTag: String?
    ): GoTypeSpecMetadata {
        val metadata = GoTypeSpecMetadata()
        jsonTag?.let {
            metadata.fieldName = it.replace(",omitempty", "")
        } ?: run {
            metadata.fieldName = fieldDef.name.toString()
        }
        metadata.fieldType = fieldDeclaration.type?.text ?: "Unknown Type"
        metadata.fieldTitle =
            fieldDeclaration.tag?.getValue("desc") ?: fieldDeclaration.tag?.getValue("description") ?: ""
        metadata.fieldDesc = findFieldComment(fieldDef)
        return metadata
    }

    private fun processEmbeddedStruct(fieldDeclaration: GoFieldDeclaration) {
        val embeddedStructType = fieldDeclaration.type ?: fieldDeclaration.anonymousFieldDefinition?.type
        embeddedStructType?.resolve(ResolveState.initial())?.let {
            val embeddedVisitor = SwiGoStructVisitor()
            it.accept(embeddedVisitor)
            structMetas.addAll(embeddedVisitor.structMetas)
        }
    }

    override fun visitPointerType(o: GoPointerType) {
        o.type?.contextlessResolve()?.let {
            if (it is GoTypeSpec) {
                visitResolvedTypeSpec(it, metadataStack.pop())
            }
        }
    }

    override fun visitArrayOrSliceType(o: GoArrayOrSliceType) {
        o.type.contextlessResolve()?.let {
            when (it) {
                is GoPointerType -> visitPointerType(it)
                is GoTypeSpec -> visitResolvedTypeSpec(it, metadataStack.pop())
            }
        }
    }

    private fun findFieldComment(fieldDef: GoFieldDefinition): String {
        val startOffset = fieldDef.textRange.startOffset
        val endOffset = fieldDef.textRange.endOffset

        // 获取包含字段定义的行的文本
        val fileText = fieldDef.containingFile.text
        val lineStart = fileText.lastIndexOf('\n', startOffset).coerceAtLeast(0)
        val lineEnd = fileText.indexOf('\n', endOffset).coerceAtMost(fileText.length)
        val lineText = fileText.substring(lineStart, lineEnd)

        // 查找注释标记并提取注释
        val commentStart = lineText.indexOf("//")
        if (commentStart != -1) {
            // 提取注释文本
            return lineText.substring(commentStart + 2).trim()
        }

        return ""
    }


    private fun isPrimitiveType(type: GoType?): Boolean {
        return type?.let {
            GoTypeUtil.isUintType(type, type.context) ||
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
        } ?: false
    }

    fun toList(): MutableList<MutableList<Any>> {
        return structMetas.map { it.toList() }.toMutableList()
    }

    fun structMetas(): MutableList<GoTypeSpecMetadata> {
        return structMetas
    }
}
