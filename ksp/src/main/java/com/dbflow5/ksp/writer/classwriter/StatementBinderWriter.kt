package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.hasTypeConverter
import com.dbflow5.ksp.writer.TypeCreator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec

private sealed interface Method {
    val name: String
    val statementListenerName: String

    object Insert : Method {
        override val name: String = "bindToInsertStatement"
        override val statementListenerName: String = "onBindToInsertStatement"
    }

    object Update : Method {
        override val name: String = "bindToUpdateStatement"
        override val statementListenerName: String = "onBindToUpdateStatement"
    }

    object Delete : Method {
        override val name: String = "bindToDeleteStatement"
        override val statementListenerName: String = "onBindToDeleteStatement"
    }
}

private data class StatementModel(
    val classModel: ClassModel,
    val method: Method,
    val fieldsToLoop: List<FieldModel>,
)

/**
 * Description:
 */
class StatementBinderWriter(
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
) {

    private fun FunSpec.Builder.loopModels(
        model: FieldModel,
        index: Int,
        modelName: String = "model",
    ): FunSpec.Builder = with(this) {
        when (model) {
            is ReferenceHolderModel -> {
                if (referencesCache.isTable(model)
                    || model.isColumnMap
                ) {
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
    ) = apply {
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
        if (model.name.nullable) {
            this.nextControlFlow("?: run")
            this.addStatement("statement.bindNull(%L)", index)
        }
        this.endControlFlow()
    }

    private fun FunSpec.Builder.writeSingleModel(
        model: FieldModel,
        modelName: String,
        index: Int
    ): FunSpec.Builder = apply {
        addCode(
            "%L.%L.%M(%L.%L",
            "Companion",
            model.propertyName,
            MemberNames.propertyBind,
            modelName,
            model.name.shortName,
        )
        if (model.hasTypeConverter(typeConverterCache)) {
            addCode(
                ") { statement.%M(%L, it) }",
                MemberNames.bind,
                index,
            )
        } else {
            addCode(",statement, %L)", index)
        }

        addCode("\n")
    }

    private val simpleWriter =
        TypeCreator<StatementModel, FunSpec> { (model, method, fieldsToLoop) ->
            FunSpec.builder(method.name)
                .apply {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter(ParameterSpec("statement", ClassNames.DatabaseStatement))
                    addParameter(ParameterSpec("model", model.classType))
                    fieldsToLoop.forEachIndexed { index, model ->
                        this.loopModels(model, index + 1)
                    }
                    if (model.implementsSQLiteStatementListener) {
                        addStatement("model.%L(statement)", method.statementListenerName)
                    }
                }
                .build()
        }


    val insertWriter = TypeCreator<ClassModel, FunSpec> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                method = Method.Insert,
                fieldsToLoop = model.fields,
            )
        )
    }

    val updateWriter = TypeCreator<ClassModel, FunSpec> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                method = Method.Update,
                fieldsToLoop = listOf(model.fields, model.primaryFields).flatten(),
            )
        )
    }

    val deleteWriter = TypeCreator<ClassModel, FunSpec> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                method = Method.Delete,
                fieldsToLoop = model.primaryFields,
            )
        )
    }
}