package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.ReferenceHolderModel
import com.dbflow5.codegen.shared.SingleFieldModel
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.distinctAdapterGetters
import com.dbflow5.codegen.shared.hasTypeConverter
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.memberSeparator
import com.dbflow5.codegen.shared.references
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeName

class QueryOpsWriter(
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<ClassModel, FunSpec> {

    override fun create(model: ClassModel): FunSpec {
        val adapters =
            model.distinctAdapterGetters(referencesCache, includeViewClassAdapters = false)
        return FunSpec.builder(
            "${model.generatedFieldName}_queryOps",
        )
            .returns(ClassNames.queryOps(model.classType))
            .addModifiers(KModifier.PRIVATE)
            .apply {
                model.originatingSource?.let {
                    originatingFileTypeSpecAdder.addOriginatingFileType(this, it)
                }
                adapters.forEach { adapter ->
                    addParameter(
                        adapter.generatedFieldName,
                        LambdaTypeName.get(
                            returnType = adapter.generatedSuperClass
                        )
                    )
                }
            }
            .apply {
                addCode("return %T·{ cursor ->\n", ClassNames.QueryOpsImpl)
                val constructorFields = model.fields
                    .filter { model.hasImmutableConstructor || model.isDataClass || !it.isVal }
                var currentIndex = -1
                constructorFields
                    .forEach { field ->
                        currentIndex = loopField(field, model, currentIndex + 1)
                    }
                constructModel(
                    model.classType,
                    model.hasImmutableConstructor || model.isDataClass,
                    model.memberSeparator,
                    constructorFields,
                    model.implementsLoadFromCursorListener,
                )
                addCode("}\n")
            }
            .build()
    }

    private fun FunSpec.Builder.constructModel(
        classType: TypeName,
        canConstructWithFields: Boolean,
        memberSeparator: String,
        constructorFields: List<FieldModel>,
        implementsLoadFromCursorListener: Boolean,
    ) {
        if (canConstructWithFields) {
            addStatement("%T(", classType)
        } else {
            beginControlFlow("%T().apply", classType)
        }
        // all local vals get placed here.
        constructorFields.forEach { field ->
            addCode("\t")
            if (!canConstructWithFields) {
                addCode("this.")
            }
            addStatement("%1N = %1N%2L", field.name.shortName, memberSeparator)
        }
        if (canConstructWithFields) {
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
    ): Int {
        val orderedCursorLookup = model.properties.orderedCursorLookup
        return when (field) {
            is ReferenceHolderModel -> {
                when (field.type) {
                    ReferenceHolderModel.Type.ForeignKey -> {
                        if (referencesCache.isTable(field)) {
                            addForeignKeyLoadStatement(
                                orderedCursorLookup,
                                field,
                                index,
                                model,
                            )
                        } else {
                            addSingleField(orderedCursorLookup, field, index, model)
                        }
                    }
                    ReferenceHolderModel.Type.Computed -> {
                        addComputedLoadStatement(field, model, index)
                    }
                    ReferenceHolderModel.Type.Reference -> {
                        addReferenceLoadStatement(field, model, index)
                    }
                }
            }
            is SingleFieldModel -> addSingleField(
                orderedCursorLookup = orderedCursorLookup,
                field, index,
                model,
            )
        }
    }

    private fun FunSpec.Builder.addComputedLoadStatement(
        field: ReferenceHolderModel,
        model: ClassModel,
        index: Int,
    ): Int {
        val references = field.references(referencesCache)
        var returnIndex = index
        references.zip(
            field.references(referencesCache, field.name)
        ).forEachIndexed { index, (plain, referenced) ->
            returnIndex = loopField(
                plain,
                model,
                returnIndex + index,
            )
        }
        addCode(
            "val %N = ",
            field.name.shortName,
        )
        constructModel(
            classType = field.nonNullClassType,
            canConstructWithFields = true,
            memberSeparator = ",",
            constructorFields = references,
            implementsLoadFromCursorListener = false,
        )
        addStatement("")
        return returnIndex
    }

    private fun FunSpec.Builder.addReferenceLoadStatement(
        field: ReferenceHolderModel,
        model: ClassModel,
        currentIndex: Int,
    ): Int {
        val reference = referencesCache.resolve(field)
        val zip = field.references(referencesCache).zip(
            field.references(referencesCache, field.name)
        )
        addCode("val %N = ", field.name.shortName)
        addStatement(
            "(%N().%M() where ",
            reference.generatedFieldName,
            MemberNames.select,
        )
        zip.forEachIndexed { index, (plain, _) ->
            addCode("\t\t")
            if (index > 0) {
                addCode("%L ", "and")
            }
            addStatement(
                "(%T.%L %L %L)",
                ClassName(
                    reference.classType.packageName,
                    "${reference.classType.simpleName}_Table",
                ),
                plain.propertyName,
                MemberNames.eq,
                (model.fields[0] as ReferenceHolderModel)
                    .references(referencesCache, model.fields[0].name)[0]
                    .accessName(), // dirty hack to grab referenced.
            )
        }
        addStatement(
            "\t).%M(%L)",
            MemberNames.list,
            "this"
        )
        return (zip.size - 1) + currentIndex
    }

    private fun FunSpec.Builder.addForeignKeyLoadStatement(
        orderedCursorLookup: Boolean,
        field: ReferenceHolderModel,
        currentIndex: Int,
        model: ClassModel,
    ): Int {
        val className = field.name
        val generatedTableClassName = ClassName(
            className.packageName,
            "${field.ksClassType.declaration.simpleName.shortName}_Table",
        )
        val zip = field.references(referencesCache).zip(
            field.references(referencesCache, field.name)
        )
        val resolvedField = referencesCache.resolve(field)

        addCode("val %N = ", field.name.shortName)
        // join references here
        val templateRefs = zip.joinToString { "%L" }
        addStatement(
            "%M(", if (field.name.nullable) {
                MemberNames.safeLet
            } else {
                MemberNames.let
            }
        )
        zip.mapIndexed { refIndex, (_, referenced) ->
            addCode(
                "%T.%N.%M(",
                model.generatedClassName.className,
                referenced.propertyName,
                MemberNames.infer,
            )
            when {
                referenced.hasTypeConverter(typeConverterCache) -> {
                    addTypeConverter(orderedCursorLookup, currentIndex + refIndex)
                }
                referenced.isEnum -> addEnumConstructor(
                    orderedCursorLookup,
                    referenced,
                    currentIndex + refIndex
                )
                else -> addPlainFieldWithDefaults(
                    orderedCursorLookup,
                    referenced,
                    currentIndex + refIndex
                )
            }
            addCode(",")
        }
        addStatement(
            ") { $templateRefs ->",
            *zip.map { (_, ref) -> ref.propertyName }.toTypedArray(),
        )
        addStatement(
            "(%N().%M() where",
            resolvedField.generatedFieldName,
            MemberNames.select,
        )

        zip.forEachIndexed { refIndex, (plain, referenced) ->
            addCode("\t\t")
            if (refIndex > 0) {
                addCode("%L ", "and")
            }
            addStatement(
                "(%T.%L %L %L)",
                generatedTableClassName,
                plain.propertyName,
                MemberNames.eq,
                referenced.propertyName,
            )
        }
        addStatement(
            "\t).%M(%L)",
            if (field.classType.isNullable)
                MemberNames.singleOrNull
            else MemberNames.single,
            "this",
        )
        addStatement("}")
        return currentIndex + (zip.size - 1)
    }

    private fun FunSpec.Builder.addSingleField(
        orderedCursorLookup: Boolean,
        field: FieldModel,
        currentIndex: Int,
        model: ClassModel,
    ): Int {
        addCode(
            "val %N = %T.%N.%M(",
            field.name.shortName,
            model.generatedClassName.className,
            field.propertyName,
            MemberNames.infer,
        )
        when {
            field.hasTypeConverter(typeConverterCache) -> {
                addTypeConverter(orderedCursorLookup, currentIndex)
            }
            field.isEnum -> addEnumConstructor(orderedCursorLookup, field, currentIndex)
            else -> addPlainFieldWithDefaults(orderedCursorLookup, field, currentIndex)
        }
        addStatement("")
        return currentIndex
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