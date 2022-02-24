package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.ClassName
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

    fun primaryModelClauseGetter(
        tableTypeName: TypeName,
    ) = ClassName(PackageNames.Adapter2, "PrimaryModelClauseGetter")
        .parameterizedBy(tableTypeName)

    fun autoIncrementUpdater(
        tableTypeName: TypeName,
    ) = ClassName(PackageNames.Adapter2, "AutoIncrementUpdater")
        .parameterizedBy(tableTypeName)

    fun dbCreator(databaseTypeName: TypeName) = ClassName(PackageNames.DatabaseConfig, "DBCreator")
        .parameterizedBy(databaseTypeName)

    val QueryOpsImpl = ClassName(PackageNames.Adapter2, "QueryOpsImpl")
    val NotifyDistributor = ClassName(PackageNames.Runtime, "NotifyDistributor")
    val OperatorGroup = ClassName(PackageNames.QueryOperations, "OperatorGroup")
    val Operation = ClassName(PackageNames.QueryOperations, "Operation")

    val ModelAdapter = ClassName(PackageNames.Adapter, "ModelAdapter")
    val ModelAdapter2 = ClassName(PackageNames.Adapter2, "ModelAdapter")
    val ViewAdapter2 = ClassName(PackageNames.Adapter2, "ViewAdapter")
    val QueryAdapter2 = ClassName(PackageNames.Adapter2, "QueryAdapter")
    fun modelAdapter2(tableTypeName: TypeName) = ClassName(PackageNames.Adapter2, "ModelAdapter")
        .parameterizedBy(tableTypeName)

    fun queryAdapter2(queryTypeName: TypeName) = ClassName(PackageNames.Adapter2, "QueryAdapter")
        .parameterizedBy(queryTypeName)

    fun viewAdapter2(viewTypeName: TypeName) = ClassName(PackageNames.Adapter2, "ViewAdapter")
        .parameterizedBy(viewTypeName)

    fun tableOps(tableTypeName: TypeName) = ClassName(PackageNames.Adapter2, "TableOps")
        .parameterizedBy(tableTypeName)

    val TableOpsImpl = ClassName(PackageNames.Adapter2, "TableOpsImpl")

    fun adapterCompanion(typeName: TypeName) =
        ClassName(PackageNames.Adapter, "AdapterCompanion")
            .parameterizedBy(typeName)

    fun dbScope(dbTypeName: TypeName) =
        ClassName("${PackageNames.Database}.scope", "DatabaseScope")
            .parameterizedBy(dbTypeName)

    fun tableBinder(tableTypeName: TypeName) = ClassName(PackageNames.Adapter2, "TableBinder")
        .parameterizedBy(tableTypeName)

    fun propertyGetter(tableTypeName: TypeName) = ClassName(PackageNames.Adapter2, "PropertyGetter")
        .parameterizedBy(tableTypeName)

    fun queryOps(classType: ClassName) = ClassName(
        PackageNames.Adapter2,
        "QueryOps"
    ).parameterizedBy(classType)

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
    val DatabaseStatementListener = ClassName(PackageNames.Query, "DatabaseStatementListener")
    fun databaseStatementListenerType(enumName: String) =
        DatabaseStatementListener.nestedClass("Type")
            .nestedClass(enumName)

    val AndroidNonNull = ClassName("android.support.annotation", "NonNull")
    val AndroidXNonNull = ClassName("androidx.annotation", "NonNull")

    val TableSQL = ClassName(PackageNames.Adapter2, "TableSQL")
    val CompilableQuery = ClassName(PackageNames.Adapter2, "CompilableQuery")

    val DBSettings = ClassName(PackageNames.DatabaseConfig, "DBSettings")
}