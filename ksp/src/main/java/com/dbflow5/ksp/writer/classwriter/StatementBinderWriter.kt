package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.writer.TypeCreator
import com.dbflow5.ksp.writer.fieldWrapperName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec

data class StatementModel(
    val classModel: ClassModel,
    val methodName: String,
    val fieldsToLoop: List<FieldModel>,
)

/**
 * Description:
 */
class StatementBinderWriter(
    private val referencesCache: ReferencesCache,
) {

    private fun FunSpec.Builder.loopModels(
        model: FieldModel,
        index: Int,
        modelName: String = "model",
    ): FunSpec.Builder = with(this) {
        when (model) {
            is ReferenceHolderModel -> {
                if (referencesCache.isTable(model)) {
                    writeReferenceModel(modelName, model, index)
                } else {
                    writeSingleModel(model, modelName, index)
                }
            }
            is SingleFieldModel -> {
                writeSingleModel(model, modelName, index)
            }
        }
    }

    private fun FunSpec.Builder.writeReferenceModel(
        modelName: String,
        model: ReferenceHolderModel,
        index: Int
    ): FunSpec.Builder {
        // foreign key has nesting logic
        this.beginControlFlow(
            "%L.%L.let { m -> ",
            modelName,
            model.accessName(useLastNull = true),
        )

        model.references(
            referencesCache,
            nameToNest = model.name
        )
            .forEachIndexed { i, reference ->
                loopModels(
                    model = reference, index = index + i,
                    modelName = "m"
                )
            }
        this.nextControlFlow("?: run")
        this.addStatement("statement.bindNull(%L)", index)
        return this.endControlFlow()
    }

    private fun FunSpec.Builder.writeSingleModel(
        model: FieldModel,
        modelName: String,
        index: Int
    ) = this.addStatement(
        "%L.bind(%L.%L, statement, %L)",
        model.fieldWrapperName,
        modelName,
        model.name.shortName,
        index,
    )

    private val simpleWriter = TypeCreator<StatementModel, FunSpec> { (model, name, fieldsToLoop) ->
        FunSpec.builder(name)
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec("statement", ClassNames.DatabaseStatement))
                addParameter(ParameterSpec("model", model.classType))
                fieldsToLoop.forEachIndexed { index, model ->
                    this.loopModels(model, index)
                }
            }
            .build()
    }


    val insertWriter = TypeCreator<ClassModel, FunSpec> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                methodName = "bindToInsertStatement",
                fieldsToLoop = model.fields
            )
        )
    }

    val updateWriter = TypeCreator<ClassModel, FunSpec> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                methodName = "bindToUpdateStatement",
                fieldsToLoop = listOf(model.fields, model.primaryFields).flatten()
            )
        )
    }

    val deleteWriter = TypeCreator<ClassModel, FunSpec> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                methodName = "bindToDeleteStatement",
                fieldsToLoop = model.primaryFields,
            )
        )
    }
}