package cn.haloop.swi.helper.resovler

class GoTypeSpecMetadata {
    var fieldName: String = ""
    var fieldType: String = ""
    var fieldTitle: String = ""
    var fieldDesc: String = ""
    var isReference: Boolean = false
    var isArray: Boolean = false
    var isRequired: Boolean = false
    var isEnum: Boolean = false
    var enumMetadata: EnumMetadata? = null

    fun toList(): MutableList<Any> {
        if (isReference) {
            return mutableListOf("$$fieldName", fieldType, fieldTitle, fieldDesc)
        }
        return mutableListOf(fieldName, fieldType, fieldTitle, fieldDesc)
    }
}

class EnumMetadata {
    private var enumName: String = ""
    private var enumDesc: String = ""
    private var enumValue: String = ""

    fun toList(): MutableList<Any> {
        return mutableListOf(enumName, enumDesc, enumValue)
    }
}

