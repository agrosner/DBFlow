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
import com.dbflow5.ksp.model.memberSeparator
import com.dbflow5.ksp.writer.TypeCreator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec

/**
 * Description:
 */
class LoadFromCursorWriter(
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
) : TypeCreator<ClassModel, FunSpec> {

    override fun create(model: ClassModel): FunSpec =
        FunSpec.builder("loadFromCursor")
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(
                    ParameterSpec("cursor", ClassNames.FlowCursor),
                )
                addParameter(
                    ParameterSpec("wrapper", ClassNames.DatabaseWrapper)
                )
                if (model.hasPrimaryConstructor) {
                    addCode("return %T(\n", model.classType)
                } else {
                    beginControlFlow("return %T().apply", model.classType)
                }
                model.fields
                    .filter { model.hasPrimaryConstructor || !it.isVal }
                    .forEach { field ->
                        when (field) {
                            is ReferenceHolderModel -> {
                                when (field.type) {
                                    ReferenceHolderModel.Type.ForeignKey -> {
                                        if (referencesCache.isTable(field)) {
                                            addForeignKeyLoadStatement(field, model)
                                        } else {
                                            addSingleField(field, model)
                                        }
                                    }
                                    ReferenceHolderModel.Type.Computed -> {
                                        // todo
                                    }
                                }
                            }
                            is SingleFieldModel -> addSingleField(field, model)
                        }

                    }
                if (model.hasPrimaryConstructor) {
                    addCode(")")
                } else {
                    endControlFlow()
                }
            }
            .build()

    private fun FunSpec.Builder.addForeignKeyLoadStatement(
        field: ReferenceHolderModel,
        model: ClassModel
    ) {
        addCode(
            "\t%N = ((%M %L %T::class) %L\n",
            field.name.shortName,
            MemberNames.select,
            MemberNames.from,
            field.nonNullClassType,
            MemberNames.where,
        )
        field.references(referencesCache).zip(
            field.references(referencesCache, field.name)
        ).forEachIndexed { index, (plain, referenced) ->
            addCode("\t\t")
            if (index > 0) {
                addCode("%L ", "and")
            }
            val className = field.nonNullClassType as ClassName
            addCode(
                "(%T.%L %L %L.%N.%M(",
                ClassName(
                    className.packageName,
                    className.simpleName + "_Table",
                ),
                plain.propertyName,
                MemberNames.eq,
                "Companion",
                referenced.propertyName,
                MemberNames.infer,
            )
            when {
                referenced.hasTypeConverter(typeConverterCache) -> {
                    addTypeConverter()
                }
                referenced.isEnum -> addEnumConstructor(referenced)
                else -> addPlainFieldWithDefaults(referenced)
            }
            addCode(")\n", model.memberSeparator)
        }
        addCode(
            "\t).%L(%N)%L\n",
            if (field.classType.isNullable)
                MemberNames.querySingle
            else MemberNames.requireSingle,
            "wrapper",
            model.memberSeparator,
        )
    }

    private fun FunSpec.Builder.addSingleField(
        field: FieldModel,
        model: ClassModel
    ) {
        addCode(
            "\t%N = %L.%N.%M(",
            field.name.shortName,
            "Companion",
            field.propertyName,
            MemberNames.infer,
        )
        when {
            field.hasTypeConverter(typeConverterCache) -> {
                addTypeConverter()
            }
            field.isEnum -> addEnumConstructor(field)
            else -> addPlainFieldWithDefaults(field)
        }
        addCode("%L\n", model.memberSeparator)
    }

    private fun FunSpec.Builder.addPlainFieldWithDefaults(field: FieldModel) {
        addCode("cursor")
        field.properties?.defaultValue?.let { value ->
            if (value.isNotBlank()) {
                addCode(",$value")
            }
        }
        addCode(")")
    }

    private fun FunSpec.Builder.addTypeConverter() {
        addCode(
            ") { dataProperty.%M(%N) }",
            MemberNames.infer,
            "cursor"
        )
    }

    private fun FunSpec.Builder.addEnumConstructor(
        field: FieldModel,
    ) {
        addCode(
            "cursor, %T::valueOf)", field.classType,
        )
    }
}