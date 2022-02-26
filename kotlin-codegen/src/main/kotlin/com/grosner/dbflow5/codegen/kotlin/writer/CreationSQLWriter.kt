package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.SQLiteLookup
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.createFlattenedFields
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.properties.TableProperties
import com.dbflow5.codegen.shared.properties.dbName
import com.dbflow5.codegen.shared.references
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

/**
 * Description:
 */
class CreationSQLWriter(
    private val referencesCache: ReferencesCache,
    private val sqLiteLookup: SQLiteLookup,
    private val typeConverterCache: TypeConverterCache,
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<ClassModel, FunSpec> {
    override fun create(model: ClassModel): FunSpec {
        val extractors = model.extractors(referencesCache)
        val isTemporary = model.properties.let { props ->
            when (props) {
                is TableProperties -> props.temporary
                else -> false
            }
        }
        return FunSpec.builder(
            "${model.generatedFieldName}_creationSQL",
        )
            .returns(ClassNames.CompilableQuery)
            .addModifiers(KModifier.PRIVATE)
            .apply {
                model.originatingSource?.let { source ->
                    originatingFileTypeSpecAdder.addOriginatingFileType(this, source)
                }
            }
            .addCode(
                CodeBlock.builder()
                    .apply {
                        if (model.isView) {
                            add(
                                "return %T(%S)",
                                ClassNames.CompilableQuery,
                                (model.type as ClassModel.Type.View).properties.query
                            )
                        } else {
                            model.type.let { classType ->
                                if (classType is ClassModel.Type.Table &&
                                    (classType == ClassModel.Type.Table.Fts3
                                        || classType is ClassModel.Type.Table.Fts4)
                                ) {
                                    add("return %T(%S)", ClassNames.CompilableQuery, buildString {
                                        append("CREATE VIRTUAL TABLE IF NOT EXISTS ${model.dbName} USING ")
                                        when (classType) {
                                            ClassModel.Type.Table.Fts3 -> append("FTS3")
                                            is ClassModel.Type.Table.Fts4 -> append("FTS4")
                                            else -> append("")
                                        }
                                        append("(")
                                        append(extractors.joinToString {
                                            it.commaNames
                                        })
                                        if (classType is ClassModel.Type.Table.Fts4) {
                                            referencesCache.allClasses
                                                .firstOrNull { it.classType == classType.contentTable }
                                                ?.let { classModel ->
                                                    if (extractors.isNotEmpty()) {
                                                        append(",")
                                                    }
                                                    append("content=${classModel.dbName}")
                                                }
                                        }
                                        append(")")
                                    })
                                } else {
                                    add(
                                        "return %T(%S)",
                                        ClassNames.CompilableQuery,
                                        buildString {
                                            append("CREATE${if (isTemporary) " TEMP" else ""} TABLE IF NOT EXISTS ${model.dbName}(")
                                            append(extractors.joinToString {
                                                it.createName(
                                                    sqLiteLookup,
                                                    typeConverterCache
                                                )
                                            })
                                            if (model.uniqueGroups.isNotEmpty()) {
                                                model.uniqueGroups.forEach { group ->
                                                    append(", UNIQUE(")
                                                    append(
                                                        createFlattenedFields(
                                                            referencesCache,
                                                            group.fields
                                                        )
                                                            .joinToString { it.dbName })
                                                    append(") ON CONFLICT ${group.conflictAction}")
                                                }
                                            }
                                            val nonAutoFields =
                                                model.primaryFlattenedFields(referencesCache)
                                                    .filterNot { (it.fieldType as FieldModel.FieldType.Primary).isAutoIncrement }
                                            if (nonAutoFields.isNotEmpty()) {
                                                val primaryKeyConflict =
                                                    (model.properties as TableProperties)
                                                        .primaryKeyConflict
                                                append(", PRIMARY KEY(")
                                                append(nonAutoFields.joinToString { it.dbName.quoteIfNeeded() })
                                                append(")")
                                                if (primaryKeyConflict != ConflictAction.NONE) {
                                                    append(" ON CONFLICT ${primaryKeyConflict.dbName}")
                                                }
                                            }
                                            if (model.referenceFields.isNotEmpty()) {
                                                model.referenceFields
                                                    .filter { referencesCache.isTable(it) }
                                                    .forEach { field ->
                                                        val reference =
                                                            referencesCache.resolve(field)
                                                        val extractor =
                                                            field.toExtractor(
                                                                model,
                                                                referencesCache
                                                            )
                                                        val references = field.references(
                                                            referencesCache,
                                                        ).map { it.toExtractor(reference) }
                                                        append(", FOREIGN KEY(")
                                                        append(extractor.commaNames)
                                                        append(") REFERENCES ")
                                                        append(reference.dbName)
                                                        append("(")
                                                        append(references.joinToString { it.commaNames })
                                                        append(
                                                            ") ON UPDATE ${
                                                                field.referenceHolderProperties.onUpdate.dbName
                                                            }"
                                                        )
                                                        append(
                                                            " ON DELETE ${
                                                                field.referenceHolderProperties.onDelete.dbName
                                                            }"
                                                        )
                                                        if (field.referenceHolderProperties.deferred) {
                                                            append(" DEFERRABLE INITIALLY DEFERRED")
                                                        }
                                                    }
                                            }
                                            append(")")
                                        })
                                }
                            }
                        }
                    }
                    .build()
            )
            .build()
    }
}