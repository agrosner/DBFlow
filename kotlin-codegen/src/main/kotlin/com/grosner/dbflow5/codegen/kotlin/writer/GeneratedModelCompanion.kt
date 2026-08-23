package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.distinctAdapterGetters
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

internal fun ClassModel.adapterSymbol(nameAllocator: NameAllocator): String =
    nameAllocator[generatedClassName]

internal fun adapterCompanionFileObject(classType: ClassName): TypeSpec =
    TypeSpec.objectBuilder(ClassNames.generatedAdapterCompanion(classType).simpleName)
        .addModifiers(KModifier.INTERNAL)
        .addSuperinterface(ClassNames.adapterCompanion(classType))
        .addProperty(adapterCompanionTableProperty(classType))
        .build()

internal fun companionOpsProperty(
    model: ClassModel,
    nameAllocator: NameAllocator,
    referencesCache: ReferencesCache,
): PropertySpec {
    val adapterGetters = model.distinctAdapterGetters(referencesCache)
    val adapterKey = nameAllocator[model.generatedClassName]
    val opsFunction = when {
        model.isNormal -> "${model.generatedFieldName}_ops"
        else -> "${model.generatedFieldName}_queryOps"
    }
    val opsType = when {
        model.isNormal -> ClassNames.tableOps(model.classType)
        else -> ClassNames.queryOps(model.classType)
    }
    return PropertySpec.builder(
        "${adapterKey}_companionOps",
        opsType,
    )
        .addModifiers(KModifier.INTERNAL)
        .initializer(
            CodeBlock.builder()
                .add("%N(", opsFunction)
                .apply {
                    adapterGetters.forEach { ref ->
                        val refClassType = (ref as ClassModel).classType
                        add(
                            "%N = { %T.Companion as %T }, ",
                            ref.generatedFieldName,
                            refClassType,
                            ref.generatedSuperClass,
                        )
                    }
                }
                .add(")")
                .build()
        )
        .build()
}

internal fun adapterFactoryCall(
    model: ClassModel,
    nameAllocator: NameAllocator,
    referencesCache: ReferencesCache,
): CodeBlock {
    val adapterGetters = model.distinctAdapterGetters(referencesCache)
    val adapterKey = nameAllocator[model.generatedClassName]
    val factoryMember = MemberName(
        model.name.packageName,
        "${adapterKey}_${model.adapterFactorySuffix()}",
    )
    return CodeBlock.builder()
        .add("%M(", factoryMember)
        .apply {
            adapterGetters.forEach { ref ->
                val refClassType = (ref as ClassModel).classType
                add(
                    "%NGetter = { %T.Companion as %T }, ",
                    ref.generatedFieldName,
                    refClassType,
                    ref.generatedSuperClass,
                )
            }
        }
        .add(")")
        .build()
}

internal fun junctionModelCompanion(
    classType: ClassName,
    adapterType: com.squareup.kotlinpoet.TypeName,
    adapterFactoryCall: CodeBlock,
): TypeSpec =
    TypeSpec.companionObjectBuilder()
        .addSuperinterface(ClassNames.adapterCompanion(classType))
        .addSuperinterface(adapterType, adapterFactoryCall)
        .addProperty(adapterCompanionTableProperty(classType))
        .build()

private fun adapterCompanionTableProperty(classType: ClassName): PropertySpec =
    PropertySpec.builder(
        "table",
        KClass::class.asClassName().parameterizedBy(classType),
    )
        .addModifiers(KModifier.OVERRIDE)
        .getter(
            FunSpec.getterBuilder()
                .addStatement("return %T::class", classType)
                .build()
        )
        .build()
