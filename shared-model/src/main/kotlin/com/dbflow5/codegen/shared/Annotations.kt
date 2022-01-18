package com.dbflow5.codegen.shared

import com.dbflow5.annotation.DBFlowKSPOnly
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.OneToManyRelation
import kotlin.reflect.KClass

/**
 * Description:
 */
sealed class Annotations(clazz: KClass<*>) {
    val qualifiedName = clazz.qualifiedName!!

    object Table : Annotations(com.dbflow5.annotation.Table::class)
    object Query : Annotations(com.dbflow5.annotation.Query::class)
    object View : Annotations(ModelView::class)
    object Database : Annotations(com.dbflow5.annotation.Database::class)
    object TypeConverter : Annotations(com.dbflow5.annotation.TypeConverter::class)
    object ManyToMany : Annotations(com.dbflow5.annotation.ManyToMany::class)
    object Migration : Annotations(com.dbflow5.annotation.Migration::class)
    @DBFlowKSPOnly
    object OneToMany : Annotations(OneToManyRelation::class)
    object MultipleManyToMany :
        Annotations(com.dbflow5.annotation.MultipleManyToMany::class)

    companion object {
        val values = listOf(
            Table,
            Query,
            View,
            Database,
            TypeConverter,
            ManyToMany,
            Migration,
            OneToMany,
            MultipleManyToMany,
        )
    }
}
