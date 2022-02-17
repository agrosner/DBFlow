package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

object ClassNames {

    fun property(
        valueTypeName: TypeName,
        tableTypeName: TypeName
    ) = ClassName(PackageNames.QueryOperations, "Property")
        .parameterizedBy(valueTypeName, tableTypeName)

    fun propertyStart(
        valueTypeName: TypeName,
        tableTypeName: TypeName
    ) = ClassName(PackageNames.QueryOperations, "PropertyStart")
        .parameterizedBy(valueTypeName, tableTypeName)

    fun typeConvertedProperty(
        dataTypeName: TypeName,
        modelTypeName: TypeName,
        tableTypeName: TypeName,
    ) = ClassName(PackageNames.QueryOperations, "TypeConvertedProperty")
        .parameterizedBy(modelTypeName, dataTypeName, tableTypeName)

    fun indexProperty(
        tableTypeName: TypeName,
    ) = ClassName(
        PackageNames.QueryOperations,
        "IndexProperty"
    )
        .parameterizedBy(tableTypeName)

    val OperatorGroup = ClassName(PackageNames.QueryOperations, "OperatorGroup")
    val Operation = ClassName(PackageNames.QueryOperations, "Operation")

    val ModelAdapter = ClassName(PackageNames.Adapter, "ModelAdapter")
    fun modelAdapter(typeName: TypeName) = ModelAdapter.parameterizedBy(typeName)
    val RetrievalAdapter = ClassName(PackageNames.Adapter, "RetrievalAdapter")
    fun retrievalAdapter(typeName: TypeName): ParameterizedTypeName {
        return RetrievalAdapter.parameterizedBy(typeName)
    }

    val ModelViewAdapter = ClassName(PackageNames.Adapter, "ModelViewAdapter")
    fun modelViewAdapter(typeName: TypeName) = ModelViewAdapter.parameterizedBy(typeName)

    fun adapterCompanion(typeName: TypeName) =
        ClassName(PackageNames.Adapter, "AdapterCompanion")
            .parameterizedBy(typeName)

    fun dbScope(dbTypeName: TypeName) =
        ClassName("${PackageNames.Database}.scope", "DatabaseScope")
            .parameterizedBy(dbTypeName)

    val DBFlowDatabase = ClassName(PackageNames.Config, "DBFlowDatabase")
    val GeneratedDatabaseHolderFactory =
        ClassName(PackageNames.Config, "GeneratedDatabaseHolderFactory")
    val DatabaseHolderFactory = ClassName(PackageNames.Config, "DatabaseHolderFactory")
    val DatabaseHolder = ClassName(PackageNames.Config, "DatabaseHolder")

    val FlowCursor = ClassName(PackageNames.Database, "FlowCursor")
    val DatabaseWrapper = ClassName(PackageNames.Database, "DatabaseWrapper")

    val DatabaseStatement = ClassName(PackageNames.Database, "DatabaseStatement")

    val ObjectType = ClassName(PackageNames.Adapter, "ObjectType")
    val TypeConverter = ClassName(PackageNames.Converter, "TypeConverter")
    val LoadFromCursorListener = ClassName(PackageNames.Query, "LoadFromCursorListener")
    val SQLiteStatementListener = ClassName(PackageNames.Query, "SQLiteStatementListener")

    val AndroidNonNull = ClassName("android.support.annotation", "NonNull")
    val AndroidXNonNull = ClassName("androidx.annotation", "NonNull")
}