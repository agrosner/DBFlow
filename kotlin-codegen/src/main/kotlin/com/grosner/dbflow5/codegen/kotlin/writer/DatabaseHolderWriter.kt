package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseHolderModel
import com.dbflow5.codegen.shared.GeneratedClassModel
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.distinctAdapterGetters
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.properties.nameWithFallback
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.stripQuotes
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description:
 */
class DatabaseHolderWriter(
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
    private val referencesCache: ReferencesCache,
    private val nameAllocator: NameAllocator,
) : TypeCreator<DatabaseHolderModel, FileSpec> {


    override fun create(model: DatabaseHolderModel): FileSpec {
        return FileSpec.builder(
            model.name.packageName,
            model.properties.nameWithFallback(
                model.name.shortName,
            ).stripQuotes()
        ).addType(
            TypeSpec.objectBuilder(
                model.properties.nameWithFallback(
                    model.name.shortName,
                ).stripQuotes()
            )
                .apply {
                    originatingFileTypeSpecAdder.addOriginatingFileType(
                        this,
                        model.originatingSource,
                    )
                }
                .addSuperinterface(ClassNames.DatabaseHolderFactory)
                .addClassProperties(model.tables, referencesCache)
                .addClassProperties(model.queries, referencesCache)
                .addClassProperties(model.views, referencesCache)
                .addDatabaseProperties(model)
                .addFunction(
                    FunSpec.builder("create")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(ClassNames.DatabaseHolder)
                        .addCode("return %T(\n", ClassNames.DatabaseHolder)
                        .addSuperGeneratedSet("tables", model.tables)
                        .addSuperGeneratedSet("views", model.views)
                        .addSuperGeneratedSet("queries", model.queries)
                        .addCode(")\n")
                        .build()
                )

                .build()
        )
            .build()
    }

    private fun FunSpec.Builder.addSuperGeneratedSet(
        name: String,
        objects: List<GeneratedClassModel>,
    ) = apply {
        addCode(
            "$name = setOf(${objects.joinToString { nameAllocator[it.generatedClassName] }}),\n",
        )
    }

    private fun TypeSpec.Builder.addDatabaseProperties(
        model: DatabaseHolderModel,
    ) = apply {
        model.databases.forEach { db ->
            val name = nameAllocator[db.generatedClassName]
            addFunction(
                FunSpec.builder("${name}_factory")
                    .addModifiers(KModifier.INTERNAL)
                    .addParameter("settings", ClassNames.DBSettings)
                    .returns(db.classType)
                    .addStatement(
                        "return %T(%L, ${db.adapterFields.joinToString { "%L = %N" }})",
                        db.generatedClassName.className,
                        "settings",
                        *db.adapterFields
                            .map { fieldModel -> fieldModel.name.shortName to fieldModel.associatedClassModel }
                            .map { (name, type) -> name to nameAllocator[type.generatedClassName] }
                            .fold(mutableListOf<String>()) { acc, (x, y) ->
                                acc.apply {
                                    add(x)
                                    add(y)
                                }
                            }
                            .toTypedArray(),
                    )
                    .build()
            )
        }
    }

    private fun TypeSpec.Builder.addClassProperties(
        objects: List<ClassModel>,
        referencesCache: ReferencesCache,
    ) = apply {
        objects.forEach { obj ->
            val name = nameAllocator[obj.generatedClassName]
            val adapterGetters = obj.distinctAdapterGetters(referencesCache)
            addProperty(
                PropertySpec.builder(
                    name,
                    obj.generatedSuperClass,
                )
                    .addModifiers(KModifier.PRIVATE)
                    .initializer(
                        "%M(${adapterGetters.joinToString { "%N = { %N }" }})",
                        obj.generatedAdapterName(nameAllocator).memberName,
                        *adapterGetters.map {
                            "${it.generatedFieldName}Getter" to nameAllocator[
                                it.generatedClassName
                            ]
                        }
                            .fold(mutableListOf<String>()) { acc, (x, y) ->
                                acc.apply {
                                    add(x)
                                    add(y)
                                }
                            }.toTypedArray()
                    )
                    .build()
            )
        }
    }
}