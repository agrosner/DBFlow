package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.DatabaseModel
import com.dbflow5.codegen.shared.generatedClassName
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.codegen.shared.ClassNames
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.ParameterPropertySpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
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

        return FileSpec.builder(model.name.packageName, model.generatedClassName.shortName)
            .apply {
                addType(
                    TypeSpec.classBuilder(model.generatedClassName.className)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter("holder", ClassNames.MutableHolder)
                                .addParameter(associatedClassName.parameterSpec)
                                .addParameter(version.parameterSpec)
                                .addParameter(foreignKeys.parameterSpec)
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
                            addInitializerBlock(
                                CodeBlock.builder()
                                    .apply {
                                        model.tables.forEach { table ->
                                            addStatement(
                                                "addModelAdapter(%T(this), holder)",
                                                table.generatedClassName.className
                                            )
                                        }
                                        model.views.forEach { view ->
                                            addStatement(
                                                "addModelViewAdapter(%T(this), holder)",
                                                view.generatedClassName.className
                                            )
                                        }
                                        model.queries.forEach { query ->
                                            addStatement(
                                                "addRetrievalAdapter(%T(this), holder)",
                                                query.generatedClassName.className
                                            )
                                        }

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
}