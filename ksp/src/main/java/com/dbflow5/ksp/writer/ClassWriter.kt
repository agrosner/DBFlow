package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.kotlinpoet.ParameterPropertySpec
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.generatedClassName
import com.dbflow5.ksp.model.hasTypeConverter
import com.dbflow5.ksp.model.memberSeparator
import com.dbflow5.ksp.model.properties.CreatableScopeProperties
import com.dbflow5.ksp.model.typeConverter
import com.dbflow5.ksp.writer.classwriter.AllColumnPropertiesWriter
import com.dbflow5.ksp.writer.classwriter.CreationQueryWriter
import com.dbflow5.ksp.writer.classwriter.FieldPropertyWriter
import com.dbflow5.ksp.writer.classwriter.GetPropertyMethodWriter
import com.dbflow5.ksp.writer.classwriter.IndexPropertyWriter
import com.dbflow5.ksp.writer.classwriter.LoadFromCursorWriter
import com.dbflow5.ksp.writer.classwriter.PrimaryConditionClauseWriter
import com.dbflow5.ksp.writer.classwriter.StatementBinderWriter
import com.dbflow5.ksp.writer.classwriter.TypeConverterFieldWriter
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.NUMBER
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import java.util.*
import kotlin.reflect.KClass

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
    private val creationQueryWriter: CreationQueryWriter,
    private val indexPropertyWriter: IndexPropertyWriter,
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
                    field = it,
                    referencesCache = referencesCache,
                    classModel = model,
                )
                is SingleFieldModel -> FieldExtractor.SingleFieldExtractor(it, model)
            }
        }
        val primaryExtractors = model.primaryFields.map {
            when (it) {
                is ReferenceHolderModel -> FieldExtractor.ForeignFieldExtractor(
                    field = it,
                    referencesCache = referencesCache,
                    classModel = model,
                )
                is SingleFieldModel -> FieldExtractor.SingleFieldExtractor(it, model)
            }
        }
        val superClass = when (model.type) {
            is ClassModel.ClassType.Normal -> ClassNames.modelAdapter(model.classType)
            is ClassModel.ClassType.View -> ClassNames.modelViewAdapter(model.classType)
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


        return FileSpec.builder(model.name.packageName, model.generatedClassName.shortName)
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
                        model.originatingFile?.let { addOriginatingKSFile(it) }
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
                            createWithDatabase(model)
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
                            updateAutoIncrement(model)
                            saveForeignKeys(model)
                        }

                        addFunction(loadFromCursorWriter.create(model))
                        addFunction(primaryConditionClauseWriter.create(model))
                        getObjectType(model)

                        addType(TypeSpec.companionObjectBuilder()
                            .addSuperinterface(
                                ClassNames.adapterCompanion(
                                    model.classType,
                                )
                            )
                            .apply {
                                addProperty(
                                    PropertySpec.builder(
                                        "table", KClass::class.asClassName()
                                            .parameterizedBy(model.classType)
                                    )
                                        .addModifiers(KModifier.OVERRIDE)
                                        .getter(
                                            FunSpec.getterBuilder()
                                                .addStatement(
                                                    "return %T::class",
                                                    model.classType
                                                )
                                                .build()
                                        )
                                        .build()
                                )
                                model.flattenedFields(referencesCache).forEach { field ->
                                    addProperty(fieldPropertyWriter.create(field))
                                }
                                model.indexGroups.forEach {
                                    addProperty(indexPropertyWriter.create(it))
                                }
                            }
                            .build()
                        )
                    }.build()
            )
            .build()

    }

    private fun TypeSpec.Builder.createWithDatabase(
        model: ClassModel,
    ) {
        if (model.properties is CreatableScopeProperties
            && !model.properties.createWithDatabase
        ) {
            addFunction(
                FunSpec.builder("createWithDatabase")
                    .returns(Boolean::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .addStatement("return %L", false)
                    .build()
            )
        }
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
        addProperty(
            creationQueryWriter.create(
                CreationQueryWriter.Input(model, extractors)
            )
        )
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
                                if (model.type is ClassModel.ClassType.Normal) {
                                    "Table"
                                } else "View"
                            )
                            .build()
                    )
                    .build()
            )
        }
    }

    private fun TypeSpec.Builder.saveForeignKeys(model: ClassModel) {
        val fields = model.fields.filter { referencesCache.isTable(it) }
        if (fields.isNotEmpty()) {
            addFunction(FunSpec.builder("saveForeignKeys")
                .returns(model.classType)
                .addParameter("model", model.classType)
                .addParameter("wrapper", ClassNames.DatabaseWrapper)
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .apply {
                    if (model.hasPrimaryConstructor) {
                        addCode("return model.copy(\n")
                    } else {
                        beginControlFlow("return model.apply")
                    }

                    fields.forEach { field ->
                        addCode(
                            "%L%L = %L.%L.%M(wrapper)%L.%M()%L\n",
                            if (!model.hasPrimaryConstructor) "this." else "",
                            field.accessName(),
                            "model",
                            field.accessName(true),
                            MemberNames.save,
                            if (field.name.nullable) "?" else "",
                            MemberNames.getOrThrow,
                            model.memberSeparator,
                        )
                    }

                    if (model.hasPrimaryConstructor) {
                        addCode(")\n")
                    } else {
                        endControlFlow()
                    }
                }
                .build())
        }
    }


    private fun TypeSpec.Builder.updateAutoIncrement(model: ClassModel) {
        val autoincrementFields = model.primaryFields
            .filter {
                val fieldType = it.fieldType
                fieldType is FieldModel.FieldType.PrimaryAuto
                    && fieldType.isAutoIncrement
            }
        if (autoincrementFields.isNotEmpty()) {
            addFunction(FunSpec.builder("updateAutoIncrement")
                .addParameter("model", model.classType)
                .addParameter("id", Number::class.asClassName())
                .returns(model.classType)
                .addModifiers(KModifier.OVERRIDE)
                .apply {
                    if (model.hasPrimaryConstructor) {
                        addCode("return model.copy(\n")
                    } else {
                        beginControlFlow("return model.apply")
                    }

                    autoincrementFields.forEach { field ->
                        if (!model.hasPrimaryConstructor) {
                            addCode("this.")
                        }
                        addCode(
                            "%L = %L.%L()%L\n",
                            field.name.shortName,
                            "id",
                            when (field.classType) {
                                INT -> "toInt"
                                DOUBLE -> "toDouble"
                                FLOAT -> "toFloat"
                                NUMBER -> ""
                                BYTE -> "toByte"
                                LONG -> "toLong"
                                SHORT -> "toShort"
                                else -> throw IllegalArgumentException(
                                    "Invalid auto primary key type ${field.classType}." +
                                        "could not turn into a number."
                                )
                            },
                            model.memberSeparator
                        )
                    }

                    if (model.hasPrimaryConstructor) {
                        addCode(")\n")
                    } else {
                        endControlFlow()
                    }
                }
                .build())
        }
    }
}
