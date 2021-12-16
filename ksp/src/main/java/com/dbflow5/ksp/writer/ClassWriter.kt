package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.kotlinpoet.ParameterPropertySpec
import com.dbflow5.ksp.model.*
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.FunSpec.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Description:
 */
class ClassWriter(
    private val fieldPropertyWriter: FieldPropertyWriter,
    private val propertyStatementWrapperWriter: PropertyStatementWrapperWriter,
    private val referencesCache: ReferencesCache,
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
                is ForeignKeyModel -> FieldExtractor.ForeignFieldExtractor(
                    it,
                    referencesCache,
                )
                is SingleFieldModel -> FieldExtractor.SingleFieldExtractor(it)
            }
        }
        val primaryExtractors = model.primaryFields.map {
            when (it) {
                is ForeignKeyModel -> FieldExtractor.ForeignFieldExtractor(
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
        return FileSpec.builder(model.name.packageName, model.name.shortName)
            .addType(
                TypeSpec.classBuilder(model.generatedClassName.className)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec("dbFlowDataBase", ClassNames.DBFlowDatabase)
                            )
                            .addParameter(tableParam.parameterSpec)
                            .addParameter(tableNameParam.parameterSpec)
                            .build()
                    )
                    .superclass(superClass)
                    .addSuperclassConstructorParameter("dbFlowDataBase")
                    .apply {
                        addProperty(tableParam.propertySpec)
                        addProperty(tableNameParam.propertySpec)

                        getPropertyMethod(model)
                        allColumnProperties(model)
                        if (model.type == ClassModel.ClassType.Normal) {
                            bindInsert(model)
                            bindUpdate(model)
                            bindDelete(model)
                            insertStatementQuery(model, extractors, isSave = false)
                            insertStatementQuery(model, extractors, isSave = true)
                            updateStatement(model, extractors, primaryExtractors)
                            deleteStatement(model, primaryExtractors)
                        }
                        if (model.type != ClassModel.ClassType.View) {
                            creationQuery(model, extractors)
                        }

                        loadFromCursor(model)
                        getPrimaryConditionClause(model)
                        getObjectType(model)

                        addType(TypeSpec.companionObjectBuilder()
                            .apply {
                                model.flattenedFields(referencesCache).forEach { field ->
                                    addProperty(fieldPropertyWriter.create(field))
                                    addProperty(propertyStatementWrapperWriter.create(field))
                                }
                            }
                            .build()
                        )
                    }.build()
            )
            .build()

    }

    private fun TypeSpec.Builder.getPropertyMethod(model: ClassModel) = apply {
        addFunction(FunSpec.builder("getProperty")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec("columnName", String::class.asClassName()))
                returns(
                    ClassNames.Property.parameterizedBy(
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
                        field.name.shortName.quoteIfNeeded(),
                        "Companion",
                        field.name.shortName
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
            .build())
    }

    private fun TypeSpec.Builder.allColumnProperties(model: ClassModel) = apply {
        addProperty(
            PropertySpec.builder(
                name = "allColumnProperties",
                type = Array::class.asClassName()
                    .parameterizedBy(ClassNames.IProperty),
                KModifier.OVERRIDE,
            )
                .getter(FunSpec.getterBuilder()
                    .addCode("return arrayOf(\n")
                    .apply {
                        model.flattenedFields(referencesCache).forEach { field ->
                            addCode(
                                "%L.%L,\n",
                                "Companion",
                                field.name.shortName
                            )
                        }
                    }
                    .addCode(")")
                    .build())
                .build()
        )
    }

    private fun TypeSpec.Builder.loadFromCursor(model: ClassModel) = apply {
        addFunction(FunSpec.builder("loadFromCursor")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(
                    ParameterSpec("cursor", ClassNames.FlowCursor),
                )
                addParameter(
                    ParameterSpec("wrapper", ClassNames.DatabaseWrapper)
                )
                addCode("return %T(\n", model.classType)
                model.fields.forEach { field ->
                    when (field) {
                        is ForeignKeyModel -> {
                            addCode(
                                "\t%N = ((%M %L %T::class) %L\n",
                                field.name.shortName,
                                MemberNames.select,
                                MemberNames.from,
                                field.nonNullClassType,
                                MemberNames.where,
                            )
                            field.references(referencesCache, "").zip(
                                field.references(referencesCache, field.dbName)
                            ).forEachIndexed { index, (plain, referenced) ->
                                addCode("\t\t")
                                if (index > 0) {
                                    addCode("%L ", "and")
                                }
                                val className = field.nonNullClassType as ClassName
                                addCode(
                                    "(%T.%L %L %L.%N.%M(%N))\n",
                                    ClassName(
                                        className.packageName,
                                        className.simpleName + "_Adapter",
                                    ),
                                    plain.name.shortName,
                                    MemberNames.eq,
                                    "Companion",
                                    referenced.name.shortName,
                                    MemberNames.infer,
                                    "cursor"
                                )
                            }
                            addCode(
                                "\t).%L(%N),\n",
                                if (field.classType.isNullable)
                                    MemberNames.querySingle
                                else MemberNames.requireSingle,
                                "wrapper",
                            )
                        }
                        is SingleFieldModel -> {
                            addCode(
                                "\t%N = %L.%N.%M(%N),\n",
                                field.name.shortName,
                                "Companion",
                                field.name.shortName,
                                MemberNames.infer,
                                "cursor"
                            )
                        }
                    }

                }
                addCode(")")
            }
            .build())
    }

    private fun TypeSpec.Builder.getPrimaryConditionClause(model: ClassModel) = apply {
        addFunction(FunSpec.builder("getPrimaryConditionClause")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec("model", model.classType))
                addCode("return %T.clause().apply{\n", ClassNames.OperatorGroup)
                model.primaryFlattenedFields(referencesCache).forEach { field ->
                    addCode(
                        "and(%L.%L %L %N.%L)\n",
                        "Companion",
                        field.name.shortName,
                        MemberNames.eq,
                        "model",
                        field.name.shortName,
                    )
                }
                addCode("}\n")
            }
            .build())
    }


    private fun Builder.loopModels(
        model: FieldModel,
        index: Int,
        modelName: String = "model",
    ): Builder = with(this) {
        when (model) {
            is ForeignKeyModel -> {
                // foreign key has nesting logic
                this.beginControlFlow(
                    "%L.%L%L.let { m -> ",
                    modelName,
                    model.originatingName.shortName,
                    if (model.classType.isNullable) "?" else ""
                )

                model.references(this@ClassWriter.referencesCache, namePrefix = model.dbName)
                    .forEachIndexed { i, reference ->
                        loopModels(
                            model = reference, index = index + i,
                            modelName = "m"
                        )
                    }
                this.nextControlFlow("?: run")
                this.addStatement("statement.bindNull(%L)", index)
                this.endControlFlow()
            }
            is SingleFieldModel -> {
                // simple field has access
                this.addStatement(
                    "%L.bind(%L.%L, statement, %L)",
                    model.fieldWrapperName,
                    modelName,
                    model.originatingName.shortName,
                    index,
                )
            }
        }
    }

    private fun TypeSpec.Builder.bindInsert(model: ClassModel) = apply {
        addFunction(FunSpec.builder("bindToInsertStatement")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec("statement", ClassNames.DatabaseStatement))
                addParameter(ParameterSpec("model", model.classType))
                model.fields.forEachIndexed { index, model ->
                    this.loopModels(model, index)
                }
            }
            .build())
    }


    private fun TypeSpec.Builder.bindUpdate(model: ClassModel) = apply {
        addFunction(FunSpec.builder("bindToUpdateStatement")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec("statement", ClassNames.DatabaseStatement))
                addParameter(ParameterSpec("model", model.classType))
                listOf(
                    model.fields,
                    model.primaryFields,
                ).flatten().forEachIndexed { index, model ->
                    this.loopModels(model, index)
                }

            }
            .build())
    }

    private fun TypeSpec.Builder.bindDelete(model: ClassModel) = apply {
        addFunction(FunSpec.builder("bindToDeleteStatement")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec("statement", ClassNames.DatabaseStatement))
                addParameter(ParameterSpec("model", model.classType))
                model.primaryFields.forEachIndexed { index, model ->
                    this.loopModels(model, index)
                }
            }
            .build())
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