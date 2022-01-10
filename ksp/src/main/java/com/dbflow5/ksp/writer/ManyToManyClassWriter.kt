package com.dbflow5.ksp.writer

import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.ksp.kotlinpoet.ParameterPropertySpec
import com.dbflow5.ksp.model.interop.ksFile
import com.dbflow5.model.ManyToManyModel
import com.dbflow5.model.ReferenceHolderModel
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile

/**
 * Description: Writes join tables.
 */
class ManyToManyClassWriter : TypeCreator<ManyToManyModel, FileSpec> {

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
                        model.originatingFile?.ksFile()?.let { addOriginatingKSFile(it) }
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