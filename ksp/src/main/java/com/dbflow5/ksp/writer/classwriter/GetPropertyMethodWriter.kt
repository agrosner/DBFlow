package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.codegen.model.ClassModel
import com.dbflow5.codegen.model.cache.ReferencesCache
import com.dbflow5.codegen.writer.TypeCreator
import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.*

/**
 * Description:
 */
class GetPropertyMethodWriter(
    private val referencesCache: ReferencesCache,
) : TypeCreator<ClassModel, FunSpec> {

    override fun create(model: ClassModel) = FunSpec.builder("getProperty")
        .apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter(ParameterSpec("columnName", String::class.asClassName()))
            returns(
                ClassNames.property(
                    WildcardTypeName.producerOf(
                        Any::class.asTypeName().copy(nullable = true)
                    )
                )
            )
            beginControlFlow(
                "return when(%N.%M())",
                "columnName",
                MemberNames.quoteIfNeeded
            )
            model.flattenedFields(referencesCache).forEach { field ->
                addCode(
                    """
                    %S -> %L.%L
                    
                """.trimIndent(),
                    field.dbName.quoteIfNeeded(),
                    "Companion",
                    field.propertyName,
                )
            }
            addCode(
                """
                else -> throw %T(%S) 
            """.trimIndent(),
                IllegalArgumentException::class.asClassName(),
                "Invalid column name passed. Ensure you are calling the correct table's column"
            )
            endControlFlow()
        }
        .build()
}