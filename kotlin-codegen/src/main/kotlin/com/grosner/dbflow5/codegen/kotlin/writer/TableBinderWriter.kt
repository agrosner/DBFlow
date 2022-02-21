package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.ReferenceHolderModel
import com.dbflow5.codegen.shared.SingleFieldModel
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.hasTypeConverter
import com.dbflow5.codegen.shared.references
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

private sealed interface Method {
    val name: String
    val statementListenerName: String

    object Insert : Method {
        override val name: String = "insert"
        override val statementListenerName: String = "onBindToInsertStatement"
    }

    object Update : Method {
        override val name: String = "update"
        override val statementListenerName: String = "onBindToUpdateStatement"
    }

    object Delete : Method {
        override val name: String = "delete"
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
class TableBinderWriter(
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
) : TypeCreator<ClassModel, PropertySpec> {

    override fun create(model: ClassModel): PropertySpec =
        PropertySpec.builder(
            "${model.generatedFieldName}_tableBinder",
            ClassNames.tableBinder(model.classType),
            KModifier.PRIVATE,
        )
            .initializer(
                CodeBlock.builder()
                    .addStatement("%T(", ClassNames.tableBinder(model.classType))
                    .add("%L\n,", insertWriter.create(model))
                    .add("%L\n,", updateWriter.create(model))
                    .add("%L\n", deleteWriter.create(model))
                    .addStatement(")")
                    .build()
            )
            .build()

    private fun CodeBlock.Builder.loopModels(
        model: FieldModel,
        classModel: ClassModel,
        index: Int,
        modelName: String = "model",
    ): Int = with(this) {
        when (model) {
            is ReferenceHolderModel -> {
                if (referencesCache.isTable(model)
                    || model.isColumnMap
                ) {
                    writeReferenceModel(
                        modelName, model,
                        classModel, index
                    )
                } else {
                    writeSingleModel(model, classModel, modelName, index)
                }
            }
            is SingleFieldModel -> {
                writeSingleModel(model, classModel, modelName, index)
            }
        }
    }

    private fun CodeBlock.Builder.writeReferenceModel(
        modelName: String,
        model: ReferenceHolderModel,
        classModel: ClassModel,
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
                    model = reference,
                    classModel = classModel,
                    index = currentIndex + 1,
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

    private fun CodeBlock.Builder.writeSingleModel(
        model: FieldModel,
        classModel: ClassModel,
        modelName: String,
        index: Int
    ): Int {
        add(
            "%L.%L.%M(%L.%L",
            classModel.generatedClassName.className,
            model.propertyName,
            MemberNames.propertyBind,
            modelName,
            model.name.shortName,
        )
        if (model.hasTypeConverter(typeConverterCache)) {
            add(
                ") { statement.%M(%L, it) }",
                MemberNames.bind,
                index,
            )
        } else {
            add(",statement, %L)", index)
        }

        add("\n")
        return index
    }

    private val simpleWriter =
        TypeCreator<StatementModel, CodeBlock> { (model, method, fieldsToLoop) ->
            CodeBlock.builder()
                .apply {
                    addStatement("${method.name} = { statement, model -> ")
                    var currentIndex = 0
                    fieldsToLoop.forEach { field ->
                        currentIndex = this.loopModels(
                            field,
                            model, currentIndex + 1
                        )
                    }
                    if (model.implementsSQLiteStatementListener) {
                        addStatement("model.%L(statement)", method.statementListenerName)
                    }
                    addStatement("}")
                }
                .build()
        }


    private val insertWriter = TypeCreator<ClassModel, CodeBlock> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                method = Method.Insert,
                fieldsToLoop = model.fields,
            )
        )
    }

    private val updateWriter = TypeCreator<ClassModel, CodeBlock> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                method = Method.Update,
                fieldsToLoop = listOf(model.fields, model.primaryFields).flatten(),
            )
        )
    }

    private val deleteWriter = TypeCreator<ClassModel, CodeBlock> { model ->
        simpleWriter.create(
            StatementModel(
                classModel = model,
                method = Method.Delete,
                fieldsToLoop = model.primaryFields,
            )
        )
    }
}