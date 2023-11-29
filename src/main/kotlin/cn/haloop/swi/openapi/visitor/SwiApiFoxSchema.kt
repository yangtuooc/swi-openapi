package cn.haloop.swi.openapi.visitor

/**
 * @author yangtuo
 */
interface SwiApiFoxSchema {
    fun getType(): String
    fun getTitle(): String
    fun getRequired(): Boolean
    fun setType(type: String)
    fun setTitle(title: String)
    fun addProperty(fieldName: String, prop: SwiApiFoxSchema)
}