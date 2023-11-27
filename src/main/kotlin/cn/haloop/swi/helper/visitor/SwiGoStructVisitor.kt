package cn.haloop.swi.helper.visitor

import com.goide.psi.GoRecursiveVisitor
import com.goide.psi.GoStructType

/**
 * @author yangtuo
 */
class SwiGoStructVisitor : GoRecursiveVisitor() {

    private val structMetas = mutableListOf<StructMeta>()

    override fun visitStructType(o: GoStructType) {
        o.fieldDeclarationList.forEach {
            val structMeta = StructMeta()
            structMeta.name = it.fieldDefinitionList[0].name!!
            structMeta.type = it.type!!.text
            structMeta.title = it.fieldDefinitionList[0].name!!
            structMeta.desc = ""
            structMetas.add(structMeta)
        }
    }

    class StructMeta {
        var name: String = ""
        var type: String = ""
        var title: String = ""
        var desc: String = ""

        fun toArray(): Array<Any> {
            return arrayOf(name, type, title, desc)
        }
    }

    fun toArrays(): Array<Array<Any>> {
        return structMetas.map { it.toArray() }.toTypedArray()
    }
}