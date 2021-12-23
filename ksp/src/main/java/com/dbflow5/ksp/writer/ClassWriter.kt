package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.kotlinpoet.ParameterPropertySpec
import com.dbflow5.ksp.model.*
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.writer.classwriter.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*

/**
 * Description:
 */
class ClassWriter(
    private val fieldPropertyWriter: FieldPropertyWriter,
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
    private val loadFromCursorWriter: LoadFromCursorWriter,
    private val getPropertyMethodWriter: GetPropertyMethodWriter,
    private val allColumnPropertiesWriter: AllColumnPropertiesWriter,
    private val primaryConditionClauseWriter: PrimaryConditionClauseWriter,
    private val statementBinderWriter: StatementBinderWriter,
    private val typeConverterFieldWriter: TypeConverterFieldWriter,
) : TypeCreator<ClassModel, FileSpec> {
    override fun create(model: ClassModel): FileSpec {
        val tableParam = ParameterPropertySpec(
            name = "table",
            type = Class::class.asClassName()
                .parameterizedBy(model.classType),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%T::class.java", model.classType)
        }
        val tableNameParam = ParameterPropertySpec(
            name = "name",
            type = String::class.asClassName(),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%S", model.dbName)
        }
        val extractors = model.fields.map {
            when (it) {
                is ReferenceHolderModel -> FieldExtractor.ForeignFieldExtractor(
                    it,
                    referencesCache,
                )
                is SingleFieldModel -> FieldExtractor.SingleFieldExtractor(it)
            }
        }
        val primaryExtractors = model.primaryFields.map {
            when (it) {
                is ReferenceHolderModel -> FieldExtractor.ForeignFieldExtractor(
                    it,
                    referencesCache,
                )
                is SingleFieldModel -> FieldExtractor.SingleFieldExtractor(it)
            }
        }
        val superClass = when (model.type) {
            ClassModel.ClassType.Normal -> ClassNames.modelAdapter(model.classType)
            ClassModel.ClassType.View -> ClassNames.modelViewAdapter(model.classType)
            ClassModel.ClassType.Query -> ClassNames.retrievalAdapter(model.classType)
        }

        val typeConverters = model.flattenedFields(referencesCache)
            .filter { it.hasTypeConverter(typeConverterCache) }
            .associate {
                val typeConverter = it.typeConverter(typeConverterCache)
                typeConverter.name.shortName.lowercase(
                    Locale.getDefault()
                ) to typeConverter
            }


        return FileSpec.builder(model.name.packageName, model.name.shortName)
            .addType(
                TypeSpec.classBuilder(model.generatedClassName.className)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec("dbFlowDataBase", ClassNames.DBFlowDatabase)
                            )
                            .addParameter(tableParam.parameterSpec)
                            .apply {
                                if (!model.isQuery) {
                                    addParameter(tableNameParam.parameterSpec)
                                }
                            }
                            .build()
                    )
                    .superclass(superClass)
                    .addSuperclassConstructorParameter("dbFlowDataBase")
                    .apply {
                        if (model.isInternal) {
                            addModifiers(KModifier.INTERNAL)
                        }
                        typeConverters.forEach { (name, model) ->
                            addProperty(
                                typeConverterFieldWriter.create(
                                    TypeConverterFieldWriter.Input(model, name)
                                )
                            )
                        }

                        addProperty(tableParam.propertySpec)
                        if (!model.isQuery) {
                            addProperty(tableNameParam.propertySpec)
                            creationQuery(model, extractors)
                        }
                        if (model.isNormal) {
                            addFunction(getPropertyMethodWriter.create(model))
                            addProperty(allColumnPropertiesWriter.create(model))
                            addFunction(statementBinderWriter.deleteWriter.create(model))
                            addFunction(statementBinderWriter.insertWriter.create(model))
                            addFunction(statementBinderWriter.updateWriter.create(model))
                            insertStatementQuery(model, extractors, isSave = false)
                            insertStatementQuery(model, extractors, isSave = true)
                            updateStatement(model, extractors, primaryExtractors)
                            deleteStatement(model, primaryExtractors)
                        }

                        addFunction(loadFromCursorWriter.create(model))
                        addFunction(primaryConditionClauseWriter.create(model))
                        getObjectType(model)

                        addType(TypeSpec.companionObjectBuilder()
                            .apply {
                                model.flattenedFields(referencesCache).forEach { field ->
                                    addProperty(fieldPropertyWriter.create(field))
                                }
                            }
                            .build()
                        )
                    }.build()
            )
            .build()

    }

    private fun TypeSpec.Builder.insertStatementQuery(
        model: ClassModel,
        extractors: List<FieldExtractor>,
        isSave: Boolean
    ) = apply {
        val joinToString = extractors.joinToString {
            it.commaNames
        }

        addProperty(PropertySpec.builder(
            "${if (isSave) "save" else "insert"}StatementQuery",
            String::class.asClassName()
        )
            .apply {
                addModifiers(KModifier.OVERRIDE)
                getter(
                    FunSpec.getterBuilder()
                        .addCode("return %S", buildString {
                            append("INSERT ${if (isSave) "OR REPLACE" else ""} INTO ${model.dbName}(")
                            append(joinToString)
                            append(") VALUES (${extractors.joinToString { it.valuesName }}")
                            append(")")
                        })
                        .build()
                )
            }
            .build())
    }

    private fun TypeSpec.Builder.updateStatement(
        model: ClassModel,
        extractors: List<FieldExtractor>,
        primaryExtractors: List<FieldExtractor>
    ) = apply {
        addProperty(PropertySpec.builder("updateStatementQuery", String::class.asClassName())
            .apply {
                addModifiers(KModifier.OVERRIDE)
                getter(
                    FunSpec.getterBuilder()
                        .addCode("return %S", buildString {
                            append("UPDATE ${model.dbName} SET ")
                            append(extractors.joinToString { it.updateName })
                            append(" WHERE ")
                            append(primaryExtractors.joinToString { it.updateName })
                        })
                        .build()
                )
            }
            .build())
    }

    private fun TypeSpec.Builder.deleteStatement(
        model: ClassModel,
        primaryExtractors: List<FieldExtractor>
    ) = apply {
        addProperty(PropertySpec.builder("deleteStatementQuery", String::class.asClassName())
            .apply {
                addModifiers(KModifier.OVERRIDE)
                getter(
                    FunSpec.getterBuilder()
                        .addCode("return %S", buildString {
                            append("DELETE FROM ${model.dbName} WHERE ")
                            append(primaryExtractors.joinToString("AND") { it.updateName })
                        })
                        .build()
                )
            }
            .build())
    }

    private fun TypeSpec.Builder.creationQuery(
        model: ClassModel,
        extractors: List<FieldExtractor>
    ) = apply {
        addProperty(PropertySpec.builder("creationQuery", String::class.asClassName())
            .apply {
                addModifiers(KModifier.OVERRIDE)
                getter(
                    FunSpec.getterBuilder()
                        .addCode("return %S", buildString {
                            append("CREATE TABLE IF NOT EXISTS ${model.dbName}(")
                            append(extractors.joinToString { it.createName })
                            append(")")
                        })
                        .build()
                )
            }
            .build())
    }

    private fun TypeSpec.Builder.getObjectType(model: ClassModel) = apply {
        if (model.type !== ClassModel.ClassType.Query) {
            addProperty(
                PropertySpec.builder("type", ClassNames.ObjectType)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode(
                                "return %T.%L", ClassNames.ObjectType,
                                if (model.type == ClassModel.ClassType.Normal) {
                                    "Table"
                                } else "View"
                            )
                            .build()
                    )
                    .build()
            )
        }
    }
}