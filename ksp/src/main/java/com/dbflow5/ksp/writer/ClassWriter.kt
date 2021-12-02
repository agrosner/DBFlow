package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.kotlinpoet.ParameterPropertySpec
import com.dbflow5.ksp.model.ClassModel
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
            defaultValue("%S", model.name.getShortName().quoteIfNeeded())
        }
        return FileSpec.builder(model.name.getQualifier(), model.name.getShortName())
            .addType(
                TypeSpec.classBuilder(
                    ClassName(
                        model.name.getQualifier(),
                        "${model.name.getShortName()}_Table"
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
                        loadFromCursor(model)
                        getPrimaryConditionClause(model)

                        addType(TypeSpec.companionObjectBuilder()
                            .apply {
                                model.fields.forEach { field ->
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
                model.fields.forEach { field ->
                    addCode(
                        """
                        %S -> %L
                        
                    """.trimIndent(),
                        field.name.getShortName().quoteIfNeeded(),
                        field.name.getShortName()
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
                        model.fields.forEach { field ->
                            addCode("%L,\n", field.name.getShortName())
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
                    addCode(
                        "%N = %N.%M(%N),\n",
                        field.name.getShortName(),
                        field.name.getShortName(),
                        MemberNames.propertyGet,
                        "cursor"
                    )
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
                model.fields.forEach { field ->
                    addCode(
                        "and(%L eq %N.%L)\n",
                        field.name.getShortName(),
                        "model",
                        field.name.getShortName(),
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
                model.fields.forEachIndexed { index, model ->
                    addStatement(
                        "%L.bind(model, statement, %L)",
                        model.fieldWrapperName,
                        index,
                    )
                }
            }
            .build())
    }
}