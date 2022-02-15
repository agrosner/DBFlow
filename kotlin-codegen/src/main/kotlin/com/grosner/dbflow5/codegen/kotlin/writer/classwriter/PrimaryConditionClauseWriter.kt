package com.grosner.dbflow5.codegen.kotlin.writer.classwriter

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
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
                addCode(
                    "return %T.clause().chain(%T.And, \n",
                    ClassNames.OperatorGroup,
                    ClassNames.Operation
                )
                model.primaryFlattenedFields(referencesCache).forEach { field ->
                    addCode(
                        "%L.%L %L %N.%L,\n",
                        "Companion",
                        field.propertyName,
                        MemberNames.eq,
                        "model",
                        field.accessName(useLastNull = false),
                    )
                }
                addCode(")\n")
            }
            .build()
}