package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.IndexGroupModel
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.createFlattenedFields
import com.dbflow5.ksp.writer.TypeCreator
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec

/**
 * Description:
 */
class IndexPropertyWriter(
    private val referencesCache: ReferencesCache,

    ) : TypeCreator<IndexGroupModel, PropertySpec> {

    override fun create(model: IndexGroupModel) = PropertySpec
        .builder(
            "index_${model.name}", ClassNames.indexProperty(
                model.tableTypeName,
            )
        )
        .initializer(CodeBlock.builder()
            .apply {
                add(
                    "%M(%S, %L,",
                    MemberNames.indexProperty,
                    model.name,
                    model.unique
                )
                add("%L",
                    createFlattenedFields(
                        referencesCache,
                        model.fields
                    ).joinToString { it.propertyName }
                )
                add(")")
            }
            .build())
        .build()
}