package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseModel
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.stripQuotes
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.ParameterPropertySpec
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.ParameterSpec
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
    private val nameAllocator: NameAllocator,
) : TypeCreator<DatabaseModel, FileSpec> {

    override fun create(model: DatabaseModel): FileSpec {
        val name =
            nameAllocator.newName(
                model.generatedClassName.shortName.stripQuotes()
                    .replaceFirstChar { it.lowercase() },
                model.generatedClassName,
            )

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

        val settings = ParameterPropertySpec(
            name = "settings",
            type = ClassNames.DBSettings,
        ) {
            addModifiers(KModifier.OVERRIDE)
        }

        val adapterFields = model.adapterFields
            .map {
                ParameterPropertySpec(
                    name = it.name.shortName,
                    type = it.adapterTypeName,
                ) {
                    addModifiers(KModifier.OVERRIDE)
                }
            }

        return FileSpec.builder(model.name.packageName, model.generatedClassName.shortName)
            .apply {
                addAnnotation(
                    AnnotationSpec.builder(ClassNames.OptIn)
                        .addMember("%T::class", ClassNames.InternalDBFlowApi)
                        .build()
                )
                addType(
                    TypeSpec.classBuilder(model.generatedClassName.className)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter(settings.parameterSpec)
                                .addParameter(version.parameterSpec)
                                .addParameter(foreignKeys.parameterSpec)
                                .addParameters(adapterFields.map { it.parameterSpec })
                                .build()
                        )
                        .apply {
                            addType(
                                TypeSpec.companionObjectBuilder()
                                    .addSuperinterface(ClassNames.dbCreator(model.classType))
                                    .addFunction(
                                        FunSpec.builder(
                                            "create"
                                        )
                                            .addModifiers(KModifier.OVERRIDE)
                                            .addParameter(
                                                ParameterSpec.builder(
                                                    "platformSettings",
                                                    ClassNames.DBPlatformSettings
                                                )
                                                    .build()
                                            )
                                            .addParameter(
                                                ParameterSpec.builder(
                                                    "settingsFn", LambdaTypeName.get(
                                                    receiver = ClassNames.DBSettings,
                                                    returnType = ClassNames.DBSettings
                                                )
                                                )
                                                    .build()
                                            )
                                            .addStatement(
                                                "return %T.%N_factory(settings = %T(name = %S, platformSettings = platformSettings).settingsFn())",
                                                ClassNames.GeneratedDatabaseHolderFactory,
                                                name,
                                                ClassNames.DBSettings,
                                                model.name.shortName,
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            model.originatingSource?.let {
                                originatingFileTypeSpecAdder.addOriginatingFileType(
                                    this,
                                    it
                                )
                            }
                            superclass(model.classType)
                            addProperty(version.propertySpec)
                            addProperty(foreignKeys.propertySpec)
                            addProperty(settings.propertySpec)
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