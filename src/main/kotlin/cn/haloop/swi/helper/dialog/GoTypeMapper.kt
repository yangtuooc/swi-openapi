package cn.haloop.swi.helper.dialog

/**
 * @author yangtuo
 */
enum class GoTypeMapper(private val type: String) {

    STRING("string"),
    INT("int"),
    INT32("int32"),
    INT64("int64"),
    FLOAT32("float32"),
    FLOAT64("float64"),
    BOOL("bool"),
    BYTE("byte"),
    UNKNOWN("unknown");

    companion object {
        fun from(type: String): GoTypeMapper {
            return when (type) {
                "string" -> STRING
                "int" -> INT
                "int32" -> INT32
                "int64" -> INT64
                "float32" -> FLOAT32
                "float64" -> FLOAT64
                "bool" -> BOOL
                else -> UNKNOWN
            }
        }
    }

    fun toApiFoxType(): String {
        return when (this) {
            STRING -> "string"
            INT -> "integer"
            INT32 -> "integer"
            INT64 -> "integer"
            FLOAT32 -> "number"
            FLOAT64 -> "number"
            BOOL -> "boolean"
            BYTE -> "string"
            UNKNOWN -> "unknown"
        }
    }
}