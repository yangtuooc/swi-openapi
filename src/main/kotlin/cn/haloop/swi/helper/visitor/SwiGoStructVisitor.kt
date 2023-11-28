package cn.haloop.swi.helper.visitor

import com.goide.psi.GoRecursiveVisitor
import com.goide.psi.GoStructType
import com.intellij.psi.ResolveState

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
                    structMeta.title = fieldDef.name.toString()
                    structMeta.desc = "" // 这里添加逻辑来提取字段描述
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


    class StructMeta {
        var name: String = ""
        var type: String = ""
        var title: String = ""
        var desc: String = ""

        fun toList(): MutableList<Any> {
            return mutableListOf(name, type, title, desc)
        }
    }

    fun toList(): MutableList<MutableList<Any>> {
        return structMetas.map { it.toList() }.toMutableList()
    }
}