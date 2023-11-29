package cn.haloop.swi.openapi.schema

/**
 * @author yangtuo
 */
abstract class SwiBasicApiFoxSchema : SwiApiFoxSchema {
    private var type: String = ""
    private var title: String = ""
    private var required: Boolean = false

    override fun getType(): String {
        return type
    }

    override fun getTitle(): String {
        return title
    }

    override fun getRequired(): Boolean {
        return required
    }

    override fun setType(type: String) {
        this.type = type
    }

    override fun setTitle(title: String) {
        this.title = title
    }

    fun setRequired(required: Boolean) {
        this.required = required
    }
}