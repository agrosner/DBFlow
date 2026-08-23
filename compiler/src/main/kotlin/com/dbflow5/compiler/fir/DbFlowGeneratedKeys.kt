package com.dbflow5.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object DbFlowCompanionKey : GeneratedDeclarationKey() {
    override fun toString(): String = "DBFlow table companion"
}

internal object DbFlowCompanionPropertyKey : GeneratedDeclarationKey() {
    override fun toString(): String = "DBFlow companion property"
}

internal object DbFlowColumnPropertyKey : GeneratedDeclarationKey() {
    override fun toString(): String = "DBFlow companion column"
}

internal val DBFLOW_MODEL_PREDICATE: org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate =
    org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.create {
        annotated(
            DbFlowFqNames.Table,
            DbFlowFqNames.Query,
            DbFlowFqNames.ModelView,
        )
    }

internal object DbFlowFqNames {
    val Table = FqName("com.dbflow5.annotation.Table")
    val Query = FqName("com.dbflow5.annotation.Query")
    val ModelView = FqName("com.dbflow5.annotation.ModelView")
    val ColumnIgnore = FqName("com.dbflow5.annotation.ColumnIgnore")
    val Column = FqName("com.dbflow5.annotation.Column")
    val ForeignKey = FqName("com.dbflow5.annotation.ForeignKey")
    val ColumnMap = FqName("com.dbflow5.annotation.ColumnMap")
    val TypeConverter = FqName("com.dbflow5.converter.TypeConverter")
    val AdapterCompanion = FqName("com.dbflow5.adapter.AdapterCompanion")
    val PropertyStart = FqName("com.dbflow5.query.operations.PropertyStart")
    val TypeConvertedProperty = FqName("com.dbflow5.query.operations.TypeConvertedProperty")
    val ModelAdapter = FqName("com.dbflow5.adapter.ModelAdapter")
    val ViewAdapter = FqName("com.dbflow5.adapter.ViewAdapter")
    val QueryAdapter = FqName("com.dbflow5.adapter.QueryAdapter")
    val ModelAdapterImpl = FqName("com.dbflow5.adapter.ModelAdapterImpl")
    val ViewAdapterImpl = FqName("com.dbflow5.adapter.ViewAdapterImpl")
    val QueryAdapterImpl = FqName("com.dbflow5.adapter.QueryAdapterImpl")
    val KClass = FqName("kotlin.reflect.KClass")
}

internal object DbFlowClassIds {
    val AdapterCompanion = ClassId.topLevel(DbFlowFqNames.AdapterCompanion)
    val PropertyStart = ClassId.topLevel(DbFlowFqNames.PropertyStart)
    val ModelAdapter = ClassId.topLevel(DbFlowFqNames.ModelAdapter)
    val ViewAdapter = ClassId.topLevel(DbFlowFqNames.ViewAdapter)
    val QueryAdapter = ClassId.topLevel(DbFlowFqNames.QueryAdapter)
    val ModelAdapterImpl = ClassId.topLevel(DbFlowFqNames.ModelAdapterImpl)
    val ViewAdapterImpl = ClassId.topLevel(DbFlowFqNames.ViewAdapterImpl)
    val QueryAdapterImpl = ClassId.topLevel(DbFlowFqNames.QueryAdapterImpl)
    val KClass = ClassId.topLevel(DbFlowFqNames.KClass)
    val Table = ClassId.topLevel(DbFlowFqNames.Table)
    val Query = ClassId.topLevel(DbFlowFqNames.Query)
    val ModelView = ClassId.topLevel(DbFlowFqNames.ModelView)
    val ColumnIgnore = ClassId.topLevel(DbFlowFqNames.ColumnIgnore)
    val Column = ClassId.topLevel(DbFlowFqNames.Column)
    val ForeignKey = ClassId.topLevel(DbFlowFqNames.ForeignKey)
    val ColumnMap = ClassId.topLevel(DbFlowFqNames.ColumnMap)
    val TypeConverter = ClassId.topLevel(DbFlowFqNames.TypeConverter)
}

internal val DBFLOW_MODEL_CLASS_IDS = setOf(
    DbFlowClassIds.Table,
    DbFlowClassIds.Query,
    DbFlowClassIds.ModelView,
)

internal val TABLE_PROPERTY = Name.identifier("table")
