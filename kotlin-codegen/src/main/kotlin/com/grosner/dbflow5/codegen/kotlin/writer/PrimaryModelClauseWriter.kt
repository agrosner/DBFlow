package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

/**
 * Writes the value operators for each key to model value used
 * in a lookup query.
 */
class PrimaryModelClauseWriter(
    private val referencesCache: ReferencesCache,
) : TypeCreator<ClassModel, PropertySpec> {
    override fun create(model: ClassModel): PropertySpec =
        PropertySpec.builder(
            "${model.generatedFieldName}_primaryModelClauseGetter",
            ClassNames.primaryModelClauseGetter(model.classType),
            KModifier.PRIVATE,
        )
            .initializer(
                CodeBlock.builder()
                    .beginControlFlow("%T", ClassNames.primaryModelClauseGetter(model.classType))
                    .add("model ->")
                    .apply {
                        addStatement(
                            "listOf(",
                        )
                        model.primaryFlattenedFields(referencesCache).forEach { field ->
                            add(
                                "%L.%L %L %N.%L,\n",
                                model.generatedClassName.className,
                                field.propertyName,
                                MemberNames.eq,
                                "model",
                                field.accessName(useLastNull = false),
                            )
                        }
                        add(")\n")
                    }
                    .endControlFlow()
                    .build()
            )
            .build()
}