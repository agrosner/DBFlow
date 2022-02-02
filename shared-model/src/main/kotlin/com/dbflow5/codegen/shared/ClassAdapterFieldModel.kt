package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName

/**
 * Description: Represents a field in a database for related adapters.
 * These generate helpers on the DB object directly.
 */
data class ClassAdapterFieldModel(
    val name: NameModel,
    val typeName: ParameterizedTypeName,
) {
    /**
     * Guaranteed.
     */
    val modelType = typeName.typeArguments[0]

    val adapterType = typeName.rawType

    val type: Type = Type.values().first { it.className == adapterType }

    enum class Type(val className: ClassName) {
        Normal(ClassNames.ModelAdapter),
        View(ClassNames.ModelViewAdapter),
        Query(ClassNames.RetrievalAdapter),
    }
}