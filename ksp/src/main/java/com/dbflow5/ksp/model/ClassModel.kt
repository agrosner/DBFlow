package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.writer.FieldExtractor
import com.dbflow5.model.ClassModel
import com.dbflow5.model.FieldModel
import com.dbflow5.model.ReferenceHolderModel
import com.dbflow5.model.SingleFieldModel
import com.dbflow5.model.properties.GeneratedClassProperties
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

fun ClassModel.flattenedFields(referencesCache: ReferencesCache) =
    createFlattenedFields(referencesCache, fields)

fun ClassModel.primaryFlattenedFields(referencesCache: ReferencesCache) =
    createFlattenedFields(referencesCache, primaryFields)

/**
 * Returns true if element exists in DB declaration, or if it self-declares its DB.
 */
inline fun <reified C : ClassModel.ClassType> ClassModel.partOfDatabaseAsType(
    databaseTypeName: TypeName,
    declaredDBElements: List<ClassName>,
    /**
     * Used for generated class.
     */
    allDBElements: List<ClassName>,
) = this.type is C &&
    (properties.database == databaseTypeName || declaredDBElements.contains(this.classType)
        || properties.let { properties ->
        (properties is GeneratedClassProperties && allDBElements.contains(
            properties.generatedFromClassType
        ))
    })


fun ClassModel.extractors(referencesCache: ReferencesCache) = fields.map(
    mapExtractorsFromFields(
        referencesCache
    )
)

fun ClassModel.primaryExtractors(referencesCache: ReferencesCache) = primaryFields.map(
    mapExtractorsFromFields(referencesCache)
)

@Suppress("unchecked_cast")
fun ClassModel.referenceExtractors(referencesCache: ReferencesCache) = referenceFields.map(
    mapExtractorsFromFields(referencesCache)
) as List<FieldExtractor.ForeignFieldExtractor>

private fun ClassModel.mapExtractorsFromFields(referencesCache: ReferencesCache): (FieldModel) -> FieldExtractor =
    {
        when (it) {
            is ReferenceHolderModel -> it.toExtractor(this, referencesCache)
            is SingleFieldModel -> it.toExtractor(this)
        }
    }
