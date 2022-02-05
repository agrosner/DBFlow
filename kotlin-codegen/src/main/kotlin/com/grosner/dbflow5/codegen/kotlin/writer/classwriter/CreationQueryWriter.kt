package com.grosner.dbflow5.codegen.kotlin.writer.classwriter

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.SQLiteLookup
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.createFlattenedFields
import com.dbflow5.codegen.shared.properties.TableProperties
import com.dbflow5.codegen.shared.properties.dbName
import com.dbflow5.codegen.shared.references
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.quoteIfNeeded
import com.grosner.dbflow5.codegen.kotlin.writer.FieldExtractor
import com.grosner.dbflow5.codegen.kotlin.writer.toExtractor
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName

class CreationQueryWriter(
    private val sqLiteLookup: SQLiteLookup,
    private val typeConverterCache: TypeConverterCache,
    private val referencesCache: ReferencesCache,
) : TypeCreator<CreationQueryWriter.Input, PropertySpec> {
    override fun create(model: Input): PropertySpec {
        val (clsModel, extractors) = model
        val isTemporary = clsModel.properties.let { props ->
            when (props) {
                is TableProperties -> props.temporary
                else -> false
            }
        }
        return PropertySpec.builder("creationQuery", String::class.asClassName())
            .apply {
                addModifiers(KModifier.OVERRIDE)
                getter(
                    FunSpec.getterBuilder()
                        .apply {
                            clsModel.type.let { classType ->
                                if (classType is ClassModel.Type.Table &&
                                    (classType == ClassModel.Type.Table.Fts3
                                        || classType is ClassModel.Type.Table.Fts4)
                                ) {
                                    addCode("return %S", buildString {
                                        append("CREATE VIRTUAL TABLE IF NOT EXISTS ${clsModel.dbName} USING ")
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
                                } else if (
                                    classType is ClassModel.Type.View
                                ) {
                                    addStatement(
                                        "return %T.%L.query",
                                        clsModel.classType,
                                        classType.properties.name.shortName + if (
                                            !classType.properties.isProperty
                                        ) "()" else "",
                                    )
                                } else {
                                    addCode("return %S", buildString {
                                        append("CREATE${if (isTemporary) " TEMP" else ""} TABLE IF NOT EXISTS ${clsModel.dbName}(")
                                        append(extractors.joinToString {
                                            it.createName(
                                                sqLiteLookup,
                                                typeConverterCache
                                            )
                                        })
                                        if (clsModel.uniqueGroups.isNotEmpty()) {
                                            clsModel.uniqueGroups.forEach { group ->
                                                append(", UNIQUE(")
                                                append(createFlattenedFields(
                                                    referencesCache,
                                                    group.fields
                                                )
                                                    .joinToString { it.dbName })
                                                append(") ON CONFLICT ${group.conflictAction}")
                                            }
                                        }
                                        val nonAutoFields =
                                            clsModel.primaryFlattenedFields(referencesCache)
                                                .filterNot { (it.fieldType as FieldModel.FieldType.Primary).isAutoIncrement }
                                        if (nonAutoFields.isNotEmpty()) {
                                            val primaryKeyConflict =
                                                (clsModel.properties as TableProperties)
                                                    .primaryKeyConflict
                                            append(", PRIMARY KEY(")
                                            append(nonAutoFields.joinToString { it.dbName.quoteIfNeeded() })
                                            append(")")
                                            if (primaryKeyConflict != ConflictAction.NONE) {
                                                append(" ON CONFLICT ${primaryKeyConflict.dbName}")
                                            }
                                        }
                                        if (clsModel.referenceFields.isNotEmpty()) {
                                            clsModel.referenceFields
                                                .filter { referencesCache.isTable(it) }
                                                .forEach { field ->
                                                    val reference = referencesCache.resolve(field)
                                                    val extractor =
                                                        field.toExtractor(clsModel, referencesCache)
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
                        .build()
                )

            }
            .build()
    }

    data class Input(
        val model: ClassModel,
        val extractors: List<FieldExtractor>,
    )
}