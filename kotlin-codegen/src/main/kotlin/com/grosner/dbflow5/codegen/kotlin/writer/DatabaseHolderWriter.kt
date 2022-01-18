package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.model.DatabaseHolderModel
import com.dbflow5.codegen.model.generatedClassName
import com.dbflow5.codegen.model.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.model.properties.nameWithFallback
import com.dbflow5.codegen.writer.TypeCreator
import com.dbflow5.ksp.ClassNames
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
                    model.allOriginatingFiles.forEach { fileType ->
                        fileType.let {
                            originatingFileTypeSpecAdder.addOriginatingFileType(
                                this,
                                it
                            )
                        }
                    }
                }
                .superclass(ClassNames.DatabaseHolder)
                .addInitializerBlock(
                    CodeBlock.builder()
                        .apply {
                            // type converters

                            model.databases.forEach { db ->
                                addStatement("%T(this)", db.generatedClassName.className)
                            }
                        }
                        .build()
                )
                .build()
        )
            .build()
}