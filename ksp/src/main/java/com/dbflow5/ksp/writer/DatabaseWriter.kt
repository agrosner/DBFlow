package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.kotlinpoet.ParameterPropertySpec
import com.dbflow5.ksp.model.DatabaseModel
import com.dbflow5.ksp.model.generatedClassName
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
class DatabaseWriter : TypeCreator<DatabaseModel, FileSpec> {

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
            name = "version",
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

        return FileSpec.builder(model.name.packageName, model.name.shortName)
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
                            superclass(model.classType)
                            addProperty(associatedClassName.propertySpec)
                            addProperty(version.propertySpec)
                            addProperty(foreignKeys.propertySpec)
                            addInitializerBlock(
                                CodeBlock.builder()
                                    .apply {
                                        model.tables.forEach { table ->
                                            add(
                                                "addModelAdapter(%T(this), holder)\n",
                                                table.generatedClassName.className
                                            )
                                        }
                                        model.views.forEach { view ->
                                            add(
                                                "addModelViewAdapter(%T(this), holder)\n",
                                                view.generatedClassName.className
                                            )
                                        }
                                        model.queryModels.forEach { query ->
                                            add(
                                                "addQueryModelAdapter(%T(this), holder)\n",
                                                query.generatedClassName.className
                                            )
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