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
            structMeta.name = it.fieldDefinitionList.firstOrNull()?.name.toString()
            structMeta.type = it.type!!.text
            structMeta.title = it.fieldDefinitionList.firstOrNull()?.name.toString()
            structMeta.desc = ""
            structMetas.add(structMeta)
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