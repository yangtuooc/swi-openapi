package cn.haloop.swi.openapi.visitor

/**
 * @author yangtuo
 */
class SwiMapApiFoxSchema : HashMap<String, Any>(), SwiApiFoxSchema {


    override fun getType(): String {
        return (this["type"] ?: "") as String
    }

    override fun getTitle(): String {
        return (this["title"] ?: "") as String
    }

    override fun getRequired(): Boolean {
        return (this["required"] ?: false) as Boolean
    }

    override fun setType(type: String) {
        this["type"] = type
    }

    override fun setTitle(title: String) {
        this["title"] = title
    }

    override fun addProperty(field: String, properties: SwiApiFoxSchema) {
        this[field] = properties
    }

}