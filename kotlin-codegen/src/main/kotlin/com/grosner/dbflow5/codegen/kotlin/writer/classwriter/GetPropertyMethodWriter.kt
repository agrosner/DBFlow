package com.grosner.dbflow5.codegen.kotlin.writer.classwriter

import com.dbflow5.codegen.shared.ClassNames
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.writer.TypeCreator
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
                else -> throw %T(%P) 
            """.trimIndent(),
                IllegalArgumentException::class.asClassName(),
                "Invalid column name passed \$columnName. Ensure you are calling the correct table's column"
            )
            endControlFlow()
        }
        .build()
}