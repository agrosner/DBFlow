package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Description: Represents a field in a database for related adapters.
 * These generate helpers on the DB object directly.
 */
data class ClassAdapterFieldModel(
    val name: NameModel,
    private val typeName: ParameterizedTypeName,
) {
    /**
     * Guaranteed.
     */
    private val modelType = typeName.typeArguments[0]

    val adapterTypeName by lazy {
        return@lazy adapterType.parameterizedBy(associatedClassModel.classType)
    }

    val adapterType = typeName.rawType

    val type: Type = Type.values().first { it.className == adapterType }

    /**
     * Find model class name to keep track of.
     */
    lateinit var associatedClassModel: ClassModel

    /**
     * If the [ClassModel] approximately matches the type, store it here.
     */
    fun associateClassModel(classModel: ClassModel): Boolean {
        return (modelType == classModel.classType || // guessing!
            (classModel.classType.simpleName == modelType.toString())).also {
            if (it) {
                associatedClassModel = classModel
            }
        }
    }

    val associated
        get() = this::associatedClassModel.isInitialized

    enum class Type(val className: ClassName) {
        Normal(ClassNames.ModelAdapter),
        View(ClassNames.ViewAdapter),
        Query(ClassNames.QueryAdapter),
    }
}