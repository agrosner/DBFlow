package com.grosner.dbflow5.codegen.kotlin.writer.classwriter

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.ReferenceHolderModel
import com.dbflow5.codegen.shared.SingleFieldModel
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.hasTypeConverter
import com.dbflow5.codegen.shared.references
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.ksp.MemberNames
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
    ): Int = with(this) {
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
    ): Int {
        // foreign key has nesting logic
        this.beginControlFlow(
            "%L.%L.let { m -> ",
            modelName,
            model.accessName(useLastNull = true),
        )

        val references = model.references(
            referencesCache,
            nameToNest = model.name
        )
        var currentIndex = index - 1
        references
            .forEach { reference ->
                currentIndex = loopModels(
                    model = reference, index = currentIndex + 1,
                    modelName = "m"
                )
            }
        if (model.name.nullable) {
            this.nextControlFlow("?: run")
            this.addStatement("statement.bindNull(%L)", index)
        }
        this.endControlFlow()
        return currentIndex
    }

    private fun FunSpec.Builder.writeSingleModel(
        model: FieldModel,
        modelName: String,
        index: Int
    ): Int {
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
        return index
    }

    private val simpleWriter =
        TypeCreator<StatementModel, FunSpec> { (model, method, fieldsToLoop) ->
            FunSpec.builder(method.name)
                .apply {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter(ParameterSpec("statement", ClassNames.DatabaseStatement))
                    addParameter(ParameterSpec("model", model.classType))
                    var currentIndex = 0
                    fieldsToLoop.forEach { model ->
                        currentIndex = this.loopModels(model, currentIndex + 1)
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