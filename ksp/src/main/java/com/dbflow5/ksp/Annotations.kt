package com.dbflow5.ksp

import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.QueryModel

/**
 * Description:
 */
enum class Annotations(val qualifiedName: String) {
    Table(com.dbflow5.annotation.Table::class.qualifiedName!!),
    Query(QueryModel::class.qualifiedName!!),
    View(ModelView::class.qualifiedName!!),
    Database(com.dbflow5.annotation.Database::class.qualifiedName!!),
    TypeConverter(com.dbflow5.annotation.TypeConverter::class.qualifiedName!!),
    ManyToMany(com.dbflow5.annotation.ManyToMany::class.qualifiedName!!),
}
