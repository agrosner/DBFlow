package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.copySeparator
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.NUMBER
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT

class AutoIncrementUpdateWriter(
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<ClassModel, PropertySpec> {

    override fun create(model: ClassModel): PropertySpec {
        val autoincrementFields = model.primaryAutoIncrementFields
        return PropertySpec.builder(
            "${model.generatedFieldName}_autoIncrementUpdater",
            ClassNames.autoIncrementUpdater(model.classType),
            KModifier.PRIVATE,
        )
            .apply {
                model.originatingSource?.let {
                    originatingFileTypeSpecAdder.addOriginatingFileType(this, it)
                }
            }
            .initializer(
                CodeBlock.builder()
                    .apply {
                        if (autoincrementFields.isNotEmpty()) {
                            beginControlFlow("%T", ClassNames.autoIncrementUpdater(model.classType))
                            add("id -> ")
                            if (model.isDataClass) {
                                addStatement("copy(")
                            } else {
                                beginControlFlow("apply")
                            }

                            autoincrementFields.forEach { field ->
                                if (!model.isDataClass) {
                                    add("this.")
                                }
                                addStatement(
                                    "%L = %L.%L()%L",
                                    field.name.shortName,
                                    "id",
                                    when (field.classType) {
                                        INT -> "toInt"
                                        DOUBLE -> "toDouble"
                                        FLOAT -> "toFloat"
                                        NUMBER -> ""
                                        BYTE -> "toByte"
                                        LONG -> "toLong"
                                        SHORT -> "toShort"
                                        else -> throw IllegalArgumentException(
                                            "Invalid auto primary key type ${field.classType}." +
                                                "could not turn into a number. ${model.name.shortName}:${field.name.shortName}"
                                        )
                                    },
                                    model.copySeparator
                                )
                            }

                            if (model.isDataClass) {
                                addStatement(")")
                            } else {
                                endControlFlow()
                            }
                            endControlFlow()
                        } else {
                            addStatement("%M()", MemberNames.emptyAutoIncrementUpdater)
                        }
                    }
                    .build()
            )
            .build()
    }
}