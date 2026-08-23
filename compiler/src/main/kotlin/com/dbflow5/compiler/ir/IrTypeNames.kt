package com.dbflow5.compiler.ir

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrStarProjection
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.types.Variance
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName

internal fun IrType.toPoetTypeName(): TypeName {
    val raw = classOrNull?.owner?.toPoetClassName()
        ?: classFqName?.let { ClassId.topLevel(it).toPoetClassName() }
        ?: ClassName("kotlin", "Any")
    val simple = this as? IrSimpleType
    val args = simple?.arguments.orEmpty().map { projection ->
        when (projection) {
            is IrStarProjection -> STAR
            is IrTypeProjection -> {
                val type = projection.type.toPoetTypeName()
                when (projection.variance) {
                    Variance.IN_VARIANCE -> WildcardTypeName.consumerOf(type)
                    Variance.OUT_VARIANCE -> WildcardTypeName.producerOf(type)
                    Variance.INVARIANT -> type
                }
            }
        }
    }
    val named = if (args.isEmpty()) raw else raw.parameterizedBy(args)
    return named.copy(nullable = isMarkedNullable())
}

internal fun IrType.toPoetClassName(): ClassName =
    (toPoetTypeName() as? ClassName)
        ?: classFqName?.let { ClassId.topLevel(it).toPoetClassName() }
        ?: ClassName("kotlin", "Any")

internal fun IrClass.fqNameOrThrow(): FqName =
    fqNameWhenAvailable ?: error("Missing FqName for ${name.asString()}")

internal fun IrClass.toPoetClassName(): ClassName {
    val id = classId ?: ClassId.topLevel(fqNameOrThrow())
    return id.toPoetClassName()
}

internal fun IrClass.poetPackageName(): String =
    (classId ?: ClassId.topLevel(fqNameOrThrow())).packageFqName.asString()

internal fun ClassId.toPoetClassName(): ClassName {
    val packageName = packageFqName.asString()
    val simpleNames = relativeClassName.pathSegments().map { it.asString() }
    return ClassName(packageName, simpleNames)
}
