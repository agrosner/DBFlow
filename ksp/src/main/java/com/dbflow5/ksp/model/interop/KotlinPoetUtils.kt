package com.dbflow5.ksp.model.interop

import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName


fun KSTypeReference.patchErrorTypeName(): TypeName {
    val resolve = resolve()
    return element
        ?.takeIf { it is KSClassifierReference }
        ?.let { element ->
            val typeArguments = (element as KSClassifierReference).typeArguments
            val packageName = resolve.declaration.getNormalizedPackageName()
            val qualifiedName =
                resolve.declaration.qualifiedName?.asString() ?: element.referencedName()
            val shortNames = if (packageName.isBlank()) {
                qualifiedName
            } else {
                qualifiedName.substring(packageName.length + 1)
            }.split(".")
            val className = ClassName(
                packageName,
                shortNames.first(), *(shortNames.drop(1).toTypedArray()),
            )
            if (typeArguments.isNotEmpty()) {
                className.parameterizedBy(typeArguments.mapNotNull {
                    when (it.variance) {
                        Variance.STAR -> it.toTypeName()
                        else -> it.type?.patchErrorTypeName()
                    }
                })
            } else {
                className
            }
        } ?: toTypeName()
}

/**
 * Root package comes as <root> instead of "" so we work around it here.
 */
internal fun KSDeclaration.getNormalizedPackageName(): String {
    return packageName.asString().let {
        if (it == "<root>") {
            ""
        } else {
            it
        }
    }
}