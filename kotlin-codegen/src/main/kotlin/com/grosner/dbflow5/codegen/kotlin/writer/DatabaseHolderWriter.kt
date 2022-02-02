package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseHolderModel
import com.dbflow5.codegen.shared.generatedClassName
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.properties.nameWithFallback
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.stripQuotes
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description:
 */
class DatabaseHolderWriter(
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<DatabaseHolderModel, FileSpec> {

    override fun create(model: DatabaseHolderModel) =
        FileSpec.builder(
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
                .addInitializerBlock(
                    CodeBlock.builder()
                        .apply {
                            // type converters

                            model.databases.forEach { db ->
                                addStatement(
                                    "putDatabase(%T())",
                                    db.generatedClassName.className
                                )
                            }
                            putAdapter("Model", model.tables)
                            putAdapter("Query", model.queries)
                            putAdapter("View", model.views)
                        }
                        .build()
                )
                .build()
        )
            .build()

    private fun CodeBlock.Builder.putAdapter(name: String, models: List<ClassModel>) {
        models.forEach { view ->
            addStatement(
                "put${name}Adapter(%T())",
                view.generatedClassName.className
            )
        }
    }
}