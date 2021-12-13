package com.dbflow5.ksp

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object PackageNames {
    const val Core = "com.dbflow5"
    const val Query = "com.dbflow5.query"
    const val Property = "com.dbflow5.query.property"
    const val Adapter = "com.dbflow5.adapter"
    const val Config = "com.dbflow5.config"
    const val Database = "com.dbflow5.database"
}

object ClassNames {


    val Property = ClassName(PackageNames.Property, "Property")
    val IProperty = ClassName(PackageNames.Property, "IProperty")
        .parameterizedBy(WildcardTypeName.producerOf(Any::class.asTypeName().copy(nullable = true)))
    val OperatorGroup = ClassName(PackageNames.Query, "OperatorGroup")
    fun modelAdapter(typeName: TypeName) =
        ClassName(PackageNames.Adapter, "ModelAdapter").parameterizedBy(typeName)

    val DBFlowDatabase = ClassName(PackageNames.Config, "DBFlowDatabase")
    val MutableHolder = ClassName(PackageNames.Config, "MutableHolder")
    val GeneratedDatabaseHolder = ClassName(PackageNames.Config, "GeneratedDatabaseHolder")

    val FlowCursor = ClassName(PackageNames.Database, "FlowCursor")
    val DatabaseWrapper = ClassName(PackageNames.Database, "DatabaseWrapper")

    fun propertyStatementWrapper(typeName: TypeName) =
        ClassName(PackageNames.Property, "PropertyStatementWrapper")
            .parameterizedBy(typeName)

    fun nullablePropertyStatementWrapper(typeName: TypeName) =
        ClassName(PackageNames.Property, "NullablePropertyStatementWrapper")
            .parameterizedBy(typeName)

    val DatabaseStatement = ClassName(PackageNames.Database, "DatabaseStatement")

    val ObjectType = ClassName(PackageNames.Adapter, "ObjectType")
}

object MemberNames {

    val property = MemberName(PackageNames.Property, "property")
    val quoteIfNeeded = MemberName(PackageNames.Core, "quoteIfNeeded")
    val propertyGet = MemberName(PackageNames.Property, "get")

    val bind = MemberName(PackageNames.Database, "bind")

    val select = MemberName(PackageNames.Query, "select")
    const val from = "from"
    const val where = "where"
    const val requireSingle = "requireSingle"
    const val querySingle = "querySingle"
    const val eq = "eq"
}