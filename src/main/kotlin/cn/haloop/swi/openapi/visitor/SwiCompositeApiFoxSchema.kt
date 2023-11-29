package cn.haloop.swi.openapi.visitor

import com.fasterxml.jackson.annotation.JsonProperty

class SwiCompositeApiFoxSchema : SwiBasicApiFoxSchema() {
    private val properties: MutableMap<String, SwiApiFoxSchema> = HashMap()

    @get:JsonProperty("x-api-fox-orders")
    val xApiFoxOrders: List<String>
        get() {
            return properties.keys.toList()
        }

    @get:JsonProperty("required")
    val requiredList: List<String>
        get() {
            return properties.filter { it.value.getRequired() }.map { it.key }
        }


    fun getProperties(): Map<String, SwiApiFoxSchema> {
        return properties
    }


    override fun addProperty(fieldName: String, prop: SwiApiFoxSchema) {
        properties[fieldName] = prop
    }


}