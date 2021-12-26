package com.dbflow5.ksp

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

object PackageNames {
    const val Core = "com.dbflow5"
    const val Query = "com.dbflow5.query"
    const val Property = "com.dbflow5.query.property"
    const val Adapter = "com.dbflow5.adapter"
    const val Config = "com.dbflow5.config"
    const val Database = "com.dbflow5.database"
    const val Converter = "com.dbflow5.converter"
    const val Structure = "com.dbflow5.structure"
    const val Annotation = "com.dbflow5.annotation"
}

object ClassNames {

    val BaseModel = ClassName(PackageNames.Structure, "BaseModel")

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
        .parameterizedBy(WildcardTypeName.producerOf(Any::class.asTypeName().copy(nullable = true)))
    val OperatorGroup = ClassName(PackageNames.Query, "OperatorGroup")
    fun modelAdapter(typeName: TypeName) =
        ClassName(PackageNames.Adapter, "ModelAdapter").parameterizedBy(typeName)

    fun retrievalAdapter(typeName: TypeName) =
        ClassName(PackageNames.Adapter, "RetrievalAdapter").parameterizedBy(typeName)

    fun modelViewAdapter(typeName: TypeName) =
        ClassName(PackageNames.Adapter, "ModelViewAdapter").parameterizedBy(typeName)

    val DBFlowDatabase = ClassName(PackageNames.Config, "DBFlowDatabase")
    val MutableHolder = ClassName(PackageNames.Config, "MutableHolder")
    val GeneratedDatabaseHolder = ClassName(PackageNames.Config, "GeneratedDatabaseHolder")
    val DatabaseHolder = ClassName(PackageNames.Config, "DatabaseHolder")

    val FlowCursor = ClassName(PackageNames.Database, "FlowCursor")
    val DatabaseWrapper = ClassName(PackageNames.Database, "DatabaseWrapper")

    val DatabaseStatement = ClassName(PackageNames.Database, "DatabaseStatement")

    val ObjectType = ClassName(PackageNames.Adapter, "ObjectType")
    val TypeConverter = ClassName(PackageNames.Converter, "TypeConverter")
    val TypeConverterWithProjection = TypeConverter.parameterizedBy(
        WildcardTypeName.producerOf(
            Any::class.asClassName().copy(
                nullable = true
            ),
        ),
        WildcardTypeName.producerOf(
            Any::class.asClassName().copy(
                nullable = true
            ),
        ),
    )
}

object MemberNames {

    val property = MemberName(PackageNames.Property, "property")
    val typeConvertedProperty = MemberName(PackageNames.Property, "typeConvertedProperty")
    val quoteIfNeeded = MemberName(PackageNames.Core, "quoteIfNeeded")
    val infer = MemberName(PackageNames.Property, "infer")
    val classToken = MemberName(PackageNames.Property, "classToken")
    val indexProperty = MemberName(PackageNames.Property, "indexProperty")

    val bind = MemberName(PackageNames.Database, "bind")
    val propertyBind = MemberName(PackageNames.Property, "bindProperty")

    val select = MemberName(PackageNames.Query, "select")
    const val from = "from"
    const val where = "where"
    const val requireSingle = "requireSingle"
    const val querySingle = "querySingle"
    const val eq = "eq"

    val chain = MemberName(PackageNames.Converter, "chain")
}