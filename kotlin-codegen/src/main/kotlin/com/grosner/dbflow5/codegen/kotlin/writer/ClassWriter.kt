package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.distinctAdapterGetters
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.memberSeparator
import com.dbflow5.codegen.shared.properties.CreatableScopeProperties
import com.dbflow5.codegen.shared.properties.TableProperties
import com.dbflow5.codegen.shared.properties.dbName
import com.dbflow5.codegen.shared.tableReferences
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.ParameterPropertySpec
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.AllColumnPropertiesWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.CreationQueryWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.FieldPropertyWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.GetPropertyMethodWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.IndexPropertyWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.LoadFromCursorWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.PrimaryConditionClauseWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.StatementBinderWriter
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NUMBER
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

/**
 * Description:
 */
class ClassWriter(
    private val fieldPropertyWriter: FieldPropertyWriter,
    private val referencesCache: ReferencesCache,
    private val loadFromCursorWriter: LoadFromCursorWriter,
    private val getPropertyMethodWriter: GetPropertyMethodWriter,
    private val allColumnPropertiesWriter: AllColumnPropertiesWriter,
    private val primaryConditionClauseWriter: PrimaryConditionClauseWriter,
    private val statementBinderWriter: StatementBinderWriter,
    private val creationQueryWriter: CreationQueryWriter,
    private val indexPropertyWriter: IndexPropertyWriter,
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
    private val tableSQLWriter: TableSQLWriter,
    private val tableBinderWriter: TableBinderWriter,
    private val primaryModelClauseWriter: PrimaryModelClauseWriter,
) : TypeCreator<ClassModel, FileSpec> {

    override fun create(model: ClassModel): FileSpec {
        val tableParam = ParameterPropertySpec(
            name = "table",
            type = KClass::class.asClassName()
                .parameterizedBy(model.classType),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%T::class", model.classType)
        }
        val tableNameParam = ParameterPropertySpec(
            name = "name",
            type = String::class.asClassName(),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%S", model.dbName)
        }
        val extractors = model.extractors(referencesCache)
        val primaryExtractors = model.primaryExtractors(referencesCache)
        val superClass = model.generatedSuperClass

        val resolveTableReferences = model.distinctAdapterGetters(referencesCache)
        val adapterGetters = resolveTableReferences.map { classModel ->
            ParameterSpec(
                "${classModel.generatedFieldName}Getter",
                LambdaTypeName.get(
                    returnType = classModel.generatedSuperClass,
                )
            )
        }
        val adapterLazies = resolveTableReferences.map { classModel ->
            PropertySpec.builder(
                classModel.generatedFieldName,
                classModel.generatedSuperClass,
            )
                .delegate(
                    "lazy { %L() }",
                    "${classModel.generatedFieldName}Getter"
                )
                .build()
        }

        return FileSpec.builder(model.name.packageName, model.generatedClassName.shortName)
            .apply {
                if (model.isNormal) {
                    addProperty(tableSQLWriter.create(model))
                    addProperty(tableBinderWriter.create(model))
                    addProperty(primaryModelClauseWriter.create(model))
                }
            }
            .addType(
                TypeSpec.classBuilder(model.generatedClassName.className)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(tableParam.parameterSpec)
                            .apply {
                                if (!model.isQuery) {
                                    addParameter(tableNameParam.parameterSpec)
                                }
                                addParameters(adapterGetters)
                            }
                            .build()
                    )
                    .superclass(superClass)
                    .apply {
                        model.originatingSource?.let {
                            originatingFileTypeSpecAdder.addOriginatingFileType(this, it)
                        }
                        if (model.isInternal) {
                            addModifiers(KModifier.INTERNAL)
                        }
                        addProperties(adapterLazies)
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

                            (model.properties as? TableProperties)?.let { props ->
                                if (props.insertConflict != ConflictAction.NONE) {
                                    addProperty(
                                        PropertySpec.builder(
                                            "insertOnConflictAction",
                                            ConflictAction::class
                                        )
                                            .addModifiers(KModifier.OVERRIDE)
                                            .getter(
                                                FunSpec.getterBuilder()
                                                    .addStatement(
                                                        "return %T.%L",
                                                        ConflictAction::class.asClassName(),
                                                        props.insertConflict,
                                                    ).build()
                                            )
                                            .build()
                                    )
                                }
                                if (props.updateConflict != ConflictAction.NONE) {
                                    addProperty(
                                        PropertySpec.builder(
                                            "updateOnConflictAction",
                                            ConflictAction::class
                                        )
                                            .addModifiers(KModifier.OVERRIDE)
                                            .getter(
                                                FunSpec.getterBuilder()
                                                    .addStatement(
                                                        "return %T.%L",
                                                        ConflictAction::class.asClassName(),
                                                        props.updateConflict
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                }
                            }
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
                                    addProperty(fieldPropertyWriter.create(model to field))
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
        model.properties.let { props ->
            if (props is CreatableScopeProperties
                && !props.createWithDatabase
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
                            append(primaryExtractors.joinToString(" AND ") { it.updateName })
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
        if (model.type !== ClassModel.Type.Query) {
            addProperty(
                PropertySpec.builder("type", ClassNames.ObjectType)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode(
                                "return %T.%L", ClassNames.ObjectType,
                                if (model.type is ClassModel.Type.Table) {
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
        val fields = model.tableReferences(referencesCache)
            .filter { it.referenceHolderProperties.saveForeignKeyModel }
        if (fields.isNotEmpty()) {
            addFunction(FunSpec.builder("saveForeignKeys")
                .returns(model.classType)
                .addParameter("model", model.classType)
                .addParameter("wrapper", ClassNames.DatabaseWrapper)
                .addModifiers(KModifier.OVERRIDE)
                .apply {
                    if (model.hasPrimaryConstructor) {
                        addCode("return model.copy(\n")
                    } else {
                        beginControlFlow("return model.apply")
                    }

                    fields.forEach { field ->
                        val reference = referencesCache.resolve(field)
                        addCode(
                            "%L%L = %L.%L.let { this@%L.%L" +
                                ".save(it, wrapper) }%L.%M()%L\n",
                            if (!model.hasPrimaryConstructor) "this." else "",
                            field.accessName(),
                            "model",
                            field.accessName(true),
                            model.generatedClassName.shortName,
                            reference.generatedFieldName,
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
        val autoincrementFields = model.primaryAutoIncrementFields
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
                                        "could not turn into a number. ${model.name.shortName}:${field.name.shortName}"
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
