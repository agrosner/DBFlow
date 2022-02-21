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
import com.dbflow5.codegen.shared.memberSeparator
import com.dbflow5.codegen.shared.references
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.TypeName

class QueryOpsWriter(
    private val referencesCache: ReferencesCache,
    private val typeConverterCache: TypeConverterCache,
) : TypeCreator<ClassModel, FunSpec> {

    override fun create(model: ClassModel): FunSpec {
        val adapters = model.distinctAdapterGetters(referencesCache)
        return FunSpec.builder(
            "${model.generatedFieldName}_queryOps",
        )
            .returns(ClassNames.queryOps(model.classType))
            .addModifiers(KModifier.PRIVATE)
            .apply {
                if (model.granularNotifications) {
                    addParameter(
                        "notifyDistributor", ClassNames.NotifyDistributor
                            .copy(nullable = true)
                    )
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
            .addCode(CodeBlock.builder()
                .apply {
                    add("return ")
                    addStatement("%T·{ cursor ->", ClassNames.QueryOpsImpl)
                    val constructorFields = model.fields
                        .filter { model.hasPrimaryConstructor || !it.isVal }
                    var currentIndex = -1
                    constructorFields
                        .forEach { field ->
                            currentIndex = loopField(field, model, currentIndex + 1)
                        }
                    constructModel(
                        model.classType,
                        model.hasPrimaryConstructor,
                        model.memberSeparator,
                        constructorFields,
                        model.implementsLoadFromCursorListener,
                        model,
                    )
                    addStatement("}")
                }
                .build())
            .build()
    }

    private fun CodeBlock.Builder.constructModel(
        classType: TypeName,
        hasPrimaryConstructor: Boolean,
        memberSeparator: String,
        constructorFields: List<FieldModel>,
        implementsLoadFromCursorListener: Boolean,
        model: ClassModel,
    ) {
        if (hasPrimaryConstructor) {
            addStatement("%T(", classType)
        } else {
            beginControlFlow("%T().apply", classType)
        }
        // all local vals get placed here.
        constructorFields.forEach { field ->
            add("\t")
            if (!hasPrimaryConstructor) {
                add("this.")
            }
            addStatement("%1N = %1N%2L", field.name.shortName, memberSeparator)
        }
        if (hasPrimaryConstructor) {
            add(")")
        } else {
            endControlFlow()
        }

        if (implementsLoadFromCursorListener) {
            addStatement(".also { it.onLoadFromCursor(cursor) } ")
        }
    }

    private fun CodeBlock.Builder.loopField(
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

    private fun CodeBlock.Builder.addComputedLoadStatement(
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
        add(
            "val %N = ",
            field.name.shortName,
        )
        constructModel(
            classType = field.nonNullClassType,
            hasPrimaryConstructor = true,
            memberSeparator = ",",
            constructorFields = references,
            implementsLoadFromCursorListener = false,
            model = model,
        )
        addStatement("")
        return returnIndex
    }

    private fun CodeBlock.Builder.addReferenceLoadStatement(
        field: ReferenceHolderModel,
        model: ClassModel,
        currentIndex: Int,
    ): Int {
        val reference = referencesCache.resolve(field)
        val zip = field.references(referencesCache).zip(
            field.references(referencesCache, field.name)
        )
        add("val %N = ", field.name.shortName)
        addStatement(
            "(%N().%M() where ",
            reference.generatedFieldName,
            MemberNames.select,
        )
        zip.forEachIndexed { index, (plain, _) ->
            add("\t\t")
            if (index > 0) {
                add("%L ", "and")
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

    private fun CodeBlock.Builder.addForeignKeyLoadStatement(
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

        add("val %N = ", field.name.shortName)
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
            add(
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
            add(",")
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
            add("\t\t")
            if (refIndex > 0) {
                add("%L ", "and")
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

    private fun CodeBlock.Builder.addSingleField(
        orderedCursorLookup: Boolean,
        field: FieldModel,
        currentIndex: Int,
        model: ClassModel,
    ): Int {
        add(
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

    private fun CodeBlock.Builder.addPlainFieldWithDefaults(
        orderedCursorLookup: Boolean,
        field: FieldModel,
        index: Int,
    ) {
        add("cursor")
        if (orderedCursorLookup) {
            add(", index = %L", index)
        }
        field.properties?.defaultValue?.let { value ->
            if (value.isNotBlank()) {
                add(", defValue = $value")
            }
        }
        add(")")
    }

    private fun CodeBlock.Builder.addTypeConverter(
        orderedCursorLookup: Boolean,
        index: Int
    ) {
        add(
            ") { dataProperty.%M(%N",
            MemberNames.infer,
            "cursor"
        )
        if (orderedCursorLookup) {
            add(", index = %L", index)
        }
        add(") }")
    }

    private fun CodeBlock.Builder.addEnumConstructor(
        orderedCursorLookup: Boolean,
        field: FieldModel,
        index: Int,
    ) {
        add("cursor, ")
        if (orderedCursorLookup) {
            add("index = %L,", index)
        }
        add("%T::valueOf)", field.classType)
    }
}