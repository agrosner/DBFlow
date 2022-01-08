package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.SQLiteLookup
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.createFlattenedFields
import com.dbflow5.ksp.model.properties.TableProperties
import com.dbflow5.ksp.model.properties.dbName
import com.dbflow5.ksp.model.toExtractor
import com.dbflow5.ksp.writer.FieldExtractor
import com.dbflow5.ksp.writer.TypeCreator
import com.dbflow5.quoteIfNeeded
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
        val isTemporary = when (clsModel.properties) {
            is TableProperties -> clsModel.properties.temporary
            else -> false
        }
        return PropertySpec.builder("creationQuery", String::class.asClassName())
            .apply {
                addModifiers(KModifier.OVERRIDE)
                getter(
                    FunSpec.getterBuilder()
                        .apply {
                            if (clsModel.type is ClassModel.ClassType.Normal &&
                                (clsModel.type == ClassModel.ClassType.Normal.Fts3
                                    || clsModel.type is ClassModel.ClassType.Normal.Fts4)
                            ) {
                                addCode("return %S", buildString {
                                    append("CREATE VIRTUAL TABLE IF NOT EXISTS ${clsModel.dbName} USING ")
                                    when (clsModel.type) {
                                        ClassModel.ClassType.Normal.Fts3 -> append("FTS3")
                                        is ClassModel.ClassType.Normal.Fts4 -> append("FTS4")
                                        else -> append("")
                                    }
                                    append("(")
                                    append(extractors.joinToString {
                                        it.commaNames
                                    })
                                    if (clsModel.type is ClassModel.ClassType.Normal.Fts4) {
                                        if (extractors.isNotEmpty()) {
                                            append(",")
                                        }
                                        val classModel = referencesCache.allClasses
                                            .first { it.classType == clsModel.type.contentTable }
                                        append("content=${classModel.dbName}")
                                    }
                                    append(")")
                                })
                            } else if (
                                clsModel.type is ClassModel.ClassType.View
                            ) {
                                addStatement(
                                    "return %T.%L.query",
                                    clsModel.classType,
                                    clsModel.type.properties.name.shortName + if (
                                        !clsModel.type.properties.isProperty
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
                                            .filterNot { (it.fieldType as FieldModel.FieldType.PrimaryAuto).isAutoIncrement }
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