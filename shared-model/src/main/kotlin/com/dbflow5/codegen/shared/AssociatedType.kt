package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.javapoet.JTypeName
import com.squareup.kotlinpoet.javapoet.KTypeName

data class AssociatedType(
    val kTypeName: KTypeName,
    val jTypeName: JTypeName,
) {
    operator fun contains(typeName: KTypeName): Boolean {
        val typeString = typeName.toString()
        return kTypeName.toString() == typeString
            || jTypeName.toString() == typeString
            || jTypeName.box().toString() == typeString
    }
}
