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
        return SwiCompositeApiFoxSchema().apply {
            setType("object")
            payload.body.forEach { addProperty(it.fieldName, resolveSchema(it)) }
        }
    }

    private fun resolveSchema(spec: GoTypeSpecMetadata): SwiApiFoxSchema {
        return if (spec.isReference) {
            resolveReferenceSchema(spec)
        } else {
            resolveNonReferenceSchema(spec)
        }
    }

    private fun resolveNonReferenceSchema(spec: GoTypeSpecMetadata): SwiApiFoxSchema {
        return SwiCompositeApiFoxSchema().apply {
            setType(GoTypeMapper.from(spec.fieldType).toApiFoxType())
            setTitle(spec.fieldDesc)
        }
    }

    private fun resolveReferenceSchema(spec: GoTypeSpecMetadata): SwiApiFoxSchema {
        return if (spec.isArray) {
            resolveArraySchema(spec)
        } else {
            resolveObjectSchema(spec)
        }
    }

    private fun resolveArraySchema(spec: GoTypeSpecMetadata): SwiApiFoxSchema {
        val itemsSchema = if (spec.references.all { it.isReference }) {
            resolveCompositeSchema(spec.references)
        } else {
            SwiCompositeApiFoxSchema().apply {
                setType(GoTypeMapper.from(spec.references.first().fieldType).toApiFoxType())
            }
        }
        return SwiMapApiFoxSchema().apply {
            setType("array")
            addProperty("items", itemsSchema)
        }
    }

    private fun resolveObjectSchema(spec: GoTypeSpecMetadata): SwiApiFoxSchema {
        val propertiesSchema = SwiMapApiFoxSchema().apply {
            spec.references.forEach { addProperty(it.fieldName, resolveSchema(it)) }
        }
        return SwiMapApiFoxSchema().apply {
            setType("object")
            addProperty("properties", propertiesSchema)
        }
    }

    private fun resolveCompositeSchema(references: List<GoTypeSpecMetadata>): SwiApiFoxSchema {
        return SwiCompositeApiFoxSchema().apply {
            setType("object")
            references.forEach {
                val propSchema = SwiCompositeApiFoxSchema().apply {
                    setType(GoTypeMapper.from(it.fieldType).toApiFoxType())
                    setTitle(it.fieldDesc)
                }
                addProperty(it.fieldName, propSchema)
            }
        }
    }
}

