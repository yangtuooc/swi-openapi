package cn.haloop.swi.helper.visitor

import com.goide.psi.GoRecursiveVisitor
import com.goide.psi.GoStructType
import com.goide.psi.impl.GoTypeUtil
import com.intellij.openapi.editor.Editor
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

/**
 * @author yangtuo
 */
class SwiOpenApiVisitor(editor: Editor) : GoRecursiveVisitor() {

    private val model: ApiFoxModel = ApiFoxModel()
    private val startOffset = editor.selectionModel.selectionStart
    private val endOffset = editor.selectionModel.selectionEnd

    override fun visitStructType(o: GoStructType) {
        (!isInSelectionRange(o.startOffset, o.endOffset)).let {
            super.visitStructType(o)
        }
        o.fieldDeclarationList.forEach {
            val property = Property()
            val name = it.fieldDefinitionList[0].name!!
            val type = it.type!!
            val context = it.context

            if (GoTypeUtil.isString(type, context)) {
                property.type("string")
            } else if (GoTypeUtil.isIntType(type, context)) {
                property.type("integer")
            } else if (GoTypeUtil.isInt64(type, context)) {
                property.type("integer")
            } else if (GoTypeUtil.isBoolean(type, context)) {
                property.type("boolean")
            } else if (GoTypeUtil.isNamedType(type)) {
                // TODO 解析自定义类型
            }
            model.addProperty(name, property)
        }
    }


    private fun isInSelectionRange(start: Int, end: Int): Boolean {
        return this.startOffset <= start && end <= this.endOffset
    }

    fun apifoxModel(): ApiFoxModel {
        return model
    }
}

class ApiFoxModel : HashMap<String, Any>() {

    private val properties: Property = Property()

    init {
        this["type"] = "object"
        this["properties"] = properties
    }

    fun addProperty(name: String, property: Property) {
        properties[name] = property
    }
}

class Property : HashMap<String, Any>() {

    fun type(type: String): Property {
        this["type"] = type
        return this
    }

    fun title(title: String): Property {
        this["title"] = title
        return this
    }
}