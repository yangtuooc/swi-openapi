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
            properties = SwiMapApiFoxSchema()
            properties.setType("array")
            var itemsProperties: SwiApiFoxSchema = SwiCompositeApiFoxSchema()
            if (spec.references.all { it.isReference }) {
                itemsProperties.setType("object")
                spec.references.forEach {
                    val props: SwiApiFoxSchema = SwiCompositeApiFoxSchema()
                    props.setType(GoTypeMapper.from(it.fieldType).toApiFoxType())
                    props.setTitle(it.fieldDesc)
                    itemsProperties.addProperty(it.fieldName, props)
                }
                properties.addProperty("items", itemsProperties)
                return properties
            }
            val metadata = spec.references.first()
            val props: SwiApiFoxSchema = SwiCompositeApiFoxSchema()
            props.setType(GoTypeMapper.from(metadata.fieldType).toApiFoxType())
            properties.addProperty("items", props)
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