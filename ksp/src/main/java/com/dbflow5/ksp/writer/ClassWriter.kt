package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.kotlinpoet.ParameterPropertySpec
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.ForeignKeyModel
import com.dbflow5.ksp.model.ReferencesCache
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

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
        return FileSpec.builder(model.name.packageName, model.name.shortName)
            .addType(
                TypeSpec.classBuilder(
                    ClassName(
                        model.name.packageName,
                        "${model.name.shortName}_Table"
                    )
                )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec("dbFlowDataBase", ClassNames.DBFlowDatabase)
                            )
                            .addParameter(tableParam.parameterSpec)
                            .addParameter(tableNameParam.parameterSpec)
                            .build()
                    )
                    .superclass(ClassNames.modelAdapter(model.classType))
                    .addSuperclassConstructorParameter("dbflowDataBase")
                    .apply {
                        addProperty(tableParam.propertySpec)
                        addProperty(tableNameParam.propertySpec)

                        getPropertyMethod(model)
                        allColumnProperties(model)
                        bindInsert(model)
                        bindUpdate(model)
                        bindDelete(model)
                        insertStatementQuery(model, extractors, isSave = false)
                        insertStatementQuery(model, extractors, isSave = true)
                        creationQuery(model, extractors)
                        updateStatement(model, extractors, primaryExtractors)
                        deleteStatement(model, primaryExtractors)
                        loadFromCursor(model)
                        getPrimaryConditionClause(model)

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
                returns(ClassNames.Property)
                beginControlFlow(
                    "return when(%N.%M())",
                    "columnName",
                    MemberNames.quoteIfNeeded
                )
                model.flattenedFields(referencesCache).forEach { field ->
                    addCode(
                        """
                        %S -> %L
                        
                    """.trimIndent(),
                        field.name.shortName.quoteIfNeeded(),
                        field.name.shortName
                    )
                }
                addCode(
                    """
                    else -> throw %T("Invalid column name passed. Ensure you are calling the correct table's column") 
                """.trimIndent(), IllegalArgumentException::class.asClassName()
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
                            addCode("%L,\n", field.name.shortName)
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
                                "\t%N = ((%M %M %T::class) %N\n",
                                field.name.shortName,
                                MemberNames.select,
                                MemberNames.from,
                                field.classType,
                                MemberNames.where,
                            )
                            field.references(referencesCache, "").zip(
                                field.references(referencesCache, field.dbName)
                            ).forEach { (plain, referenced) ->
                                addCode(
                                    "\t\t%T.%L %M %N.%M(%N),\n",
                                    field.classType,
                                    plain.name.shortName,
                                    MemberNames.eq,
                                    referenced.name.shortName,
                                    MemberNames.propertyGet,
                                    "cursor"
                                )
                            }
                            addCode(
                                "\t).%M(%N)\n",
                                if (field.classType.isNullable)
                                    MemberNames.querySingle
                                else MemberNames.requireSingle,
                                "wrapper",
                            )
                        }
                        is SingleFieldModel -> {
                            addCode(
                                "\t%N = %N.%M(%N),\n",
                                field.name.shortName,
                                field.name.shortName,
                                MemberNames.propertyGet,
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
                addCode("return %T.clause().apply {\n", ClassNames.OperatorGroup)
                model.primaryFlattenedFields(referencesCache).forEach { field ->
                    addCode(
                        "and(%L %M %N.%L)\n",
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

    private fun TypeSpec.Builder.bindInsert(model: ClassModel) = apply {
        addFunction(FunSpec.builder("bindToInsertStatement")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec("statement", ClassNames.DatabaseStatement))
                addParameter(ParameterSpec("model", model.classType))
                model.flattenedFields(referencesCache).forEachIndexed { index, model ->
                    addStatement(
                        "%L.bind(model, statement, %L)",
                        model.fieldWrapperName,
                        index,
                    )
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
                    model.flattenedFields(referencesCache),
                    model.primaryFlattenedFields(referencesCache),
                ).flatten().forEachIndexed { index, model ->
                    addStatement(
                        "%L.bind(model, statement, %L)",
                        model.fieldWrapperName,
                        index,
                    )
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
                model.primaryFlattenedFields(referencesCache).forEachIndexed { index, model ->
                    addStatement(
                        "%L.bind(model, statement, %L)",
                        model.fieldWrapperName,
                        index,
                    )
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
}