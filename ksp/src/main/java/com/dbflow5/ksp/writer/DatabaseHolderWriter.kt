package com.dbflow5.ksp.writer

import com.dbflow5.ksp.model.DatabaseHolderModel
import com.dbflow5.ksp.model.generatedClassName
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description:
 */
class DatabaseHolderWriter : TypeCreator<DatabaseHolderModel, FileSpec> {

    override fun create(model: DatabaseHolderModel) =
        FileSpec.builder(
            model.name.packageName,
            model.properties.nameWithFallback(
                model.name.shortName,
            )
        ).addType(
            TypeSpec.objectBuilder(
                model.properties.nameWithFallback(
                    model.name.shortName,
                )
            )
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