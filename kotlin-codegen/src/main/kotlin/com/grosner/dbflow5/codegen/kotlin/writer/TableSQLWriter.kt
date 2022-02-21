package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.properties.TableProperties
import com.dbflow5.codegen.shared.properties.dbName
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

/**
 * Writes the associated queries for a [Table]
 */
class TableSQLWriter(
    private val referencesCache: ReferencesCache,
) : TypeCreator<ClassModel, PropertySpec> {

    override fun create(model: ClassModel): PropertySpec {
        val extractors = model.extractors(referencesCache)
        val primaryExtractors = model.primaryExtractors(referencesCache)
        return PropertySpec
            .builder(
                "${model.generatedFieldName}_sql", ClassNames.TableSQL,
                KModifier.PRIVATE
            )
            .initializer(
                CodeBlock.builder()
                    .addStatement("%T(", ClassNames.TableSQL)
                    .addStatement(
                        "insert = %T(%S),", ClassNames.CompilableQuery,
                        insertStatementQuery(model = model, extractors = extractors, isSave = false)
                    )
                    .addStatement(
                        "update = %T(%S),", ClassNames.CompilableQuery,
                        updateStatementQuery(
                            model = model,
                            extractors = extractors,
                            primaryExtractors = primaryExtractors
                        )
                    )
                    .addStatement(
                        "delete = %T(%S),", ClassNames.CompilableQuery,
                        deleteStatementQuery(model = model, primaryExtractors = primaryExtractors)
                    )
                    .addStatement(
                        "save = %T(%S),", ClassNames.CompilableQuery,
                        insertStatementQuery(model, extractors, isSave = true)
                    )
                    .addStatement(")")
                    .build()
            )
            .build()
    }

    private fun insertStatementQuery(
        model: ClassModel,
        extractors: List<FieldExtractor>,
        isSave: Boolean
    ) = buildString {
        val joinToString = extractors.joinToString {
            it.commaNames
        }
        val insertConflict = (model.properties
            as? TableProperties)?.insertConflict ?: ConflictAction.NONE
        append(
            "INSERT ${
                when {
                    isSave -> "OR ${ConflictAction.REPLACE.dbName}"
                    insertConflict !== ConflictAction.NONE -> {
                        insertConflict.dbName
                    }
                    else -> ""
                }
            } INTO ${model.dbName}("
        )
        append(joinToString)
        append(") VALUES (${extractors.joinToString { it.valuesName }})")
    }

    private fun updateStatementQuery(
        model: ClassModel,
        extractors: List<FieldExtractor>,
        primaryExtractors: List<FieldExtractor>,
    ) = buildString {
        append("UPDATE ")
        (model.properties as? TableProperties)?.updateConflict
            ?: ConflictAction.NONE
                .takeIf { it != ConflictAction.NONE }?.let { action ->
                    append(" OR ${action.dbName}")
                }
        append("${model.dbName} SET ")
        append(extractors.joinToString { it.updateName })
        append(" WHERE ")
        append(primaryExtractors.joinToString(separator = " AND ") { it.updateName })
    }

    private fun deleteStatementQuery(
        model: ClassModel,
        primaryExtractors: List<FieldExtractor>,
    ) = buildString {
        append("DELETE FROM ${model.dbName} WHERE ")
        append(primaryExtractors.joinToString(" AND ") { it.updateName })
    }
}