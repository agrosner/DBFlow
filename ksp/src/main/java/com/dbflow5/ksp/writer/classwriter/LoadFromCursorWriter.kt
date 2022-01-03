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
import com.squareup.kotlinpoet.TypeName

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
                    .forEachIndexed { index, field -> loopField(field, model, index) }
                addCode("return ")
                constructModel(
                    model.classType,
                    model.hasPrimaryConstructor,
                    model.memberSeparator,
                    constructorFields,
                    model.implementsLoadFromCursorListener,
                )
            }
            .build()

    private fun FunSpec.Builder.constructModel(
        classType: TypeName,
        hasPrimaryConstructor: Boolean,
        memberSeparator: String,
        constructorFields: List<FieldModel>,
        implementsLoadFromCursorListener: Boolean,
    ) {
        if (hasPrimaryConstructor) {
            addCode("%T(\n", classType)
        } else {
            beginControlFlow("%T().apply", classType)
        }
        // all local vals get placed here.
        constructorFields.forEach { field ->
            addCode("\t")
            if (!hasPrimaryConstructor) {
                addCode("this.")
            }
            addStatement("%1N = %1N%2L", field.name.shortName, memberSeparator)
        }
        if (hasPrimaryConstructor) {
            addCode(")")
        } else {
            endControlFlow()
        }

        if (implementsLoadFromCursorListener) {
            addStatement(".also { it.onLoadFromCursor(cursor) } ")
        }
    }

    private fun FunSpec.Builder.loopField(
        field: FieldModel,
        model: ClassModel,
        index: Int,
    ) {
        val orderedCursorLookup = model.properties.orderedCursorLookup
        when (field) {
            is ReferenceHolderModel -> {
                when (field.type) {
                    ReferenceHolderModel.Type.ForeignKey -> {
                        if (referencesCache.isTable(field)) {
                            addForeignKeyLoadStatement(
                                orderedCursorLookup,
                                field,
                                index
                            )
                        } else {
                            addSingleField(orderedCursorLookup, field, index)
                        }
                    }
                    ReferenceHolderModel.Type.Computed -> {
                        addComputedLoadStatement(field, model)
                    }
                    ReferenceHolderModel.Type.Reference -> {
                        addReferenceLoadStatement(field, model)
                    }
                }
            }
            is SingleFieldModel -> addSingleField(
                orderedCursorLookup = orderedCursorLookup,
                field, index
            )
        }
    }

    private fun FunSpec.Builder.addComputedLoadStatement(
        field: ReferenceHolderModel,
        model: ClassModel
    ) {
        val references = field.references(referencesCache)
        references.zip(
            field.references(referencesCache, field.name)
        ).forEachIndexed { index, (plain, referenced) ->
            loopField(
                plain,
                model,
                index,
            )
        }
        addCode(
            "val %N = ",
            field.name.shortName,
        )
        constructModel(
            classType = field.nonNullClassType,
            hasPrimaryConstructor = true,
            memberSeparator = ",",
            constructorFields = references,
            implementsLoadFromCursorListener = false,
        )
        addStatement("")
    }

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
        orderedCursorLookup: Boolean,
        field: ReferenceHolderModel,
        index: Int,
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
        ).forEachIndexed { refIndex, (plain, referenced) ->
            addCode("\t\t")
            if (refIndex > 0) {
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
                    addTypeConverter(orderedCursorLookup, index + refIndex)
                }
                referenced.isEnum -> addEnumConstructor(
                    orderedCursorLookup,
                    referenced,
                    index + refIndex
                )
                else -> addPlainFieldWithDefaults(orderedCursorLookup, referenced, index + refIndex)
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
        orderedCursorLookup: Boolean,
        field: FieldModel,
        index: Int,
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
                addTypeConverter(orderedCursorLookup, index)
            }
            field.isEnum -> addEnumConstructor(orderedCursorLookup, field, index)
            else -> addPlainFieldWithDefaults(orderedCursorLookup, field, index)
        }
        addStatement("")
    }

    private fun FunSpec.Builder.addPlainFieldWithDefaults(
        orderedCursorLookup: Boolean,
        field: FieldModel,
        index: Int,
    ) {
        addCode("cursor")
        if (orderedCursorLookup) {
            addCode(", index = %L", index)
        }
        field.properties?.defaultValue?.let { value ->
            if (value.isNotBlank()) {
                addCode(", defValue = $value")
            }
        }
        addCode(")")
    }

    private fun FunSpec.Builder.addTypeConverter(
        orderedCursorLookup: Boolean,
        index: Int
    ) {
        addCode(
            ") { dataProperty.%M(%N",
            MemberNames.infer,
            "cursor"
        )
        if (orderedCursorLookup) {
            addCode(", index = %L", index)
        }
        addCode(") }")
    }

    private fun FunSpec.Builder.addEnumConstructor(
        orderedCursorLookup: Boolean,
        field: FieldModel,
        index: Int,
    ) {
        addCode("cursor, ")
        if (orderedCursorLookup) {
            addCode("index = %L,", index)
        }
        addCode("%T::valueOf)", field.classType)
    }
}