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
import com.squareup.kotlinpoet.ParameterizedTypeName

/**
 * Description:
 */
class LoadFromCursorWriter(
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
) : TypeCreator<ClassModel, FunSpec> {

    override fun create(model: ClassModel): FunSpec =
        FunSpec.builder("loadFromCursor")
            .returns(model.classType)
            .apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(
                    ParameterSpec("cursor", ClassNames.FlowCursor),
                )
                addParameter(
                    ParameterSpec("wrapper", ClassNames.DatabaseWrapper)
                )
                val constructorFields = model.fields
                    .filter { model.hasPrimaryConstructor || !it.isVal }
                constructorFields
                    .forEach { field ->
                        when (field) {
                            is ReferenceHolderModel -> {
                                when (field.type) {
                                    ReferenceHolderModel.Type.ForeignKey -> {
                                        if (referencesCache.isTable(field)) {
                                            addForeignKeyLoadStatement(field)
                                        } else {
                                            addSingleField(field)
                                        }
                                    }
                                    ReferenceHolderModel.Type.Computed -> {
                                        // todo
                                    }
                                    ReferenceHolderModel.Type.Reference -> {
                                        addReferenceLoadStatement(field, model)
                                    }
                                }
                            }
                            is SingleFieldModel -> addSingleField(field)
                        }

                    }
                if (model.hasPrimaryConstructor) {
                    addCode("return %T(\n", model.classType)
                } else {
                    beginControlFlow("return %T().apply", model.classType)
                }
                // all local vals get placed here.
                constructorFields.forEach { field ->
                    addCode("\t")
                    if (!model.hasPrimaryConstructor) {
                        addCode("this.")
                    }
                    addStatement("%1N = %1N%2L", field.name.shortName, model.memberSeparator)
                }
                if (model.hasPrimaryConstructor) {
                    addCode(")")
                } else {
                    endControlFlow()
                }
            }
            .build()

    private fun FunSpec.Builder.addReferenceLoadStatement(
        field: ReferenceHolderModel,
        model: ClassModel
    ) {
        val childTableType = (field.nonNullClassType as ParameterizedTypeName).typeArguments[0]
            as ClassName
        addStatement(
            "val %N = ((%M %L %T::class) %L ",
            field.name.shortName,
            MemberNames.select,
            MemberNames.from,
            childTableType, // hack since list
            MemberNames.where,
        )
        field.references(referencesCache).zip(
            field.references(referencesCache, field.name)
        ).forEachIndexed { index, (plain, referenced) ->
            addCode("\t\t")
            if (index > 0) {
                addCode("%L ", "and")
            }
            addStatement(
                "(%T.%L %L %L)",
                ClassName(
                    childTableType.packageName,
                    "${childTableType.simpleName}_Table",
                ),
                plain.propertyName,
                MemberNames.eq,
                (model.fields[0] as ReferenceHolderModel)
                    .references(referencesCache, model.fields[0].name)[0]
                    .accessName(), // dirty hack to grab referenced.
            )
        }
        addStatement(
            "\t).%L(%N)",
            MemberNames.queryList,
            "wrapper"
        )
    }

    private fun FunSpec.Builder.addForeignKeyLoadStatement(
        field: ReferenceHolderModel
    ) {
        addStatement(
            "val %N = ((%M %L %T::class) %L",
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
            val className = field.name
            addCode(
                "(%T.%L %L %L.%N.%M(",
                ClassName(
                    className.packageName,
                    "${field.ksClassType.declaration.simpleName.getShortName()}_Table",
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
            addStatement(")")
        }
        addStatement(
            "\t).%L(%N)",
            if (field.classType.isNullable)
                MemberNames.querySingle
            else MemberNames.requireSingle,
            "wrapper",
        )
    }

    private fun FunSpec.Builder.addSingleField(
        field: FieldModel
    ) {
        addCode(
            "val %N = %L.%N.%M(",
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
        addStatement("")
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