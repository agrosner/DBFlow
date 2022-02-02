package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName

object ClassNames {

    fun property(typeName: TypeName) = ClassName(PackageNames.Property, "Property")
        .parameterizedBy(typeName)

    fun typeConvertedProperty(
        dataTypeName: TypeName,
        modelTypeName: TypeName,
    ) = ClassName(PackageNames.Property, "TypeConvertedProperty")
        .parameterizedBy(dataTypeName, modelTypeName)

    fun indexProperty(
        tableTypeName: TypeName,
    ) = ClassName(
        PackageNames.Property,
        "IndexProperty"
    )
        .parameterizedBy(tableTypeName)

    val IProperty = ClassName(PackageNames.Property, "IProperty")
        .parameterizedBy(
            WildcardTypeName.Companion.producerOf(
                Any::class.asTypeName().copy(nullable = true)
            )
        )
    val OperatorGroup = ClassName(PackageNames.Query, "OperatorGroup")

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