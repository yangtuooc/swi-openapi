package cn.haloop.swi.openapi.resovler

import cn.haloop.swi.openapi.dialog.GoTypeMapper
import cn.haloop.swi.openapi.schema.SwiApiFoxSchema
import cn.haloop.swi.openapi.schema.SwiCompositeApiFoxSchema
import cn.haloop.swi.openapi.schema.SwiMapApiFoxSchema
import cn.haloop.swi.openapi.schema.SwiPayload

/**
 * @author yangtuo
 */
class ApiFoxSchemaResolver {

    fun resolve(payload: SwiPayload): SwiCompositeApiFoxSchema {
        val schema = SwiCompositeApiFoxSchema()
        schema.setType("object")
        payload.body.forEach {
            schema.addProperty(it.fieldName, resolveSchema(it))
        }
        return schema
    }

    private fun resolveSchema(spec: GoTypeSpecMetadata): SwiApiFoxSchema {
        var properties: SwiApiFoxSchema = SwiCompositeApiFoxSchema()
        if (!spec.isReference) {
            properties.setType(GoTypeMapper.from(spec.fieldType).toApiFoxType())
            properties.setTitle(spec.fieldDesc)
            return properties
        }
        if (spec.isArray) {
            properties.setType("array")
            val itemsProperties = SwiCompositeApiFoxSchema()
            spec.references.forEach {
                val refSchema = resolveSchema(it)
                itemsProperties.addProperty(it.fieldName, refSchema)
            }
            properties.addProperty("items", itemsProperties)
            return properties
        }
        properties = SwiMapApiFoxSchema()
        properties.setType("object")
        val objProperties = SwiMapApiFoxSchema()
        spec.references.forEach {
            objProperties[it.fieldName] = resolveSchema(it)
        }
        properties.addProperty("properties", objProperties)
        return properties
    }
}