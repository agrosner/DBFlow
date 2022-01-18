package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.model.ClassModel
import com.dbflow5.codegen.model.FieldModel
import com.dbflow5.codegen.model.ReferenceHolderModel
import com.dbflow5.codegen.model.SingleFieldModel
import com.dbflow5.codegen.model.cache.ReferencesCache


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
