package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.SQLiteLookup
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.properties.TableProperties
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
) : TypeCreator<CreationQueryWriter.Input, PropertySpec> {
    override fun create(model: Input): PropertySpec {
        val (model, extractors) = model
        val isTemporary = when (model.properties) {
            is TableProperties -> model.properties.temporary
            else -> false
        }
        return PropertySpec.builder("creationQuery", String::class.asClassName())
            .apply {
                addModifiers(KModifier.OVERRIDE)
                getter(
                    FunSpec.getterBuilder()
                        .addCode("return %S", buildString {
                            append("CREATE${if (isTemporary) " TEMP" else ""} TABLE IF NOT EXISTS ${model.dbName}(")
                            append(extractors.joinToString {
                                it.createName(
                                    sqLiteLookup,
                                    typeConverterCache
                                )
                            })
                            val nonAutoFields = model.primaryFields
                                .filter { !(it.fieldType as FieldModel.FieldType.PrimaryAuto).isAutoIncrement }

                            if (nonAutoFields.isNotEmpty()) {
                                append(", PRIMARY KEY(")
                                append(nonAutoFields.joinToString { it.dbName.quoteIfNeeded() })
                                append(")")
                            }
                            append(")")
                        })
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