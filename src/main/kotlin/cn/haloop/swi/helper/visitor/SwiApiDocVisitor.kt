package cn.haloop.swi.helper.visitor

import com.goide.psi.GoRecursiveVisitor
import com.goide.psi.GoStructType
import com.goide.psi.impl.GoTypeUtil

/**
 * @author yangtuo
 */
class SwiApiDocVisitor : GoRecursiveVisitor() {

    private val schema: ApiFoxSchema = ApiFoxSchema()
    override fun visitStructType(o: GoStructType) {
        schema.setType("object")
        o.fieldDeclarationList.forEach {
            val property = ApiFoxSchema()
            val fieldName = it.fieldDefinitionList[0].name!!
            val type = it.type!!
            val context = it.context

            if (GoTypeUtil.isString(type, context)) {
                property.setType("string")
            } else if (GoTypeUtil.isIntType(type, context)) {
                property.setType("integer")
            } else if (GoTypeUtil.isInt64(type, context)) {
                property.setType("integer")
            } else if (GoTypeUtil.isBoolean(type, context)) {
                property.setType("boolean")
            } else if (GoTypeUtil.isNamedType(type)) {
                val visitor = SwiApiDocVisitor()
                type.contextlessResolve()?.accept(visitor)
                schema.addProperty(fieldName, visitor.apiFoxSchema())
                return@forEach
            }
            schema.addProperty(fieldName, property)
        }
    }


    fun apiFoxSchema(): ApiFoxSchema {
        return schema
    }
}

