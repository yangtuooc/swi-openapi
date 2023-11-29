package cn.haloop.swi.helper.visitor

import com.fasterxml.jackson.annotation.JsonProperty

class ApiFoxSchema {
    private var type: String = ""
    private var title: String = ""
    private var required: Boolean = false
    private val properties: MutableMap<String, ApiFoxSchema> = HashMap()

    @get:JsonProperty("x-api-fox-orders")
    val xApiFoxOrders: List<String>
        get() {
            return properties.keys.toList()
        }

    @get:JsonProperty("required")
    val requiredList: List<String>
        get() {
            return properties.filter { it.value.required }.map { it.key }
        }

    fun getProperties(): Map<String, ApiFoxSchema> {
        return properties
    }

    fun getTitle(): String {
        return title
    }

    fun addProperty(fieldName: String, prop: ApiFoxSchema) {
        properties[fieldName] = prop
    }

    fun setType(type: String) {
        this.type = type
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setRequired(required: Boolean) {
        this.required = required
    }

    fun getType(): String {
        return type
    }
}