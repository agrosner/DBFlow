package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.kotlinpoet.ParameterPropertySpec
import com.dbflow5.ksp.model.DatabaseModel
import com.dbflow5.ksp.model.generatedClassName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile

/**
 * Description:
 */
class DatabaseWriter : TypeCreator<DatabaseModel, FileSpec> {

    override fun create(model: DatabaseModel): FileSpec {
        val associatedClassName = ParameterPropertySpec(
            name = "associatedDatabaseClassFile",
            type = Class::class.asClassName()
                .parameterizedBy(model.classType),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%T::class.java", model.classType)
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

        val areConsistencyChecksEnabled = ParameterPropertySpec(
            name = "areConsistencyChecksEnabled",
            type = Boolean::class.asClassName(),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%L", model.properties.areConsistencyChecksEnabled)
        }

        val backupEnabled = ParameterPropertySpec(
            name = "backupEnabled",
            type = Boolean::class.asClassName(),
        ) {
            addModifiers(KModifier.OVERRIDE)
            defaultValue("%L", model.properties.backupEnabled)
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
                                .addParameter(areConsistencyChecksEnabled.parameterSpec)
                                .addParameter(backupEnabled.parameterSpec)
                                .build()
                        )
                        .apply {
                            model.originatingFile?.let { addOriginatingKSFile(it) }
                            superclass(model.classType)
                            addProperty(associatedClassName.propertySpec)
                            addProperty(version.propertySpec)
                            addProperty(foreignKeys.propertySpec)
                            addProperty(areConsistencyChecksEnabled.propertySpec)
                            addProperty(backupEnabled.propertySpec)
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
                                                "addRetrievalAdapter(%T(this), holder)\n",
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