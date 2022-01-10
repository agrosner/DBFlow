package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.model.ClassModel
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.primaryFlattenedFields
import com.dbflow5.ksp.writer.TypeCreator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec

/**
 * Description:
 */
class PrimaryConditionClauseWriter(
    private val referencesCache: ReferencesCache,
) : TypeCreator<ClassModel, FunSpec> {

    override fun create(model: ClassModel) =
        FunSpec.builder("getPrimaryConditionClause")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec("model", model.classType))
                addCode("return %T.clause().apply{\n", ClassNames.OperatorGroup)
                model.primaryFlattenedFields(referencesCache).forEach { field ->
                    addCode(
                        "and(%L.%L %L %N.%L)\n",
                        "Companion",
                        field.propertyName,
                        MemberNames.eq,
                        "model",
                        field.accessName(useLastNull = false),
                    )
                }
                addCode("}\n")
            }
            .build()
}