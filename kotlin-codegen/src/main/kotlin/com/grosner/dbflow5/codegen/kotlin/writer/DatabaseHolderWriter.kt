package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseHolderModel
import com.dbflow5.codegen.shared.GeneratedClassModel
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.properties.nameWithFallback
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.stripQuotes
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description:
 */
class DatabaseHolderWriter(
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<DatabaseHolderModel, FileSpec> {

    override fun create(model: DatabaseHolderModel): FileSpec {
        return FileSpec.builder(
            model.name.packageName,
            model.properties.nameWithFallback(
                model.name.shortName,
            ).stripQuotes()
        ).addType(
            TypeSpec.classBuilder(
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
                .superclass(ClassNames.DatabaseHolder)
                .addSuperGeneratedSet("databases", model.databases)
                .addSuperGeneratedSet("tables", model.tables)
                .addSuperGeneratedSet("views", model.views)
                .addSuperGeneratedSet("queries", model.queries)
                .build()
        )
            .build()
    }

    private fun TypeSpec.Builder.addSuperGeneratedSet(
        name: String,
        objects: List<GeneratedClassModel>,
    ) = apply {
        addSuperclassConstructorParameter(
            "$name = setOf(${objects.joinToString { "%T()" }})",
            *objects.map { it.generatedClassName.className }.toTypedArray()
        )
    }
}