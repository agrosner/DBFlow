package com.grosner.dbflow5.codegen.kotlin.kotlinpoet

import com.dbflow5.codegen.shared.PackageNames
import com.squareup.kotlinpoet.MemberName

object MemberNames {

    val property = MemberName(PackageNames.Query2Operations, "property")
    val typeConvertedProperty = MemberName(PackageNames.Query2Operations, "typeConvertedProperty")
    val quoteIfNeeded = MemberName(PackageNames.Core, "quoteIfNeeded")
    val infer = MemberName(PackageNames.Query2Operations, "infer")
    val classToken = MemberName(PackageNames.Property, "classToken")
    val indexProperty = MemberName(PackageNames.Query2Operations, "indexProperty")

    val safeLet = MemberName(PackageNames.Adapter, "dbFlowInternalSafeLet")
    val let = MemberName(PackageNames.Adapter, "dbFlowInternalLet")
    val bind = MemberName(PackageNames.Database, "bind")
    val propertyBind = MemberName(PackageNames.Query2Operations, "bindProperty")

    val select = MemberName(PackageNames.Query2, "select")
    val single = MemberName(PackageNames.Query2, "single")
    val singleOrNull = MemberName(PackageNames.Query2, "singleOrNull")
    val list = MemberName(PackageNames.Query2, "list")

    const val from = "from"
    const val where = "where"
    const val requireSingle = "requireSingle"
    const val querySingle = "querySingle"
    const val queryList = "queryList"
    const val eq = "eq"

    val save = MemberName(PackageNames.Structure, "save")
    val chain = MemberName(PackageNames.Converter, "chain")

    val getOrThrow = MemberName("kotlin", "getOrThrow")
}