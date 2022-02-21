package com.grosner.dbflow5.codegen.kotlin.kotlinpoet

import com.dbflow5.codegen.shared.PackageNames
import com.squareup.kotlinpoet.MemberName

object MemberNames {

    val property = MemberName(PackageNames.QueryOperations, "property")
    val typeConvertedProperty = MemberName(PackageNames.QueryOperations, "typeConvertedProperty")
    val quoteIfNeeded = MemberName(PackageNames.Core, "quoteIfNeeded")
    val infer = MemberName(PackageNames.QueryOperations, "infer")
    val classToken = MemberName(PackageNames.Property, "classToken")
    val indexProperty = MemberName(PackageNames.QueryOperations, "indexProperty")

    val safeLet = MemberName(PackageNames.Adapter, "dbFlowInternalSafeLet")
    val let = MemberName(PackageNames.Adapter, "dbFlowInternalLet")
    val bind = MemberName(PackageNames.Database, "bind")
    val propertyBind = MemberName(PackageNames.QueryOperations, "bindProperty")

    val select = MemberName(PackageNames.Query, "select")
    val single = MemberName(PackageNames.Query, "single")
    val singleOrNull = MemberName(PackageNames.Query, "singleOrNull")
    val list = MemberName(PackageNames.Query, "list")

    const val from = "from"
    const val where = "where"
    const val requireSingle = "requireSingle"
    const val querySingle = "querySingle"
    const val queryList = "queryList"
    const val eq = "eq"

    val save = MemberName(PackageNames.Structure, "save")
    val chain = MemberName(PackageNames.Converter, "chain")

    val getOrThrow = MemberName("kotlin", "getOrThrow")

    val emptyAutoIncrementUpdater = MemberName(PackageNames.Adapter2, "emptyAutoIncrementUpdater")
    val modelAdapter = MemberName(PackageNames.Adapter2, "modelAdapter")
}