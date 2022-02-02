package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseModel
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.ParameterPropertySpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

/**
 * Description:
 */
class DatabaseWriter(
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<DatabaseModel, FileSpec> {

    override fun create(model: DatabaseModel): FileSpec {
        val associatedClassName = ParameterPropertySpec(
            name = "associatedDatabaseClassFile",
            type = KClass::class.asClassName()
                .parameterizedBy(model.classType),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%T::class", model.classType)
        }
        val version = ParameterPropertySpec(
            name = "databaseVersion",
            type = Int::class.asClassName()
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%L", model.properties.version)
        }
        val foreignKeys = ParameterPropertySpec(
            name = "isForeignKeysSupported",
            type = Boolean::class.asClassName(),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%L", model.properties.foreignKeyConstraintsEnforced)
        }

        val adapterFields = model.adapterFields
            .map {
                ParameterPropertySpec(
                    name = it.name.shortName,
                    type = it.typeName,
                ) {
                    addModifiers(KModifier.OVERRIDE)
                }
            }

        return FileSpec.builder(model.name.packageName, model.generatedClassName.shortName)
            .apply {
                addType(
                    TypeSpec.classBuilder(model.generatedClassName.className)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter(associatedClassName.parameterSpec)
                                .addParameter(version.parameterSpec)
                                .addParameter(foreignKeys.parameterSpec)
                                .addParameters(adapterFields.map { it.parameterSpec })
                                .build()
                        )
                        .apply {
                            model.originatingSource?.let {
                                originatingFileTypeSpecAdder.addOriginatingFileType(
                                    this,
                                    it
                                )
                            }
                            superclass(model.classType)
                            addProperty(associatedClassName.propertySpec)
                            addProperty(version.propertySpec)
                            addProperty(foreignKeys.propertySpec)
                            addProperty(classProperty("queries", model.queries))
                            addProperty(classProperty("tables", model.tables))
                            addProperty(classProperty("views", model.views))
                            addProperties(adapterFields.map { it.propertySpec })
                            addInitializerBlock(
                                CodeBlock.builder()
                                    .apply {
                                        model.migrations.groupBy { it.properties.version }
                                            .toSortedMap(reverseOrder())
                                            .forEach { (version, migrations) ->
                                                migrations
                                                    ?.sortedBy { it.properties.priority }
                                                    ?.forEach { definition ->
                                                        addStatement(
                                                            "addMigration(%L, %T())",
                                                            version,
                                                            definition.classType,
                                                        )
                                                    }
                                            }
                                    }
                                    .build()
                            )
                        }
                        .build()
                )

                // extension methods for db scope

                model.adapterFields
                    .forEach { clazz ->
                        addProperty(
                            PropertySpec.builder(
                                clazz.name.shortName,
                                clazz.typeName,
                            )
                                .apply {
                                    //if (clazz.isInternal) {
                                    //    addModifiers(KModifier.INTERNAL)
                                    //}
                                }
                                .receiver(ClassNames.dbScope(model.classType))
                                .getter(
                                    FunSpec.getterBuilder()
                                        .addStatement(
                                            "return db.%L", clazz.name.shortName,
                                        )
                                        .build()
                                )
                                .build()
                        )
                    }
            }
            .build()
    }

    private fun classProperty(
        name: String,
        objects: List<ClassModel>,
    ) = PropertySpec.builder(
        name,
        List::class.asTypeName().parameterizedBy(
            KClass::class.asTypeName()
                .parameterizedBy(WildcardTypeName.producerOf(Any::class.asTypeName()))
        )
    )
        .addModifiers(KModifier.OVERRIDE)
        .initializer(CodeBlock.builder()
            .apply {
                addStatement("listOf(")
                objects.forEach {
                    addStatement(
                        "%T::class,",
                        it.classType
                    )
                }
                addStatement(")")
            }
            .build())
        .build()
}