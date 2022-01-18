package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.codegen.shared.ManyToManyModel
import com.dbflow5.codegen.shared.ReferenceHolderModel
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.ParameterPropertySpec
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description: Writes join tables.
 */
class ManyToManyClassWriter(
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<ManyToManyModel, FileSpec> {

    override fun create(model: ManyToManyModel): FileSpec {
        val classModel = model.classModel
        return FileSpec.builder(
            classModel.name.packageName,
            classModel.name.shortName,
        )
            .apply {
                val paramProperties = classModel.fields
                    .map { prop ->
                        ParameterPropertySpec(
                            name = prop.propertyName,
                            type = prop.classType,
                        ) {
                            addAnnotation(PrimaryKey::class)
                            if (prop is ReferenceHolderModel) {
                                addAnnotation(
                                    AnnotationSpec.builder(ForeignKey::class)
                                        .build()
                                )
                            }
                        }
                    }
                addType(TypeSpec.classBuilder(
                    classModel.name.shortName,
                )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(paramProperties.map { it.parameterSpec })
                            .build()
                    )
                    .apply {
                        model.originatingSource?.let {
                            originatingFileTypeSpecAdder.addOriginatingFileType(this, it)
                        }
                        // doesn't work quite yet.
                        /* addAnnotation(
                             AnnotationSpec.builder(
                                 Table::class,
                             )
                                 .addMember("database = %T::class", model.databaseTypeName)
                                 .addMember("name = %S", model.dbName.quoteIfNeeded())
                                 .build()
                         )*/
                        addModifiers(KModifier.DATA)
                        addProperties(paramProperties.map { it.propertySpec })
                    }
                    .build())
            }
            .build()
    }
}