package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassNames
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

internal fun adapterCompanionObject(classType: ClassName): TypeSpec =
    TypeSpec.companionObjectBuilder()
        .addSuperinterface(ClassNames.adapterCompanion(classType))
        .addProperty(adapterCompanionTableProperty(classType))
        .build()

/**
 * File-level stand-in so generated helpers do not reference `Model.Companion`,
 * which is FIR-only and invisible when `*_Adapter.kt` compiles later.
 */
internal fun adapterCompanionFileObject(classType: ClassName): TypeSpec =
    TypeSpec.objectBuilder(ClassNames.generatedAdapterCompanion(classType).simpleName)
        .addModifiers(KModifier.INTERNAL)
        .addSuperinterface(ClassNames.adapterCompanion(classType))
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
